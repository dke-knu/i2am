package org.apache.storm.messaging.jxio;

import org.accelio.jxio.*;
import org.accelio.jxio.exceptions.JxioGeneralException;
import org.accelio.jxio.exceptions.JxioSessionClosedException;
import org.accelio.jxio.WorkerCache.Worker;
import org.accelio.jxio.WorkerCache.WorkerProvider;
import org.apache.storm.Config;
import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.metric.api.IStatefulObject;
import org.apache.storm.serialization.KryoValuesSerializer;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by seokwoo on 17. 3. 17.
 */
public class Server extends ConnectionWithStatus implements IStatefulObject, WorkerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class.getCanonicalName());

    @SuppressWarnings("rawtypes")
    private Map storm_conf;
    private int port;
    private String host;
    private final ConcurrentHashMap<String, AtomicInteger> messagesEnqueued = new ConcurrentHashMap<>();
    private final AtomicInteger messagesDequeued = new AtomicInteger(0);

    private volatile boolean closing = false;
    private KryoValuesSerializer _ser;
    private IConnectionCallback _cb = null;

    //JXIO's
    private int num_of_workers;
    private final ServerPortalCallbacks spc;
    private final EventQueueHandler listen_eqh;
    private final ServerPortal listener;
    private static PriorityQueue<ServerPortalHandler> SPWorkers;
    private final MsgPool msgPool;
    private ServerSession session;
    private volatile Set<ServerSession> allSessions;
    private ExecutorService executor;

