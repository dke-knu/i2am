package org.apache.storm.messaging.jxio;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.messaging.org.accelio.jxio.ClientSession;
import org.apache.storm.messaging.org.accelio.jxio.EventName;
import org.apache.storm.messaging.org.accelio.jxio.EventQueueHandler;
import org.apache.storm.messaging.org.accelio.jxio.EventReason;
import org.apache.storm.messaging.org.accelio.jxio.Msg;
import org.apache.storm.messaging.org.accelio.jxio.MsgPool;
import org.apache.storm.metric.api.IStatefulObject;



public class Client extends ConnectionWithStatus implements IStatefulObject {

	private EventQueueHandler eqh;
	private ClientSession cs;
	private MsgPool msgPool;
	private EventName connectErrorType = null;
	private boolean close = false;
	private boolean established = false;
	private Msg msg = null;

    public Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
       
    	cs = null;
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
			
		}

		@Override
		public void onMsgError(Msg msg, EventReason reason) {
			// TODO Auto-generated method stub
			
		}
    	
    }

	@Override
	public void registerRecv(IConnectionCallback cb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(int taskId, byte[] payload) {
		// TODO Auto-generated method stub
		
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
	
}