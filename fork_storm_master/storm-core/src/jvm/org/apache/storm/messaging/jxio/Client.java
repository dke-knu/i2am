package org.apache.storm.messaging.jxio;

import org.accelio.jxio.*;
import org.accelio.jxio.jxioConnection.impl.JxioResourceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.storm.Config;
import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.metric.api.IStatefulObject;
import org.apache.storm.utils.Utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;



public class Client extends ConnectionWithStatus implements IStatefulObject, Callable<Integer> {


	private EventQueueHandler eqh;
	private ClientSession cs;
	private MsgPool msgPool;
	private boolean close = false;
	private boolean established = false;
	private Map stormConf;
	private URI uri;
	
	private volatile Map<Integer, Double> serverLoad = null;

    public Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {

    	this.stormConf = stormConf;
    	
    	try {
			uri = new URI(String.format("rdma://%s:%s", host, port));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	eqh = new EventQueueHandler(null);
		msgPool = new MsgPool(Utils.getInt(stormConf.get(Config.STORM_MEESAGING_JXIO_MSGPOOL_BUFFER_SIZE)), 
				Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_INPUT_BUFFER_COUNT)), 
				Utils.getInt(stormConf.get(Config.STORM_MESSAGING_JXIO_CLIENT_OUTPUT_BUFFER_COUNT)));
    	
    }
    private void connect(URI uri){
    	cs = new ClientSession(eqh, uri, new ClientCallbacks() );
    	eqh.runEventLoop(1, -1);
    }
    
    public class ClientCallbacks implements ClientSession.Callbacks {

		@Override
		public void onResponse(Msg msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSessionEstablished() {
			// TODO Auto-generated method stub
			established = true;
		}

		@Override
		public void onSessionEvent(EventName event, EventReason reason) {
			// TODO Auto-generated method stub
			if(event == EventName.SESSION_CLOSED || event == EventName.SESSION_ERROR 
					|| event == EventName.SESSION_REJECT){
				
			}
		}

		@Override
		public void onMsgError(Msg msg, EventReason reason) {
			// TODO Auto-generated method stub
			
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
		
	}

	@Override
	public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status status() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}