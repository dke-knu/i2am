package org.apache.storm.messaging.jxio;

import org.accelio.jxio.EventName;
import org.accelio.jxio.jxioConnection.JxioConnection;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkState;

public class Client extends ConnectionWithStatus implements IStatefulObject {
    private static final long PENDING_MESSAGES_FLUSH_TIMEOUT_MS = 600000L;
    private static final long PENDING_MESSAGES_FLUSH_INTERVAL_MS = 1000L;
    private static final long NO_DELAY_MS = 0L;

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private JxioConnection jxClient;
    private OutputStream output;
    private InputStream input;
    private URI uri;
    private ScheduledThreadPoolExecutor scheduler;
    private Map stormConf;
    private HashMap<String, Integer> jxioConfigs = new HashMap<>();
    protected String dstAddressPrefixedName;
    private static final String PREFIX = "JXIO-Client-";
    private static final Timer timer = new Timer("JXIO-SessionAlive-Timer", true);
    private final Context context;
    private final StormBoundedExponentialBackoffRetry retryPolicy;

    private volatile boolean closing;

    private final MessageBuffer batcher;

    /**
     * Periodically checks for connected channel in order to avoid loss
     * of messages
     */
    private final long SESSION_ALIVE_INTERVAL_MS = 30000L;

    /**
     * Number of messages that could not be sent to the remote destination.
     */
    private final AtomicInteger messagesLost = new AtomicInteger(0);

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
     * Whether the SASL channel is ready.
     */
    private final AtomicBoolean saslChannelReady = new AtomicBoolean(false);

    /**
     * Number of connection attempts since the last disconnect.
     */
    private final AtomicInteger connectionAttempts = new AtomicInteger(0);

    Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
        this.stormConf = stormConf;
        closing = false;
        this.scheduler = scheduler;
        this.context = context;
        // if SASL authentication is disabled, saslChannelReady is initialized as true; otherwise false
        saslChannelReady.set(!Utils.getBoolean(stormConf.get(Config.STORM_MESSAGING_NETTY_AUTHENTICATION), false));

