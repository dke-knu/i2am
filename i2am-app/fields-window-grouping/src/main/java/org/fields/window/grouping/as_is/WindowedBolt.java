package org.fields.window.grouping.as_is;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.windowing.TupleWindow;

public class WindowedBolt extends BaseWindowedBolt {
	private OutputCollector collector;
	
	private int objectId;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.objectId = System.identityHashCode(WindowedBolt.this);
	}

	public void execute(TupleWindow inputWindow) {
		System.out.println("OBJECT ID: " + objectId);
		for(Tuple tuple: inputWindow.get()){
			System.out.print(tuple.getStringByField("sender") + " sended " + tuple.getDoubleByField("value"));
			System.out.println();
		}
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

}