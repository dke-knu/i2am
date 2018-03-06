package org.apache.storm.messaging.jxio;

import org.accelio.jxio.*;
import org.accelio.jxio.exceptions.JxioGeneralException;
import org.accelio.jxio.exceptions.JxioQueueOverflowException;
import org.accelio.jxio.exceptions.JxioSessionClosedException;
import org.apache.storm.Config;
import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.metric.api.IStatefulObject;
import org.apache.storm.serialization.KryoValuesDeserializer;
import org.apache.storm.utils.StormBoundedExponentialBackoffRetry;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;


public class Client extends ConnectionWithStatus implements IStatefulObject {
    private static final long PENDING_MESSAGES_FLUSH_TIMEOUT_MS = 600000L;
    private static final long PENDING_MESSAGES_FLUSH_INTERVAL_MS = 1000L;

    private static final Logger LOG = LoggerFactory.getLogger(Client.class.getCanonicalName());

    private Map storm_conf;
    private volatile boolean closing = false;
    private final Context context;
    private final ScheduledThreadPoolExecutor scheduler;
    private final StormBoundedExponentialBackoffRetry retryPolicy;
    private static final long NO_DELAY_MS = 0L;
    private static final Timer timer = new Timer("JXIO-SessionAlive-Timer", true);

    private volatile Map<Integer, Double> serverLoad = null;

    private final AtomicInteger totalConnectionAttempts = new AtomicInteger(0);
    private final AtomicInteger connectionAttempts = new AtomicInteger(0);
    private final AtomicInteger messagesSent = new AtomicInteger(0);
    private final AtomicInteger messagesLost = new AtomicInteger(0);
    private final long SESSION_ALIVE_INTERVAL_MS = 30000L;
    private final AtomicLong pendingMessages = new AtomicLong(0);
    private final AtomicBoolean saslChannelReady = new AtomicBoolean(false);

    private final InetSocketAddress dstAddress;

    private final MessageBuffer batcher;

    private int numMessages;
    private volatile boolean loadMetricsFlag = false;
    private final Object writeLock = new Object();

    private final short LOAD_METRICS_NO = -900;
    private final short LOAD_METRICS_REQ = -901;

    //JXIO's
    private MsgPool msgPool;
    private Msg msg;
    private final EventQueueHandler eqh;
    private final AtomicReference<ClientSession> sessionRef = new AtomicReference<>();
    private ClientSession cs;
    private URI uri;
//    private ExecutorService eqhThread;

    public Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
        int messageBatchSize = Utils.getInt(stormConf.get(Config.STORM_JXIO_MESSAGE_BATCH_SIZE));
        this.eqh = new EventQueueHandler(null);
        this.msgPool = new MsgPool(
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_BUFFER_COUNT)),
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_MINIMUM_BUFFER_SIZE)),
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_BUFFER_SIZE)));

        this.storm_conf = stormConf;
        this.context = context;
        this.scheduler = scheduler;
        int maxReconnectionAttempts = 29; //Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MAX_RETRIES));
        int minWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MIN_SLEEP_MS));
        int maxWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MAX_SLEEP_MS));
        retryPolicy = new StormBoundedExponentialBackoffRetry(minWaitMs, maxWaitMs, maxReconnectionAttempts);
        saslChannelReady.set(!Utils.getBoolean(stormConf.get(Config.STORM_MESSAGING_NETTY_AUTHENTICATION), false));

        LOG.info("creating JXIO client, connecting to {}:{}, bufferSize: {}", host, port, messageBatchSize);
//        eqhThread = Executors.newCachedThreadPool(new JxioRenameThreadFactory("Client EQH thread"));

        try {
            uri = new URI(String.format("rdma://%s:%s", host, port));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        dstAddress = new InetSocketAddress(host, port);
        launchSessionAliveThread();
        scheduleConnect(NO_DELAY_MS);
        batcher = new MessageBuffer(messageBatchSize);
//        eqhThread.submit(eqh);
    }

    private void launchSessionAliveThread() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    LOG.debug("running timer task, address {}", dstAddress);
