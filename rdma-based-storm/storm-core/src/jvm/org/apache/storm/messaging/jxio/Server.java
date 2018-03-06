
package org.apache.storm.messaging.jxio;

import org.accelio.jxio.*;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
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
    public KryoValuesSerializer _ser;
    private IConnectionCallback _cb = null;

    //JXIO's
    private int num_of_workers;
    private final ServerPortalCallbacks spc;
    private final EventQueueHandler listen_eqh;
    private final ServerPortal listener;
    private static ConcurrentLinkedQueue<ServerPortalHandler> SPWorkers;
    private ExecutorService executor;

//    public volatile Map<Integer, Double> taskToLoad = null;
    MessageBatch mb;

    private final Map<String, Integer> jxioConfig = new HashMap<String, Integer>();

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
        listen_eqh = new EventQueueHandler(null);
        spc = new ServerPortalCallbacks(this);
        listener = new ServerPortal(listen_eqh, uri, spc, this);

        ThreadFactory factory = new JxioRenameThreadFactory(jxio_name() + "-worker");
//        executor = Executors.newFixedThreadPool(num_of_workers, factory);
        SPWorkers = new ConcurrentLinkedQueue<ServerPortalHandler>();
        jxioConfig.put("poolSize", Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_INITIAL_BUFFER_COUNT)));
        jxioConfig.put("in", Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_BUFFER_SIZE)));
        jxioConfig.put("out", Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_MSGPOOL_MINIMUM_BUFFER_SIZE)));
        for (int i = 1; i <= num_of_workers; i++) {
            SPWorkers.add(new ServerPortalHandler(i, listener.getUriForServer(), spc, jxioConfig));
        }

        LOG.info("Create JXIO Server " + jxio_name() + ", worker threads: " + num_of_workers);

        runServer();
    }

    private void runServer() {
        LOG.info("Starting server portal workers");

        for (ServerPortalHandler sph : SPWorkers) {
//            executor.submit(sph);
            sph.start();
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
//        LOG.info("[server] set taskToLoad");
        mb = new MessageBatch(1);
        try {
            mb.add(new TaskMessage(-1, _ser.serialize(Arrays.asList((Object) taskToLoad))));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (closing) return;

        closing = true;
        for (Iterator<ServerPortalHandler> it = SPWorkers.iterator(); it.hasNext(); ) {
            it.next().disconnect();
        }
        listen_eqh.stop();
        listen_eqh.close();
        LOG.info(jxio_name() + " shutting down");
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
        for (ServerPortalHandler sph : workers) {
            if (!(connectionEstablished(sph))) {
                allEstablished = false;
                break;
            }
        }
        return allEstablished;
    }

    private boolean connectionEstablished(ServerPortalHandler sph) {
        for (ServerSessionHandler ssh : sph.getHandler())
            if (ssh.getSession() != null && listen_eqh.getInRunEventLoop()) {
                if (sph.getEqh().getInRunEventLoop()) {
                    return true;
                }
            }
        return false;
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
        for (WorkerCache.Worker w : SPWorkers) {
            if (w.isFree()) {
                return w;
            }
        }
        return createNewWorker();
    }

    private ServerPortalHandler createNewWorker() {
        num_of_workers++;
        ServerPortalHandler sph = new ServerPortalHandler(num_of_workers, listener.getUriForServer(), spc, jxioConfig);
        sph.start();
        LOG.info(jxio_name() + " Create new worker");
        return sph;
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

            SPWorkers.remove(sph);
            SPWorkers.add(sph);
            ServerSessionHandler sessionHandler = new ServerSessionHandler(sesKey, server, srcIP);
            sph.setSessionHandler(sessionHandler);
            LOG.info("Server worker number {} got new session from {}", sph.portal_index, srcIP);
            listener.forward(sph.getPortal(), sessionHandler.getSession());
            LOG.info("forward this session from {}", srcIP);
        }

        @Override
        public void onSessionEvent(EventName eventName, EventReason eventReason) {
            LOG.info("[Server]onSessionEvent, GOT EVENT {} because of {}", eventName.toString(), eventReason.toString());
        }
    }
}

