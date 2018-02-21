package org.fields.window.grouping.to_be;

import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

public class WindowedBolt implements IRichBolt {
	private OutputCollector collector;
	
	private int objectId;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.objectId = System.identityHashCode(WindowedBolt.this);
	}

	public void execute(Tuple input) {
		System.out.println("OBJECT ID: " + objectId);
		for(Tuple tuple: (List<Tuple>) input.getValueByField("tuples")){
			System.out.print(tuple.getStringByField("sender") + " sended " + tuple.getDoubleByField("value"));
			System.out.println();
		}
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}