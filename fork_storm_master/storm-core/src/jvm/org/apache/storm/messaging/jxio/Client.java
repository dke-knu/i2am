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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final Object writeLock = new Object();

    //JXIO's
    private final MsgPool msgPool;
    private final EventQueueHandler eqh;
    private final AtomicReference<ClientSession> sessionRef = new AtomicReference<>();
    private ClientSession cs;
    private URI uri;
//    private ExecutorService eqhThread;

    public Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
        this.eqh = new EventQueueHandler(null);
        this.msgPool = new MsgPool(
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_BUFFER_COUNT)),
                200,
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_BUFFER_SIZE)));

        this.storm_conf = stormConf;
        this.context = context;
        this.scheduler = scheduler;
        int maxReconnectionAttempts = 29; //Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MAX_RETRIES));
        int minWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MIN_SLEEP_MS));
        int maxWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MAX_SLEEP_MS));
        retryPolicy = new StormBoundedExponentialBackoffRetry(minWaitMs, maxWaitMs, maxReconnectionAttempts);
        saslChannelReady.set(!Utils.getBoolean(stormConf.get(Config.STORM_MESSAGING_NETTY_AUTHENTICATION), false));
        int messageBatchSize = Utils.getInt(stormConf.get(Config.STORM_JXIO_MESSAGE_BATCH_SIZE), 262144);

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
    }

    private void launchSessionAliveThread() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    LOG.debug("running timer task, address {}", dstAddress);
