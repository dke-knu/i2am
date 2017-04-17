package org.apache.storm.messaging.jxio;

import org.apache.storm.Config;
import org.apache.storm.messaging.org.accelio.jxio.jxioConnection.JxioConnection;
import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Client extends ConnectionWithStatus {

    private JxioConnection jxClient;
    private OutputStream output;
    private URI uri;
    private Object writeLock = new Object();
    private InputStream input;
    private ScheduledThreadPoolExecutor scheduler;
    private Map stormConf;
    private HashMap<String, Integer> jxioConfigs = new HashMap<>();

    private volatile boolean closing = false;

    Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
        this.stormConf = stormConf;
        closing = false;
        this.scheduler = scheduler;

        jxioConfigs.put("msgpool", Utils.getInt(stormConf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE)));
        jxioConfigs.put("is_msgpool_count", Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_INPUT_BUFFER_COUNT)));
        jxioConfigs.put("os_msgpool_count", Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_OUTPUT_BUFFER_COUNT)));

        try {
            uri = new URI(String.format("rdma://%s:%s", host, port));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            jxClient = new JxioConnection(uri, jxioConfigs);
            output = jxClient.getOutputStream();
            input = jxClient.getInputStream();

        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

        byte[] temp = null;

        if (closing) {
            int numMessages = iteratorSize(msgs);
            return;
        }

        if (!hasMessages(msgs)) {
            return;
        }

        if (output == null) {
            dropMessages(msgs);
            return;
        }

        synchronized (writeLock) {
            while (msgs.hasNext()) {
                try {
                    output.write(msgs.next().serialize().array());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean hasMessages(Iterator<TaskMessage> msgs) {
        return msgs != null && msgs.hasNext();
    }

    private void dropMessages(Iterator<TaskMessage> msgs) {
        // We consume the iterator by traversing and thus "emptying" it.
        int msgCount = iteratorSize(msgs);
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
        jxClient.disconnect();
        closing = true;
    }

    @Override
    public Status status() {
        // TODO Auto-generated method stub
        if (closing) {
            return Status.Closed;
        }
        return Status.Connecting;
    }
    private class Connect{

        public Connect(){

        }

        private void reschedule(){

        }

        public void run(){

        }

    }


}
