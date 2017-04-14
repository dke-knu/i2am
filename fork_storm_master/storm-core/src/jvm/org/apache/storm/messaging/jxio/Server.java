package org.apache.storm.messaging.jxio;

import org.apache.storm.Config;
import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.messaging.org.accelio.jxio.jxioConnection.JxioConnectionServer;
import org.apache.storm.messaging.org.accelio.jxio.jxioConnection.impl.MultiBufOutputStream;
import org.apache.storm.messaging.org.accelio.jxio.jxioConnection.impl.ServerWorker;
import org.apache.storm.metric.api.IStatefulObject;
import org.apache.storm.serialization.KryoValuesSerializer;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by seokwoo on 17. 3. 17.
 */
public class Server extends ConnectionWithStatus implements IStatefulObject {

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

    protected JxioConnectionServer conServer;
    private UserServerCallbacks appCallbacks;


    /*
    *서버 객체를 생성하면 bind
    * */
    @SuppressWarnings("rawtypes")
    public Server(Map storm_conf, int port) {
        this.storm_conf = storm_conf;
        this.port = port;
        _ser = new KryoValuesSerializer(storm_conf);
        host = getLocalServerIp();

        LOG.info("host IP = " + host);

        //Configure the Server.
        int buffer_size = Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_BUFFER_SIZE));
        //buffer size = MSGPOOL_BUF_SIZE ??
//        int backlog = Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SOCKET_BACKLOG), 500);
        int maxWorkers = Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_SERVER_WORKER_THREADS));

        appCallbacks = new UserServerCallbacks(this, _ser);


        try {
            String uriString = String.format("rdma://%s:%s", host, String.valueOf(port));
            URI uri = null;
            try {
                uri = new URI(uriString);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            conServer = new JxioConnectionServer(uri, maxWorkers, appCallbacks);
            conServer.start();

        } catch (Throwable t) {
            t.printStackTrace();
            t.getCause().printStackTrace();
        }
        LOG.info("Create JXIO Server " + jxio_name() + ", buffer_size: " + buffer_size + ", maxWorkers: " + maxWorkers);

    }

    public String jxio_name() {
        return "JXIO-server-localhost-" + port;
    }

    private String getLocalServerIp()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex) {}
        return null;
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
        Iterator<ServerWorker> isw = conServer.getSPWorkers().iterator();
        while (isw.hasNext()) {
            ServerWorker sw = isw.next();
            String[] data = sw.getUri().getQuery().split("stream=");
            if (data[1].split("&")[0].compareTo("input") == 0) {
                appCallbacks.setTaskToLoad(taskToLoad);
                appCallbacks.newSessionOS(sw.getUri(), new MultiBufOutputStream(sw));
            }

        }
        //            allChannels.write(mb);

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

    /*
    * Server가 Load를 보내고 Client가 읽어 exception을 출력한다.
    * */
    @Override
    public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
        throw new RuntimeException("Server connection cannot get load");
    }

    @Override
    public void close() {
        conServer.disconnect();
    }

    @Override
    public Status status() {
        if (closing) {
            return Status.Closed;
        }
        //세션중 하나라도 false를 리턴하면 Connecting을 리턴한다.
        else if (!connectionEstablished(conServer.getSPWorkers())) {
            return Status.Connecting;
        } else {
            return Status.Ready;
        }
    }

//    private boolean connectionEstablished(Channel channel) {
//        return channel != null && channel.isBound();
//    }

    private boolean connectionEstablished(ConcurrentLinkedQueue<ServerWorker> workers) {
        boolean allEstablished = true;

        for(ServerWorker worker : workers) {
            //세션이 없거나 sessionClosed 플래그가 true(closed)이면 allEstablished = false
            if(worker.getSession() == null || worker.isSessionClosed())
                allEstablished = false;
        }

        return allEstablished;
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
}
