package org.apache.storm.messaging.jxio;

import org.apache.storm.Config;
import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.messaging.org.accelio.jxio.jxioConnection.JxioConnection;
import org.apache.storm.metric.api.IStatefulObject;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Client extends ConnectionWithStatus implements IStatefulObject {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private JxioConnection jxClient;
    private OutputStream output;
    private URI uri;
    private Object writeLock = new Object();
    private InputStream input;
    private ScheduledThreadPoolExecutor scheduler;
    private Map stormConf;
    private HashMap<String, Integer> jxioConfigs = new HashMap<>();
    protected String dstAddressPrefixedName;
    private static final String PREFIX = "JXIO-Client-";
    private static final Timer timer = new Timer("JXIO-SessionAlive-Timer", true);

    private volatile boolean closing = false;

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

    Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
        this.stormConf = stormConf;
        closing = false;
        this.scheduler = scheduler;

        jxioConfigs.put("msgpool", Utils.getInt(stormConf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE)));
        jxioConfigs.put("is_msgpool_count", Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_INPUT_BUFFER_COUNT)));
        jxioConfigs.put("os_msgpool_count", Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_OUTPUT_BUFFER_COUNT)));

        try {
            uri = new URI(String.format("rdma://%s:%s", host, port));
            dstAddressPrefixedName = prefixedName(uri);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

			jxClient = new JxioConnection(uri, jxioConfigs);
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


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
//                    getConnectedChannel();
                } catch (Exception exp) {
                    LOG.error("channel connection error {}", exp);
                }
            }
        }, 0, SESSION_ALIVE_INTERVAL_MS);
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


    private boolean reconnectingAllowed() {
        return !closing;
    }
    
    private class Connect implements Runnable{
    	
    	public Connect(){
    		
    	}
    	
    	private void reschedule(){
    		
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(reconnectingAllowed()){
				try {
					output = jxClient.getOutputStream();
					input = jxClient.getInputStream();
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
			}
			else{
				close();
				return;
			}
		}
    }	
    	

    public Object getState() {
        LOG.debug("Getting metrics for client connection to {}", dstAddressPrefixedName);
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("reconnects", totalConnectionAttempts.getAndSet(0));
        ret.put("sent", messagesSent.getAndSet(0));
        ret.put("pending", pendingMessages.get());
        ret.put("lostOnSend", messagesLost.getAndSet(0));
        ret.put("dest", uriToString(uri));
        String src = srcAddressName();
        if (src != null) {
            ret.put("src", src);
        }
        return ret;
    }

    //need to get local address from uri...
    private String srcAddressName() {
        String name = null;
        try {
            Socket tempSocket = new Socket(uri.getHost(), uri.getPort());
            if(tempSocket != null) name = tempSocket.getLocalAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    private String uriToString(URI uri) {
        return (uri.getHost() + ":" + uri.getPort());
    }

    }
