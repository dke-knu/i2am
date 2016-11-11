package org.dynamic.anomaly.detection.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class ADGroupingBolt implements IRichBolt {
	private OutputCollector collector;
	
	private Map<String, List<Tuple>> windows;
	private final int WINDOW_SIZE = 12;
	
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.windows = new HashMap<String, List<Tuple>>();
	}

	public void execute(Tuple input) {
		String cluster = input.getStringByField("cluster");
		String host = input.getStringByField("host");
		String key = input.getStringByField("key");
		String cluster_host_key = cluster + "," + host + "," + key;
		long time = input.getLongByField("time");
		
		if (!windows.containsKey(cluster_host_key)) {
			List<Tuple> oneTuple = new ArrayList<Tuple>();
			oneTuple.add(input);
			windows.put(cluster_host_key, oneTuple);
		} else {
			List<Tuple> tuples = windows.get(cluster_host_key);
			tuples.add(input);
			if (tuples.size() > WINDOW_SIZE)
				tuples.remove(0);
			windows.put(cluster_host_key, tuples);
		}
		
		collector.emit(new Values(cluster, host, key, windows.get(cluster_host_key), time));
	}

	public void cleanup() {
		// TODO Auto-generated method stub

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("cluster", "host", "key", "window", "time"));
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
