package org.apache.storm.messaging.jxio;

import org.accelio.jxio.*;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by seokwoo on 17. 3. 17.
 */
public class Server extends ConnectionWithStatus implements IStatefulObject, WorkerCache.WorkerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    @SuppressWarnings("rawtypes")
    Map storm_conf;
    int port;
    String host;
    private final ConcurrentHashMap<String, AtomicInteger> messagesEnqueued = new ConcurrentHashMap<>();
    private final AtomicInteger messagesDequeued = new AtomicInteger(0);

    private volatile boolean closing = false;
    //    List<TaskMessage> closeMessage = Arrays.asList(new TaskMessage(-1, null));
    private KryoValuesSerializer _ser;
    private IConnectionCallback _cb = null;

    private HashMap<String, Integer> jxioConfigs = new HashMap<>();

    //    private boolean close = false;
    private EventQueueHandler listen_eqh;
    private ServerPortal listener;
    private static ConcurrentLinkedQueue<ServerPortalHandler> SPWorkers = new ConcurrentLinkedQueue<ServerPortalHandler>();
    private final PortalServerCallbacks psc;
    private int numOfWorkers;


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
            LOG.info("host IP = " + host);
        } else {
            host = getLocalServerIp();
            LOG.info("No Configuration associated host, get local host -> {}", host);
        }

        //Configure the Server.
        int msgpool_buf_size = Utils.getInt(storm_conf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE));
        //buffer size = MSGPOOL_BUF_SIZE ??
//        int backlog = Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SOCKET_BACKLOG), 500);
        int initWorkers = Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_WORKER_THREADS));
        numOfWorkers = initWorkers;

        jxioConfigs.put("msgpool", msgpool_buf_size);
        jxioConfigs.put("inc_buf_count", Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_INC_BUFFER_COUNT)));
        jxioConfigs.put("initial_buf_count", Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_INITIAL_BUFFER_COUNT)));

        String uriString = String.format("rdma://%s:%s", host, String.valueOf(port));
        URI uri = null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        psc = new PortalServerCallbacks();
        listen_eqh = new EventQueueHandler(null);
        listener = new ServerPortal(listen_eqh, uri, psc, this);

        for (int i = 1; i <= numOfWorkers; i++) {
            SPWorkers.add(new ServerPortalHandler(i, listener.getUriForServer(), psc, jxioConfigs, this));
        }

        LOG.info("Create JXIO Server " + jxio_name() + ", buffer_size: " + msgpool_buf_size + ", maxWorkers: " + initWorkers);

        runServer();

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
    public void close() {
        if (closing)
            return;

        closing = true;
        for (Iterator<ServerPortalHandler> it = SPWorkers.iterator(); it.hasNext(); ) {
            it.next().disconnect();
        }
        listen_eqh.stop();
    }

    @Override
    public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {
        for (ServerPortalHandler worker : SPWorkers) {
            try {
                Msg msg = worker.getMsg();
                if (msg != null) {
                    TaskMessage tm = new TaskMessage(-1, _ser.serialize(Arrays.asList((Object) taskToLoad)));
                    msg.getOut().put(tm.serialize());
                    worker.getSession().sendResponse(msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * Server가 Load를 보내고 Client가 읽어 exception을 출력한다.
    * */
    @Override
    public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
        throw new RuntimeException("Server connection cannot get load");
    }

    /*
    * 서버는 Send 함수를 호출하면 안된다.
    * Client로 send 하는 유일한 메시지는 Load Metrics
    * */
    @Override
    public void send(int taskId, byte[] payload) {
        throw new UnsupportedOperationException("Server connection should not send any messages");
    }

    /*
    * 서버는 Send 함수를 호출하면 안된다.
    * Client로 send 하는 유일한 메시지는 Load Metrics
    * */
    @Override
    public void send(Iterator<TaskMessage> msgs) {
        throw new UnsupportedOperationException("Server connection should not send any messages");
    }

    @Override
    public Status status() {
        if (closing) {
            return Status.Closed;
        } else if (!connectionEstablished(SPWorkers)) {
            return Status.Connecting;
        } else {
            return Status.Ready;
        }
    }

    private boolean connectionEstablished(ConcurrentLinkedQueue<ServerPortalHandler> workers) {
        boolean allEstablished = true;
        for (ServerPortalHandler h : workers) {
            if (!connectionEstablished(h)) {
                allEstablished = false;
                break;
            }
        }
        return allEstablished;
    }

    private boolean connectionEstablished(ServerPortalHandler h) {
        return h != null && h.isSessionAlive();
    }

    public void received(Object message, String remote) throws InterruptedException {
        List<TaskMessage> msgs = (List<TaskMessage>) message;
        enqueue(msgs, remote);
    }

    @Override
    public Object getState() {
        LOG.debug("Getting metrics for server on port {}", port);
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

    /**
     * Thread entry point when running as a new thread
     */
    public void runServer() {
        LOG.info("invoke run");
        for (ServerPortalHandler worker : SPWorkers) {
            LOG.info("handler worker start!");
            worker.start();
            LOG.info("handler worker started!");
        }
        Thread task = new Thread(() -> {
            LOG.info("listen_eqh run");
            listen_eqh.run();
            LOG.info("listen_eqh done");
        });
        LOG.info("start thread");
        task.start();
        LOG.info("end??");

    }

    @Override
    public WorkerCache.Worker getWorker() {
        for (WorkerCache.Worker w : SPWorkers) {
            if (w.isFree()) {
                return w;
            }
        }
        return createNewWorker();
    }

    private ServerPortalHandler createNewWorker() {
        LOG.info(this.toString() + "Creating new worker");
        numOfWorkers++;
        ServerPortalHandler sph = new ServerPortalHandler(numOfWorkers, listener.getUriForServer(), psc, jxioConfigs, this);
        sph.start();
        return sph;
    }

    /**
     * Callbacks for the listener server portal
     */
    public class PortalServerCallbacks implements ServerPortal.Callbacks {

        public void onSessionEvent(EventName event, EventReason reason) {
            LOG.info(Server.this.toString() + " GOT EVENT " + event.toString() + "because of " + reason.toString());
        }

        public void onSessionNew(ServerSession.SessionKey sesKey, String srcIP, WorkerCache.Worker workerHint) {
            if (closing) {
                LOG.info(this.toString() + "Rejecting session");
                listener.reject(sesKey, EventReason.CONNECT_ERROR, "Server is closed");
                return;
            }
            ServerPortalHandler sph;
            if (workerHint == null) {
                listener.reject(sesKey, EventReason.CONNECT_ERROR, "No availabe worker was found in cache");
                return;
            } else {
                sph = (ServerPortalHandler) workerHint;
                sph.setRemoteIp(srcIP);
            }
            // add last -> why remove??
            SPWorkers.remove(sph);
            SPWorkers.add(sph);
            LOG.info(Server.this.toString() + " Server worker number " + sph.portalIndex
                    + " got new session");
            ServerSession ss = new ServerSession(sesKey, sph.getSessionCallbacks());
            listener.forward(sph.getPortal(), ss);

        }
    }

    public String jxio_name() {
        return "JXIO-server-" + host + "-" + port;
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

    public String name() {
        return (String) storm_conf.get(Config.TOPOLOGY_NAME);
    }

    @Override
    public String toString() {
        return String.format("JXIO server listening on port %s", port);
    }
}