        int maxReconnectionAttempts = 29; //Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_MAX_RETRIES));
        int minWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_NETTY_MIN_SLEEP_MS));
        int maxWaitMs = Utils.getInt(stormConf.get(Config.STORM_MESSAGING_NETTY_MAX_SLEEP_MS));
        retryPolicy = new StormBoundedExponentialBackoffRetry(minWaitMs, maxWaitMs, maxReconnectionAttempts);

        try {
            LOG.info("host: " + host + ", port: " + port);
            LOG.info("host address: " + InetAddress.getByName(host).getHostAddress());
            LOG.info("host address: " + InetAddress.getByName(host).getCanonicalHostName());
            LOG.info("getLocalIp: " + getLocalServerIp());

            uri = new URI(String.format("rdma://%s:%s", host, port));
            dstAddressPrefixedName = prefixedName(uri);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            jxClient = new JxioConnection(uri);
            scheduleConnect(NO_DELAY_MS);
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] fromIpBytes;
        fromIpBytes = getLocalServerIp().getBytes();
        batcher = new MessageBuffer(Constants.MSGPOOL_BUF_SIZE, fromIpBytes);
    }

    private String prefixedName(URI uri) {
        if (null != uri) {
            return PREFIX + uri.toString();
        }
        return "";
    }

    /**
     * This thread helps us to check for channel connection periodically.
     * This is performed just to know whether the destination address
     * is alive or attempts to refresh connections if not alive. This
     * solution is better than what we have now in case of a bad channel.
     */
    private void launchChannelAliveThread() {
        // netty TimerTask is already defined and hence a fully
        // qualified name
        timer.schedule(new java.util.TimerTask() {
            public void run() {
                try {
                    LOG.debug("running timer task, address {}", uri);
                    if (closing) {
                        this.cancel();
                        return;
                    }
                    getAliveSession();
                } catch (Exception exp) {
                    LOG.error("channel connection error {}", exp);
                }
            }
        }, 1000L, SESSION_ALIVE_INTERVAL_MS);
    }

    private void getAliveSession() {
        if (output != null & input != null) {
            return;
        } else {
            // Closing the channel and reconnecting should be done before handling the messages.
            boolean reconnectScheduled = closeSessionAndReconnect(output, input);
            if (reconnectScheduled) {
                // Log the connection error only once
                LOG.error("connection to {} is unavailable", uri.toString());
            }
        }
    }

    private boolean closeSessionAndReconnect(OutputStream out, InputStream in) {
        if (out != null && in != null) {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            scheduleConnect(NO_DELAY_MS);
            return true;
        }
        return false;
    }

    @Override
    public void registerRecv(IConnectionCallback cb) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Client connection should not receive any messages");
    }

    @Override
    public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Client connection should not receive any messages");

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

        if (closing) {
            int numMessages = iteratorSize(msgs);
            LOG.error("discarding {} messages because the Netty client to {} is being closed", numMessages,
                    dstAddressPrefixedName);
            return;
        }

        if (!hasMessages(msgs)) {
            return;
        }

        if (output == null) {
            dropMessages(msgs);
            return;
        }

        //        synchronized (writeLock) {
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
//        }

        //if channel.isWritable
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
        final int numMessages = batch.size();
        LOG.debug("writing {} messages to session {}", batch.size(), uri.toString());
        pendingMessages.addAndGet(numMessages);

        //need 13 bytes to store ip address ex) 192.168.1.100
        try {
            //maximum batch size is 262144B (256KB)
            //so, Msg size must be upper than 256KB
            ByteBuffer bb = batch.buffer();
            byte[] temp = new byte[bb.limit()];
            bb.flip();
            bb.get(temp);

            long sent = 0;
            int n = 0;
            while (sent < temp.length) {
                n = (int) Math.min(temp.length - sent, Constants.MSGPOOL_BUF_SIZE);
                output.write(temp, 0, n);
                sent += n;
            }
            output.flush();

        } catch (Exception e) {
            LOG.error("[Client-flushMessages] put message to bytebuffer error {}");
            e.printStackTrace();
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        if (!closing) {
            LOG.info("closing JXIO Client {}", dstAddressPrefixedName);
            context.removeClient(uri.getHost(), uri.getPort());
            //Set Closing to true to prevent any further reconnection attempts.
            closing = true;
            waitForPendingMessagesToBeSent();
            jxClient.disconnect();
        }
    }

    private void waitForPendingMessagesToBeSent() {
        LOG.info("waiting up to {} ms to send {} pending messages to {}",
                PENDING_MESSAGES_FLUSH_TIMEOUT_MS, pendingMessages.get(), dstAddressPrefixedName);
        long totalPendingMsgs = pendingMessages.get();
        long startMs = System.currentTimeMillis();
        while (pendingMessages.get() != 0) {
            try {
                long deltaMs = System.currentTimeMillis() - startMs;
                if (deltaMs > PENDING_MESSAGES_FLUSH_TIMEOUT_MS) {
                    LOG.error("failed to send all pending messages to {} within timeout, {} of {} messages were not " +
                            "sent", dstAddressPrefixedName, pendingMessages.get(), totalPendingMsgs);
                    break;
                }
                Thread.sleep(PENDING_MESSAGES_FLUSH_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    /**
     * Note:  Storm will check via this method whether a worker can be activated safely during the initial startup of a
     * topology.  The worker will only be activated once all of the its connections are ready.
     */
    @Override
    public Status status() {
        // TODO Auto-generated method stub
        if (closing) {
            return Status.Closed;
        } else if (!(jxClient != null && output != null)) {
            LOG.debug("[Client-status] Connecting " + uri.toString());
            return Status.Connecting;
        } else {
            if (saslChannelReady.get()) {
                return Status.Ready;
            } else {
                return Status.Connecting; // need to wait until sasl channel is also ready
            }
        }
    }


    private boolean reconnectingAllowed() {
        return !closing;
    }

    private void scheduleConnect(long delayMs) {
        scheduler.schedule(new Connect(this, uri), delayMs, TimeUnit.MILLISECONDS);
    }


    public Object getState() {
        LOG.debug("Getting metrics for client connection to {}", dstAddressPrefixedName);
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("reconnects", totalConnectionAttempts.getAndSet(0));
        ret.put("sent", messagesSent.getAndSet(0));
        ret.put("pending", pendingMessages.get());
        ret.put("lostOnSend", messagesLost.getAndSet(0));
        ret.put("dest", uriToString(uri));
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
        }
        return null;
    }

    private String uriToString(URI uri) {
        return (uri.getHost() + ":" + uri.getPort());
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
                try {
                    output = jxClient.getOutputStream();
                    input = jxClient.getInputStream();
                } catch (ConnectException e) {
                    e.printStackTrace();
                    long nextDelayMs = retryPolicy.getSleepTimeMs(connectionAttempts.get(), 0);
                    LOG.info("nextDelayMS = {}", nextDelayMs);
                    scheduleConnect(nextDelayMs);
                }

                LOG.info("Successfully connected to {}:{}", uri.getHost(), uri.getPort());

                if (messagesLost.get() > 0) {
                    LOG.warn("Re-connection to {} was successful but {} messages has been lost so far ", uri.toString(), messagesLost.get());
                }

            } else {
                close();
                throw new RuntimeException("Giving up to scheduleConnect to " + uri.toString() + " after " +
                        connectionAttempts + " failed attempts. " + messagesLost.get() + " messages were lost");
            }
        }
    }
}
