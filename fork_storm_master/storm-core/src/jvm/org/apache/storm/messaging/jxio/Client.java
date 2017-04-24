package org.apache.storm.messaging.jxio;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.metric.api.IStatefulObject;



public class Client extends ConnectionWithStatus implements IStatefulObject {
    

    Client(Map stormConf, ScheduledThreadPoolExecutor scheduler, String host, int port, Context context) {
       

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