//    private volatile boolean loadMetricsFlag = false;

    private volatile Map<Integer, Double> taskToLoad;

    /*
    *서버 객체를 생성하면 bind
    * */
    @SuppressWarnings("rawtypes")
    public Server(Map storm_conf, int port) {
        this.storm_conf = storm_conf;
        this.port = port;
        _ser = new KryoValuesSerializer(storm_conf);

        if (storm_conf.containsKey(Config.STORM_LOCAL_HOSTNAME)) {
            host = (String) storm_conf.get(Config.STORM_LOCAL_HOSTNAME);
            LOG.info("host IP = {}", host);
        } else {
            host = getLocalServerIp();
            LOG.info("No Configuration associate host,get local host -> {}", host);
        }

        URI uri = null;
        try {
            uri = new URI(String.format("rdma://%s:%s", host, String.valueOf(port)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        num_of_workers = Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_WORKER_THREADS));

        listen_eqh = new EventQueueHandler(new EqhCallbacks(
                Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_INC_BUFFER_COUNT)),
                Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_BUFFER_SIZE)), 200));

        msgPool = new MsgPool(
                Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_INITIAL_BUFFER_COUNT)),
                Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_BUFFER_SIZE)), 200);

        listen_eqh.bindMsgPool(msgPool);
        spc = new ServerPortalCallbacks(this);
        listener = new ServerPortal(listen_eqh, uri, spc);
        allSessions = new HashSet<ServerSession>();

        SPWorkers = new PriorityQueue<ServerPortalHandler>(num_of_workers);
        for (int i = 1; i <= num_of_workers; i++) {
            SPWorkers.add(new ServerPortalHandler(i, listener.getUriForServer(), spc));
        }

        LOG.info("Create JXIO Server " + jxio_name() + ", worker threads: " + num_of_workers);

        runServer();
    }

    private void runServer() {
        LOG.info("Starting server portal workers");
        ThreadFactory factory = new JxioRenameThreadFactory(jxio_name() + "-worker");
        executor = Executors.newCachedThreadPool(factory);

        for(ServerPortalHandler sph : SPWorkers) {
            executor.submit(sph);
        }

        new JxioRenameThreadFactory(jxio_name() + "-EQH").newThread(listen_eqh).start();
        LOG.info("EQH start!!");
    }

    private void addReceiveCount(String from, int amount) {
        //This is possibly lossy in the case where a value is deleted
        // because it has received no messages over the metrics collection
        // period and new messages are starting to come in.  This is
        // because I don't want the overhead of a synchronize just to have
        // the metric be absolutely perfect.
        AtomicInteger i = messagesEnqueued.get(from);
        if (i == null) {
            i = new AtomicInteger(amount);
            AtomicInteger prev = messagesEnqueued.putIfAbsent(from, i);
            if (prev != null) {
                prev.addAndGet(amount);
            }
        } else {
            i.addAndGet(amount);
        }
    }

    /**
     * enqueue a received message
     *
     * @throws InterruptedException
     */
    protected void enqueue(List<TaskMessage> msgs, String from) throws InterruptedException {
        if (null == msgs || msgs.size() == 0 || closing) {
            return;
        }
        addReceiveCount(from, msgs.size());
        if (_cb != null) {
            _cb.recv(msgs);
        }
    }

    @Override
    public void registerRecv(IConnectionCallback cb) {
        _cb = cb;
    }

    @Override
    public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {
        LOG.info("[server] set taskToLoad");
        this.taskToLoad = taskToLoad;
    }

    @Override
    public void send(int taskId, byte[] payload) {
        throw new UnsupportedOperationException("Server connection should not send any messages");
    }

    @Override
    public void send(Iterator<TaskMessage> msgs) {
        throw new UnsupportedOperationException("Server connection should not send any messages");
    }

    @Override
    public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
        throw new RuntimeException("Server connection cannot get load");
    }

    @Override
    public synchronized void close() {
        listen_eqh.releaseMsgPool(msgPool);
        msgPool.deleteMsgPool();
        listen_eqh.stop();
        if (allSessions != null) {
            for (ServerSession ss : allSessions) {
                ss.close();
            }
            allSessions = null;
        }
    }

    @Override
    public Status status() {
        if (closing) {
            return Status.Closed;
        } else if (!connectionEstablished(allSessions)) {
            return Status.Connecting;
        } else {
            return Status.Ready;
        }
    }

    private boolean connectionEstablished(Set<ServerSession> allSessions) {
        boolean allEstablished = true;
        for (ServerSession ss : allSessions) {
            if (!(connectionEstablished(ss))) {
                allEstablished = false;
                break;
            }
        }
        return allEstablished;
    }

    private boolean connectionEstablished(ServerSession ss) {
        return ss != null && (listen_eqh.getInRunEventLoop() == true);
    }

    @Override
    public Object getState() {
        LOG.debug("Getting metrics for server on port {}", port);
        LOG.info("Getting metrics for server on port {}", port);
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("dequeuedMessages", messagesDequeued.getAndSet(0));
        HashMap<String, Integer> enqueued = new HashMap<String, Integer>();
        Iterator<Map.Entry<String, AtomicInteger>> it = messagesEnqueued.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, AtomicInteger> ent = it.next();
            //Yes we can delete something that is not 0 because of races, but that is OK for metrics
            AtomicInteger i = ent.getValue();
            if (i.get() == 0) {
                it.remove();
            } else {
                enqueued.put(ent.getKey(), i.getAndSet(0));
            }
        }
        ret.put("enqueued", enqueued);
        return ret;
    }

    public void received(Object message, String remote) throws InterruptedException {
        List<TaskMessage> msgs = (List<TaskMessage>) message;
        enqueue(msgs, remote);
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

    public String jxio_name() {
        return "JXIO-server-localhost-" + port;
    }

    public String toString() {
        return String.format("JXIO server listening on port %s", port);
    }

    @Override
    public WorkerCache.Worker getWorker() {
        return null;
    }

    private synchronized static ServerPortalHandler getNextWorker() {
        // retrieve next spw and update its position in the queue
        ServerPortalHandler s = SPWorkers.poll();
        s.incrNumOfSessions();
        SPWorkers.add(s);
        return s;
    }

    public synchronized static void updateWorkers(ServerPortalHandler s) {
        // remove & add the ServerPortalWorker in order.
        SPWorkers.remove(s);
        SPWorkers.add(s);
    }

    public class ServerPortalCallbacks implements ServerPortal.Callbacks {
        private Server server;

        public ServerPortalCallbacks(Server server) {
            this.server = server;
        }

        @Override
        public void onSessionNew(ServerSession.SessionKey sesKey, String srcIP, WorkerCache.Worker workerHint) {
            if (closing) {
                LOG.info("[Server]onSessionNew, CONNECT_ERROR, Rejecting session");
                listener.reject(sesKey, EventReason.CONNECT_ERROR, "Server is closed");
                return;
            }

            ServerPortalHandler sph;
            if (workerHint == null) {
                listener.reject(sesKey, EventReason.CONNECT_ERROR, "No available worker was found in cache");
                return;
            } else {
                sph = (ServerPortalHandler) workerHint;
            }

            LOG.info("[Server][SUCCESS] Got event onSessionNew from " + srcIP + ", URI='" + sesKey.getUri() + "'");
            /*session = new ServerSession(sesKey, new ServerSessionCallbacks(server));
            listener.accept(session);
            allSessions.add(session);*/


            LOG.info("Server worker number {} got new session from {}", sph.portalIndex, srcIP);
            listener.forward(sph.getPortal(), (new ServerSessionHandler(sesKey, sph, server)).getSession());
            LOG.info("forward this session from {}", srcIP);
        }

        @Override
        public void onSessionEvent(EventName eventName, EventReason eventReason) {
            LOG.info("[Server]onSessionEvent, GOT EVENT {} because of {}", eventName.toString(), eventReason.toString());
        }
    }

    public class ServerSessionCallbacks implements ServerSession.Callbacks {
        private Server server;
        private List<TaskMessage> messages = new ArrayList<TaskMessage>();
        private AtomicInteger failure_count;

        public ServerSessionCallbacks(Server server) {
            this.server = server;
            failure_count = new AtomicInteger(0);
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

        @Override
        public void onRequest(Msg msg) {

            if (!msg.getIn().hasRemaining()) {
                LOG.error("[Server-onRequest] msg.getIn is null, no messages in msg");
                return;
            }

            Object obj = decoder(msg.getIn());
            if (obj instanceof ControlMessage) {
                LOG.info("[Server] onRequest2");
                ControlMessage ctrl_msg = (ControlMessage) obj;
                LOG.info("[Server] onRequest3");
                if (ctrl_msg == ControlMessage.LOADMETRICS_REQUEST) {
                    LOG.info("[Server] onRequest4");
                    try {
                        LOG.info("[Server] onRequest5");
                        TaskMessage tm = new TaskMessage(-1, _ser.serialize(Arrays.asList((Object) taskToLoad)));
                        LOG.info("[Server] onRequest6");
                        msg.getOut().put(tm.serialize());
                        LOG.info("[Server] onRequest7");
                        session.sendResponse(msg);
                        LOG.info("[Server] onRequest8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                msg.getIn().rewind();
                ByteBuffer bb = msg.getIn();
                byte[] ipByte = new byte[13];
                bb.get(ipByte);
                String ipStr = new String(ipByte);
                LOG.info("[Server] normal messages from {}", ipStr);

                //single TaskMessage
            /*int taskId = bb.getShort();
            byte[] tempByte = new byte[bb.limit()-15];
            bb.get(tempByte);
            TaskMessage tm = new TaskMessage(taskId, tempByte);
            messages.add(tm);*/

                //batch TaskMessage
                List<TaskMessage> messages = (ArrayList<TaskMessage>) decoder(msg.getIn());

                try {
                    server.received(messages, ipStr);
                } catch (InterruptedException e) {
                    LOG.info("failed to enqueue a request message", e);
                    failure_count.incrementAndGet();
                    e.printStackTrace();
                }
                char ch = 's';
                msg.getOut().put((byte) ch);
                try {
                    session.sendResponse(msg);
                } catch (JxioGeneralException e) {
                    e.printStackTrace();
                } catch (JxioSessionClosedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSessionEvent(EventName eventName, EventReason eventReason) {
            String str = "[ServerSession][EVENT] Got event " + eventName + " because of " + eventReason;
            if (eventName == EventName.SESSION_CLOSED) { // normal exit
                LOG.info(str);
            } else {
                LOG.error(str);
            }
        }

        @Override
        public boolean onMsgError(Msg msg, EventReason eventReason) {
            LOG.error("[ServerSession][ERROR] onMsgErrorCallback. reason=" + eventReason);
            return true;
        }
    }

    class EqhCallbacks implements EventQueueHandler.Callbacks {
        private final int numMsgs;
        private final int inMsgSize;
        private final int outMsgSize;

        public EqhCallbacks(int msgs, int in, int out) {
            numMsgs = msgs;
            inMsgSize = in;
            outMsgSize = out;
        }

        public MsgPool getAdditionalMsgPool(int in, int out) {
            MsgPool mp = new MsgPool(numMsgs, inMsgSize, outMsgSize);
            LOG.warn("new MsgPool: " + mp);
            return mp;
        }
    }
}