//                    LOG.info("[Client-AliveThread] running timer task, address {}", dstAddress);
                    if (closing) {
                        this.cancel();
                        return;
                    }
                    getAliveSession();
                } catch (Exception e) {
                    LOG.error("Session connection error {}", e);
                }
            }
        }, 0, SESSION_ALIVE_INTERVAL_MS);
    }

    @Override
    public Object getState() {
        LOG.debug("Getting metrics for client connection to {}", uri.toString());
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("reconnects", totalConnectionAttempts.getAndSet(0));
        ret.put("sent", messagesSent.getAndSet(0));
        ret.put("pending", pendingMessages.get());
        ret.put("lostOnSend", messagesLost.getAndSet(0));
        ret.put("dest", dstAddress.toString());
        String src = getLocalServerIp();
        if (src != null) {
            ret.put("src", src);
        }
        return ret;
    }

    /**
     * Receiving messages is not supported by a client.
     *
     * @throws java.lang.UnsupportedOperationException whenever this method is being called.
     */
    @Override
    public void registerRecv(IConnectionCallback cb) {
        throw new UnsupportedOperationException("Client connection should not receive any messages");
    }

    @Override
    public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {
        throw new RuntimeException("Client connection should not send load metrics");
    }

    @Override
    public void send(int taskId, byte[] payload) {
        TaskMessage msg = new TaskMessage(taskId, payload);
        List<TaskMessage> wrapper = new ArrayList<TaskMessage>(1);
        wrapper.add(msg);
        send(wrapper.iterator());
    }

    @Override
    public void send(Iterator<TaskMessage> msgs) {
        if (closing) {
            numMessages = iteratorSize(msgs);
            LOG.error("discarding {} messages because the JXIO client to {} is being closed", numMessages, uri.toString());
            return;
        }
        if (!hasMessages(msgs)) {
            return;
        }
        ClientSession cs = getAliveSession();
        if (cs == null) {
            dropMessages(msgs);
            LOG.error("Drop {} messages because of ClientSession null...", iteratorSize(msgs));
            return;
        }
//        synchronized (writeLock) {
            while (msgs.hasNext()) {
                //use batch
                TaskMessage message = msgs.next();

                MessageBatch full2 = batcher.checkAdd(message);
                if (full2 != null) {
                    flushMessages(full2);
                }

            /*MessageBatch full = batcher.add(message);
            if (full != null) {
                //Need to make Msg each time.
                flushMessages(full);
            }*/
            }
//        }

//        synchronized (writeLock) {
            MessageBatch batch = batcher.drain();
            if (batch != null) {
                flushMessages(batch);
            }
//        }
    }

    private void flushMessages(final MessageBatch batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        numMessages = batch.size();

//        for(StackTraceElement ste : Thread.currentThread().getStackTrace())
//            LOG.info(""+ste);

        pendingMessages.addAndGet(numMessages);
        do {
            msg = msgPool.getMsg();
        } while(msgPool.isEmpty());

        LOG.debug("[seokwoo-error-checkpoint] before eqh send to " + uri.getHost() + " : " + uri.getPort()
                + " msgPool capacity:count = " + msgPool.capacity() + " : " + msgPool.count());
        ByteBuffer bb = null;
        try {
            //maximum batch size is 262144B (256KB)
            //so, Msg size must be upper than 256KB
            bb = batch.buffer();
            if(loadMetricsFlag) {
                loadMetricsFlag = false;
                msg.getOut().putShort(LOAD_METRICS_REQ);
            } else {
                msg.getOut().putShort(LOAD_METRICS_NO);
            }
//            msg.getOut().putShort(LOAD_METRICS_NO);
            msg.getOut().put(bb.array());
        } catch (Exception e) {
            LOG.error("[seokwoo-error-checkpoint] send fail to " + uri.getHost() + " : " + uri.getPort() + " msgPool capacity:count = " + msgPool.capacity() + " : " + msgPool.count());
            LOG.error("[seokwoo-error-checkpoint] bb.array().length = " + bb.array().length);
            LOG.error("[seokwoo-error-checkpoint] loadMetricsFlag = " + loadMetricsFlag);
            LOG.error("writing {}:{} to {}:{}", numMessages, bb.array().length, uri.getHost(), uri.getPort());
            messagesLost.getAndAdd(numMessages);
        }
        try {
            cs.sendRequest(msg);
//            LOG.debug("writing {}, msg = {}, messages to session {}", numMessages, msg.getOut().toString(), uri.toString());
        } catch (JxioGeneralException e) {
            failSendMessages(numMessages);
            msgPool.releaseMsg(msg);
            e.printStackTrace();
        } catch (JxioSessionClosedException e) {
            failSendMessages(numMessages);
            msgPool.releaseMsg(msg);
            e.printStackTrace();
        } catch (JxioQueueOverflowException e) {
            failSendMessages(numMessages);
            msgPool.releaseMsg(msg);
            e.printStackTrace();
        }
        eqh.runEventLoop(1, -1);
        LOG.debug("[seokwoo-error-checkpoint] after eqh send to " + uri.getHost() + " : " + uri.getPort()
                + " msgPool capacity:count = " + msgPool.capacity() + " : " + msgPool.count());
    }

    @Override
    public void requestLoadMectrics() {
        loadMetricsFlag = true;
        /*
        ClientSession cs = sessionRef.get();
        Msg msg = msgPool.getMsg();
        msg.getOut().putShort(LOAD_METRICS_REQ);
        LOG.debug("[seokwoo-error-checkpoint] request loadmetrics before eqh send to " + uri.getHost() + " : " + uri.getPort()
                + " msgPool capacity:count = " + msgPool.capacity() + " : " + msgPool.count() + ", metrics = " + serverLoad);
        if (cs != null) {
            synchronized (writeLock) {
                try {
                    cs.sendRequest(msg);
                } catch (JxioGeneralException e) {
                    LOG.error("sendRequest error, JxioGeneralException to {}", uri.toString());
                    msgPool.releaseMsg(msg);
                    e.printStackTrace();
                } catch (JxioSessionClosedException e) {
                    LOG.error("sendRequest error, JxioSessionClosedException to {}", uri.toString());
                    msgPool.releaseMsg(msg);
                    e.printStackTrace();
                } catch (JxioQueueOverflowException e) {
                    LOG.error("sendRequest error, JxioQueueOverflowException to {}", uri.toString());
                    msgPool.releaseMsg(msg);
                    e.printStackTrace();
                }
                eqh.runEventLoop(1, -1);
//                LOG.info("[seokwoo-error-checkpoint] metrics = {}", serverLoad);
                LOG.debug("[seokwoo-error-checkpoint] request loadmetrics after eqh send to " + uri.getHost() + " : " + uri.getPort()
                        + " msgPool capacity:count = " + msgPool.capacity() + " : " + msgPool.count() + ", metrics = " + serverLoad);
            }
        } else {
            LOG.warn("Not exist ClientSession");
        }*/
    }

    private void dropMessages(Iterator<TaskMessage> msgs) {
        // We consume the iterator by traversing and thus "emptying" it.
        int msgCount = iteratorSize(msgs);
        messagesLost.getAndAdd(msgCount);
    }

    private ClientSession getAliveSession() {
        ClientSession cs = sessionRef.get();
        if (connectionEstablished(cs)) {
            return cs;
        } else {
            // Closing the channel and reconnecting should be done before handling the messages.
            boolean reconnectScheduled = closeSessionAndReconnect(cs);
            if (reconnectScheduled) {
                // Log the connection error only once
                LOG.error("connection to {} is unavailable", uri.toString());
            }
            return null;
        }
    }

    private boolean hasMessages(Iterator<TaskMessage> msgs) {
        return msgs != null && msgs.hasNext();
    }

    private int iteratorSize(Iterator<TaskMessage> msgs) {
        int size = 0;
        if (msgs != null) {
            while (msgs.hasNext()) {
                size++;
                msgs.next();
            }
        }
        return size;
    }

    /**
     * Schedule a reconnect if we closed a non-null channel, and acquired the right to
     * provide a replacement by successfully setting a null to the channel field
     *
     * @param cs
     * @return if the call scheduled a re-connect task
     */
    private boolean closeSessionAndReconnect(ClientSession cs) {
        if (cs != null) {
            cs.close();
            if (sessionRef.compareAndSet(cs, null)) {
                scheduleConnect(NO_DELAY_MS);
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        if (!closing) {
            LOG.info("closing JXIO Client {}", uri.toString());
            context.removeClient(uri.getHost(), uri.getPort());
            closing = true;
            waitForPendingMessagesToBeSent();
            releaseResources();
            closeSession();
        }
    }

    @Override
    public Status status() {
        if (closing) {
            return Status.Closed;
        } else if (!connectionEstablished(sessionRef.get())) {
            LOG.debug("[Client-status] Connecting " + uri.toString());
            return Status.Connecting;
        } else {
            if (saslChannelReady.get()) {
                LOG.debug("[Client-status] Ready " + uri.toString());
                return Status.Ready;
            } else {
                return Status.Connecting; // need to wait until sasl channel is also ready
            }
        }
    }

    private boolean connectionEstablished(ClientSession cs) {
        if (cs != null) {
            if (!cs.getIsClosing()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void scheduleConnect(long delayMs) {
        scheduler.schedule(new Connect(this, uri), delayMs, TimeUnit.MILLISECONDS);
    }

    private boolean reconnectingAllowed() {
        return !closing;
    }

    public void releaseResources() {
        msgPool.deleteMsgPool();
        LOG.info("Client releaseResources");
//        eqhThread.shutdown();
        eqh.stop();
    }

    private String getLocalServerIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public InetSocketAddress getDstAddress() {
        return dstAddress;
    }

    public void setServerLoad(Map<Integer, Double> serverLoad) {
        LOG.debug("set Server Load");
        this.serverLoad = serverLoad;
    }

    @Override
    public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
        Map<Integer, Double> loadCache = serverLoad;
        Map<Integer, Load> ret = new HashMap<Integer, Load>();
        if (loadCache != null) {
            double clientLoad = Math.min(pendingMessages.get(), 1024) / 1024.0;
            for (Integer task : tasks) {
                Double found = loadCache.get(task);
                if (found != null) {
                    ret.put(task, new Load(true, found, clientLoad));
                }
            }
        }
        return ret;
    }

    private void waitForPendingMessagesToBeSent() {
        LOG.info("waiting up to {} ms to send {} pending messages to {}",
                PENDING_MESSAGES_FLUSH_TIMEOUT_MS, pendingMessages.get(), uri.toString());
        long totalPendingMsgs = pendingMessages.get();
        long startMs = System.currentTimeMillis();
        while (pendingMessages.get() != 0) {
            try {
                long deltaMs = System.currentTimeMillis() - startMs;
                if (deltaMs > PENDING_MESSAGES_FLUSH_TIMEOUT_MS) {
                    LOG.error("failed to send all pending messages to {} within timeout, {} of {} messages were not " +
                            "sent", uri.toString(), pendingMessages.get(), totalPendingMsgs);
                    break;
                }
                Thread.sleep(PENDING_MESSAGES_FLUSH_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void closeSession() {
        ClientSession cs = sessionRef.get();
        if (cs != null) {
            cs.close();
            LOG.info("channel to {} closed", uri.toString());
        }
    }

    private void failSendMessages(int numMessages) {
        LOG.error("Failed to send {} messages to {}", numMessages, uri.toString());
        closeSessionAndReconnect(sessionRef.get());
        messagesLost.getAndAdd(numMessages);
    }

    private class Connect extends TimerTask {
        private final URI uri;
        private Client client;

        public Connect(Client client, URI uri) {
            this.uri = uri;
            this.client = client;
        }

        @Override
        public void run() {
            if (reconnectingAllowed()) {
                final int connectionAttempt = connectionAttempts.getAndIncrement();
                totalConnectionAttempts.getAndIncrement();

                LOG.info("connecting to {}:{} [attempt {}]", uri.getHost(), uri.getPort(), connectionAttempt);
                cs = new ClientSession(eqh, uri, new ClientSessionCallbacks(client, storm_conf));

                //netty's add listener
                eqh.runEventLoop(1, EventQueueHandler.INFINITE_DURATION); //listen session established

/*                if (sessionRef.get() != null) {
                    LOG.info("ClientSession started so, start eqh thread, already running => {}", eqh.getInRunEventLoop());
                    eqhThread.submit(eqh);
                } else {
                    LOG.error("[Client]Connect run but, ClientSession null point Exception");
                }
                */
            } else {
                close();
                throw new RuntimeException("Giving up to scheduleConnect to " + uri.toString() + " after " +
                        connectionAttempts + " failed attempts. " + messagesLost.get() + " messages were lost");
            }
        }
    }

    private class ClientSessionCallbacks implements ClientSession.Callbacks {
        private Client client;
        private KryoValuesDeserializer _des;

        ClientSessionCallbacks(Client client, Map conf) {
            this.client = client;
            _des = new KryoValuesDeserializer(conf);
        }

        @Override
        public void onResponse(Msg msg) {
            LOG.debug("[seokwoo-error-checkpoint]11 count = {}, to {} : {},", msgPool.count(), uri.getHost(), uri.getPort());
            ByteBuffer bb = msg.getIn();
            short code = bb.getShort();

            if (code == LOAD_METRICS_REQ) {
                LOG.debug("[seokwoo-error-checkpoint]22 LOAD_METRICS_REQ count = {}, to {} : {}, metrics = {}"
                        , msgPool.count(), uri.getHost(), uri.getPort(), client.serverLoad);
                Object obj = decoder(bb);
                if (obj instanceof ControlMessage) {
                    ControlMessage ctrl_msg = (ControlMessage) obj;
                    if (ctrl_msg == ControlMessage.FAILURE_RESPONSE) {
                        LOG.info("failure response:{}", msg);
                    }
                } else if (obj instanceof List) {
                    try {
                        List<TaskMessage> list = (List<TaskMessage>) obj;

                        if (list.size() < 1) {
                            LOG.error("Didn't see enough load metrics (" + client.getDstAddress() + ") " + list);
                            client.setServerLoad(null);
                            msgPool.releaseMsg(msg);
                            return;
                        }

                        TaskMessage tm = ((List<TaskMessage>) obj).get(list.size() - 1);
                        if (tm.task() != -1) {
                            LOG.error("Metrics messages are sent to the system task (" + client.getDstAddress() + ") " + tm);
                            client.setServerLoad(null);
                            msgPool.releaseMsg(msg);
                            return;
                        }

                        List metrics = _des.deserialize(tm.message());
                        if (metrics.size() < 1) {
                            LOG.error("No metrics data in the metrics message (" + client.getDstAddress() + ") " + metrics);
                            client.setServerLoad(null);
                            msgPool.releaseMsg(msg);
                            return;
                        }

                        if (!(metrics.get(0) instanceof Map)) {
                            LOG.error("The metrics did not have a map in the first slot (" + client.getDstAddress() + ") ");
                            client.setServerLoad(null);
                            msgPool.releaseMsg(msg);
                            return;
                        }

                        client.setServerLoad((Map<Integer, Double>) metrics.get(0));
                    } catch (IOException e) {
//                        throw new RuntimeException(e);
                        LOG.error(e.getMessage());
                    }
//                    LOG.debug("[seokwoo-error-checkpoint]Load Metrics success");
                } else {
                    LOG.error("Don't know how to handle a message of type "
                            + obj + " (" + client.getDstAddress() + ")");
                }
            } else if (code == LOAD_METRICS_NO) {
                LOG.debug("[seokwoo-error-checkpoint]22 LOAD_METRICS_NO count = {}, to {} : {},", msgPool.count(), uri.getHost(), uri.getPort());
                LOG.debug("[seokwoo-error-checkpoint] sent {} messages to {}:{}", numMessages, uri.getHost(), uri.getPort());
            }


            pendingMessages.addAndGet(0 - numMessages);
            messagesSent.getAndSet(numMessages);
            msgPool.releaseMsg(msg);

            LOG.debug("[seokwoo-error-checkpoint]aa count = {}, to {} : {}, serverLoad = {}", msgPool.count(), uri.getHost(), uri.getPort(), client.serverLoad);
        }

        @Override
        public void onSessionEstablished() {
            boolean setSession = sessionRef.compareAndSet(null, cs);
            checkState(setSession);
            LOG.info("Successfully connected to {}:{}", uri.getHost(), uri.getPort());

            if (messagesLost.get() > 0) {
                LOG.warn("Re-connection to {} was successful but {} messages has been lost so far ", uri.toString(), messagesLost.get());
            }
        }

        @Override
        public void onSessionEvent(EventName eventName, EventReason eventReason) {
            String str = "[Client][EVENT] Got event " + eventName + " because of " + eventReason + " to " + uri.toString();
            if (eventName == EventName.SESSION_CLOSED && eventReason != EventReason.CONNECT_ERROR) { // normal exit
                LOG.info(str);
                if (closing) {
                    LOG.info("closing true releaseResources");
                    eqh.stop();
                    close();
                } else {
//                    eqhThread.shutdown();
                    LOG.info("closing false do reconnect no delay");
//                    scheduleConnect(NO_DELAY_MS);
                    closeSessionAndReconnect(sessionRef.get());
                }
            } else {
                LOG.error(str);
                LOG.info("Re connect to {}:{}", uri.getHost(), uri.getPort());
                long nextDelayMs = retryPolicy.getSleepTimeMs(connectionAttempts.get(), 0);
                LOG.info("nextDelayMS = {}", nextDelayMs);
                cs = null;
                scheduleConnect(nextDelayMs);
            }
        }

        @Override
        public void onMsgError(Msg msg, EventReason eventReason) {
            LOG.error("MSG error reason is={} to {}", eventReason, uri.toString());
            LOG.error("MSG = {} lost = {}", msg.toString(), numMessages);
            ClientSession cs = sessionRef.get();
            if (!cs.getIsClosing()) {
                try {
                    LOG.info("[Client-onMsgError] re send the messages if ClientSession alive");
                    cs.sendRequest(msg);
                } catch (JxioGeneralException e) {
                    e.printStackTrace();
                } catch (JxioSessionClosedException e) {
                    e.printStackTrace();
                } catch (JxioQueueOverflowException e) {
                    e.printStackTrace();
                }
            } else {
                messagesLost.getAndAdd(numMessages);
                msg.returnToParentPool();
            }
        }

        private Object decoder(ByteBuffer buf) {
            long available = buf.remaining();
            if (available < 2) {
                //need more data
                return null;
            }
            List<Object> ret = new ArrayList<>();

            //Use while loop, try to decode as more messages as possible in single call
            while (available >= 2) {

                // Mark the current buffer position before reading task/len field
                // because the whole frame might not be in the buffer yet.
                // We will reset the buffer position to the marked position if
                // there's not enough bytes in the buffer.
                buf.mark();

                short code = buf.getShort();
                available -= 2;

                //case 1: Control message
                //who send controlmessage?
                ControlMessage ctrl_msg = ControlMessage.mkMessage(code);
                if (ctrl_msg != null) {
                    if (ctrl_msg == ControlMessage.EOB_MESSAGE) {
                        continue;
                    } else {
                        return ctrl_msg;
                    }
                }
                //case 2: SaslTokenMeesageRequest
                //skip

                //case 3: TaskMessage
                //Make sure that we have received at least an integer (length)
                if (available < 4) {
                    //need more data
                    buf.reset();
                    break;
                }
                //Read the length field.
                int length = buf.getInt();
                available -= 4;

                if (length <= 0) {
                    ret.add(new TaskMessage(code, null));
                    break;
                }

                //Make sure if there's enough bytes in the buffer.
                if (available < length) {
                    //The whole bytes were not received yet - return null.
                    buf.reset();
                    break;
                }
                available -= length;

                //There's enough bytes in the buffer. Read it.
                byte[] payload = new byte[length];
                buf.get(payload);

                //Successfully decoded a frame.
                //Return a TaskMessage object
                ret.add(new TaskMessage(code, payload));
            }

            if (ret.size() == 0) {
                return null;
            } else {
                return ret;
            }
        }
    }
}