//                    LOG.info("[Client-AliveThread] running timer task, address {}", dstAddress);
                    if (closing) {
                        LOG.info("[Client-AliveThread] closing");
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
        LOG.info("Getting metrics for client connection to {}", uri.toString());
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
            int numMessages = iteratorSize(msgs);
            LOG.error("discarding {} messages because the JXIO client to {} is being closed", numMessages, uri.toString());
            return;
        }
        if (!hasMessages(msgs)) {
            return;
        }
        ClientSession cs = getAliveSession();
        if (cs == null) {
            dropMessages(msgs);
            return;
        }
        int count = 0;
        synchronized (writeLock) {
            while (msgs.hasNext()) {
//                flushMessages(msgs.next());

                //use batch
                TaskMessage message = msgs.next();
                MessageBatch full = batcher.add(message);
                if (full != null) {
                    //Need to make Msg each time.
                    flushMessages(full);
                }
            }
        }

        //if channel.isWritable
        synchronized (writeLock) {
            MessageBatch batch = batcher.drain();
            if (batch != null) {
                flushMessages(batch);
            }
        }
    }

    private void flushMessages(TaskMessage message) {
        if (message == null) {
            return;
        }
        final int numMessages = message.serialize().array().length;
        LOG.debug("writing {} messages to session {}", numMessages, uri.toString());
        LOG.info("writing {} messages to session {}", numMessages, uri.toString());
        pendingMessages.addAndGet(numMessages);
        Msg msg = msgPool.getMsg();
        String fromIp = getLocalServerIp();
        msg.getOut().put(fromIp.getBytes());
        msg.getOut().put(message.serialize().array());

        try {
            cs.sendRequest(msg);
        } catch (JxioGeneralException e) {
            e.printStackTrace();
        } catch (JxioSessionClosedException e) {
            e.printStackTrace();
        } catch (JxioQueueOverflowException e) {
            e.printStackTrace();
        }
        eqh.runEventLoop(1, -1);
    }

    private void flushMessages(final MessageBatch batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        final int numMessages = batch.size();
        LOG.debug("writing {} messages to session {}", batch.size(), uri.toString());
        pendingMessages.addAndGet(numMessages);

        Msg msg = msgPool.getMsg();
        String fromIp = getLocalServerIp();
        //need 13 bytes to store ip address ex) 192.168.1.100
        msg.getOut().put(fromIp.getBytes());
        try {
            //maximum batch size is 262144B (256KB)
            //so, Msg size must be upper than 256KB
            ByteBuffer bb = batch.buffer();
            byte[] tempByte = new byte[bb.limit()];
            bb.flip();
            bb.get(tempByte);
            msg.getOut().put(tempByte);
        } catch (Exception e) {
            LOG.error("[Client-flushMessages] put message to bytebuffer error");
            e.printStackTrace();
        }
        try {
            cs.sendRequest(msg);
            LOG.info("send msg: {} from {} to {}:{}", msg.toString(), getLocalServerIp(), uri.getHost(), uri.getPort());
        } catch (JxioGeneralException e) {
            failSendMessages(numMessages);
            e.printStackTrace();
        } catch (JxioSessionClosedException e) {
            failSendMessages(numMessages);
            e.printStackTrace();
        } catch (JxioQueueOverflowException e) {
            failSendMessages(numMessages);
            e.printStackTrace();
        }
        eqh.runEventLoop(1, -1);
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
            LOG.info("[Client-getAliveSession] ClientSession is null");
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
            LOG.info("[Client] to uri = {} Connecting session={}", uri.toString(), (sessionRef.get() != null));
            return Status.Connecting;
        } else {
            if (saslChannelReady.get()) {
                LOG.info("[Client] to uri = {} Ready", uri.toString());
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
                LOG.info("[Client-connectionEstablished] cs.getIsClosing={}", !cs.getIsClosing());
                return false;
            }
        } else {
            LOG.info("[Client-connectionEstablished] Client Session is null");
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
//        eqhThread.shutdown();
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

    @Override
    public void requestLoadMectrics() {
        Msg loadMsg = msgPool.getMsg();
        try {
            ByteBuffer bb = ControlMessage.LOADMETRICS_REQUEST.buffer();
            byte[] loadByte = new byte[2];
            bb.flip();
            bb.put(loadByte);
            loadMsg.getOut().put(loadByte);
            loadMsg.getOut().flip();
            LOG.info("[Client] LoadMetrics message = {}", loadMsg.getOut().getShort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            cs.sendRequest(loadMsg);
            LOG.info("[Client] request load metrics");
        } catch (JxioGeneralException e) {
            e.printStackTrace();
        } catch (JxioSessionClosedException e) {
            e.printStackTrace();
        } catch (JxioQueueOverflowException e) {
            e.printStackTrace();
        }
        LOG.info("[Client] run event loop");
        eqh.runEventLoop(1, -1);
        LOG.info("[Client] complete event loop");
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
            LOG.debug("channel to {} closed", uri.toString());
            LOG.info("channel to {} closed", uri.toString());
        }
    }

    private void failSendMessages(int numMessages) {
        LOG.error("failed to send {} messages to {}", numMessages, uri.toString());
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
                eqh.runEventLoop(1, -1); //listen session established

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
            if (msg.getIn().remaining() >= 2) {
                LOG.info("[Client] load metrics response");
                ByteBuffer bb = msg.getIn();
                int taskId = bb.getShort();
                byte[] message = new byte[bb.limit() - 2];
                bb.get(message);
                TaskMessage taskMessage = new TaskMessage(taskId, message);
                LOG.info("[Client] load metrics response2");
                try {
                    if (taskMessage.task() != -1) {
                        throw new RuntimeException("Metrics messages are sent to the system task (" + client.getDstAddress() + ") " + taskMessage);
                    }
                    LOG.info("[Client] load metrics response3");
                    List metrics = _des.deserialize(taskMessage.message());
                    LOG.info("[Client] load metrics response4");
                    if (metrics.size() < 1) {
                        throw new RuntimeException("No metrics data in the metrics message (" + client.getDstAddress() + ") " + metrics);
                    }
                    if (!(metrics.get(0) instanceof Map)) {
                        throw new RuntimeException("The metrics did not have a map in the first slot (" + client.getDstAddress() + ") " + metrics);
                    }
                    LOG.info("[Client] load metrics response5");
                    client.setServerLoad((Map<Integer, Double>) metrics.get(0));
                    LOG.info("[Client] load metrics response6");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                LOG.info("Metrics message received7");
                msg.returnToParentPool();
                return;
            } else {
                byte message = msg.getIn().get();
                if ((char) message == 's') {
                    LOG.info("[Client-onResponse] success");
                    msg.returnToParentPool();
                    return;
                }
            }
        }

        @Override
        public void onSessionEstablished() {
            boolean setSession = sessionRef.compareAndSet(null, cs);
            checkState(setSession);
//            eqhThread.submit(eqh);
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
                    releaseResources();
                } else {
//                    eqhThread.shutdown();
                    LOG.info("closing false do reconnect no delay");
                    scheduleConnect(NO_DELAY_MS);
                }
            } else {
                LOG.error(str);
                LOG.info("Re connect to {}:{}", uri.getHost(), uri.getPort());
//                eqh.breakEventLoop();
                long nextDelayMs = retryPolicy.getSleepTimeMs(connectionAttempts.get(), 0);
                LOG.info("nextDelayMS = {}", nextDelayMs);
                scheduleConnect(nextDelayMs);
            }
        }

        @Override
        public void onMsgError(Msg msg, EventReason eventReason) {
            LOG.error("MSG error reason is={} to {}", eventReason, uri.toString());
            LOG.error("MSG = {}", msg.toString());
            msg.returnToParentPool();
        }
    }
}