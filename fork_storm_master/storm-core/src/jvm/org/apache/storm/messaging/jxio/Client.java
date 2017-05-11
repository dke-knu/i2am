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
    ScheduledThreadPoolExecutor scheduler;

    protected final String dstAddressPrefixedName;
    private static final String PREFIX = "JXIO-Client-";
    private final InetSocketAddress dstAddress;

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


    public Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {

        this.stormConf = stormConf;

        try {
            uri = new URI(String.format("rdma://%s:%s", host, port));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.scheduler = scheduler;
        eqh = new EventQueueHandler(null);
        msgPool = new MsgPool(Utils.getInt(stormConf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE)),
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_INPUT_BUFFER_COUNT)),
                Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_OUTPUT_BUFFER_COUNT)));

        dstAddress = new InetSocketAddress(host, port);
        dstAddressPrefixedName = prefixedName(dstAddress);
        LOG.info("creating JXIO Client, connecting to {}:{}", host, port);
        connect();
        scheduler.schedule(eqh, 0, TimeUnit.MILLISECONDS);
        LOG.info("Success!");
    }

    private void connect() {
        cs = new ClientSession(eqh, uri, new ClientCallbacks());
        eqh.run();

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
            established.set(true);
        }

        @Override
        public void onSessionEvent(EventName event, EventReason reason) {
            // TODO Auto-generated method stub
            if (event == EventName.SESSION_CLOSED || event == EventName.SESSION_ERROR
                    || event == EventName.SESSION_REJECT) {
                LOG.error("Got a event: {}, reason: {}", event, reason);
                if (!close.get()) {
                    cs.close();
                    LOG.info("Do reconnecting");
                    connect();
                    return;
                }
                eqh.stop();
            }
        }

        @Override
        public void onMsgError(Msg msg, EventReason reason) {
            // TODO Auto-generated method stub
            //pending??
            LOG.error("Got a MsgError, reason: {}", reason);
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
            return;
        }
        if (!hasMessages(msgs)) {
            return;
        }
        if (cs == null) {
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

    @Override
    public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
        Map<Integer, Double> loadCache = serverLoad;
        Map<Integer, Load> ret = new HashMap<Integer, Load>();
        if (loadCache != null) {
            double clientLoad = Math.min(pendingMessages.get(), 1024)/1024.0;
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
        else if (!established.get()) return Status.Connecting;
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