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
import org.apache.storm.utils.StormBoundedExponentialBackoffRetry;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Client extends ConnectionWithStatus implements IStatefulObject {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private EventQueueHandler eqh;
    private ClientSession cs;
    private MsgPool msgPool;
    private AtomicBoolean close = new AtomicBoolean(false);
    private AtomicBoolean established = new AtomicBoolean(false);
    private Map stormConf;
    private URI uri;
    private static final long NO_DELAY_MS = 0L;
    ScheduledThreadPoolExecutor scheduler;

    protected final String dstAddressPrefixedName;
    private static final String PREFIX = "JXIO-Client-";
    private final InetSocketAddress dstAddress;
    private boolean reconn = false;
    private final StormBoundedExponentialBackoffRetry retryPolicy;

    private volatile Map<Integer, Double> serverLoad = null;

    /**
     * Total number of connection attempts.
     */
    private final AtomicInteger totalConnectionAttempts = new AtomicInteger(0);

    /**
     * Number of messages successfully sent to the remote destination.
     */
    private final AtomicInteger messagesSent = new AtomicInteger(0);

    /**
     * Number of messages buffered in memory.
     */
    private final AtomicLong pendingMessages = new AtomicLong(0);

    /**
     * Number of messages that could not be sent to the remote destination.
     */
    private final AtomicInteger messagesLost = new AtomicInteger(0);

    private final AtomicInteger connectionAttempts = new AtomicInteger(0);


    public Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {

        this.stormConf = stormConf;

        try {
            uri = new URI(String.format("rdma://%s:%s", host, port));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.scheduler = scheduler;
        eqh = new EventQueueHandler(new ClientEQHCallbacks());

        int maxReconnectionAttempts = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_NETTY_MAX_RETRIES));
        int minWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_NETTY_MIN_SLEEP_MS));
        int maxWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_NETTY_MAX_SLEEP_MS));
        msgPool = new MsgPool(Utils.getInt(stormConf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE)),
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_INPUT_BUFFER_COUNT)),
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_OUTPUT_BUFFER_COUNT)));

        retryPolicy = new StormBoundedExponentialBackoffRetry(minWaitMs, maxWaitMs, maxReconnectionAttempts);
        dstAddress = new InetSocketAddress(host, port);
        dstAddressPrefixedName = prefixedName(dstAddress);
        LOG.info(", ThreadName: " + Thread.currentThread().getName() + " creating JXIO Client, connecting to {}:{}", host, port);
        connect(NO_DELAY_MS);

    }

    private void connect(long delayMs) {
        final int connectionAttempt = connectionAttempts.getAndIncrement();
        totalConnectionAttempts.getAndIncrement();
        LOG.info("connecting to {}:{} [attempt {}]", uri.getHost(), uri.getPort(), connectionAttempt);
        cs = new ClientSession(eqh, uri, new ClientCallbacks());
        Thread task = new Thread(() -> {
            eqh.runEventLoop(1, -1);
        });
        task.setName(Thread.currentThread().getName() + "JXIO Client eqh-run thread");
        scheduler.schedule(task, delayMs, TimeUnit.SECONDS);


    }

    class ClientEQHCallbacks implements EventQueueHandler.Callbacks {

        @Override
        public MsgPool getAdditionalMsgPool(int i, int i1) {
            LOG.info("Messages in Client's message pool ran out, Aborting test");
            return null;
        }
    }

    class ClientCallbacks implements ClientSession.Callbacks {

        @Override
        public void onResponse(Msg msg) {
            // TODO Auto-generated method stub
            //StormClientHandler 참조
            msg.returnToParentPool();
        }

        @Override
        public void onSessionEstablished() {
            // TODO Auto-generated method stub
            LOG.debug("successfully connected to {}:{}, [attempt {}]", uri.getHost(), uri.getPort());
            established.set(true);
            scheduler.schedule(eqh, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onSessionEvent(EventName event, EventReason reason) {
            // TODO Auto-generated method stub
            if (event == EventName.SESSION_CLOSED || event == EventName.SESSION_ERROR
                    || event == EventName.SESSION_REJECT) {
                LOG.error("Got a event: {}, reason: {}", event, reason);
                if (!close.get()) {
                    cs.close();
                    reconn = true;
                    LOG.info("Do reconnecting to {}:{} threadName {}", uri.getHost(), uri.getPort(), Thread.currentThread().getName());
                    long nextDelayMs = retryPolicy.getSleepTimeMs(connectionAttempts.get(), 0);
                    connect(nextDelayMs);
                    return;
                }
                eqh.stop();
            }
        }

        @Override
        public void onMsgError(Msg msg, EventReason reason) {
            // TODO Auto-generated method stub
            //pending??
            if (cs.getIsClosing()) {
                LOG.info("On Message Error while closing. Reason is=" + reason);
            } else {
                LOG.error("On Message Error. Reason is=" + reason);
            }
            msg.returnToParentPool();
        }

    }

    /**
     * Receiving messages is not supported by a client.
     *
     * @throws java.lang.UnsupportedOperationException whenever this method is being called.
     */
    @Override
    public void registerRecv(IConnectionCallback cb) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Client connection should not receive any messages");
    }

    @Override
    public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Client connection should not send load metrics");

    }

    @Override
    public void send(int taskId, byte[] payload) {
        // TODO Auto-generated method stub
        TaskMessage msg = new TaskMessage(taskId, payload);
        List<TaskMessage> wrapper = new ArrayList<TaskMessage>(1);
        wrapper.add(msg);
        send(wrapper.iterator());
    }

    @Override
    public void send(Iterator<TaskMessage> msgs) {
        // TODO Auto-generated method stub
        if (close.get()) {
            int numMessages = iteratorSize(msgs);
            LOG.error("discarding {} messages because the JXIO client to {} is being closed", numMessages,
                    dstAddressPrefixedName);
            return;
        }
        if (!hasMessages(msgs)) {
            return;
        }
        if (cs == null) {
            dropMessages(msgs);
            return;
        }
        while (msgs.hasNext()) {
            Msg msg = msgPool.getMsg();
            msg.getOut().put(msgs.next().serialize().array());

            try {
                cs.sendRequest(msg);
            } catch (JxioGeneralException | JxioSessionClosedException | JxioQueueOverflowException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                msg.returnToParentPool();
            }
        }
    }

    private boolean hasMessages(Iterator<TaskMessage> msgs) {
        return msgs != null && msgs.hasNext();
    }

    private void dropMessages(Iterator<TaskMessage> msgs) {
        // We consume the iterator by traversing and thus "emptying" it.
        int msgCount = iteratorSize(msgs);
        messagesLost.getAndAdd(msgCount);
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
    public void close() {
        // TODO Auto-generated method stub
        close.set(true);
        cs.close();
        eqh.stop();
        eqh.close();
        msgPool.deleteMsgPool();
    }

    @Override
    public Object getState() {
        LOG.debug("Getting metrics for client connection to {}", dstAddressPrefixedName);
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

    @Override
    public Status status() {
        // TODO Auto-generated method stub
        if (close.get()) return Status.Closed;
        else if (!established.get() && cs != null) return Status.Connecting;
        else return Status.Ready;

    }

    private String prefixedName(InetSocketAddress dstAddress) {
        if (null != dstAddress) {
            return PREFIX + dstAddress.toString();
        }
        return "";
    }

    @Override
    public String toString() {
        return String.format("JXIO client for connecting to %s", dstAddressPrefixedName);
    }

}