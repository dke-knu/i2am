
package org.dynamic.anomaly.detection.storm;

import java.util.HashMap;
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
	
	private Map<String, LogValueList> windows;
	private final int WINDOW_SIZE = 12;
	
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.windows = new HashMap<String, LogValueList>();
	}

	public void execute(Tuple input) {
		String cluster = input.getStringByField("cluster");
		String host = input.getStringByField("host");
		String key = input.getStringByField("key");
		String cluster_host_key = cluster + "," + host + "," + key;
		long time = input.getLongByField("time");
		// for performance.
		long startTime = input.getLongByField("startTime");
				
		if (!windows.containsKey(cluster_host_key)) {
			LogValueList value = new LogValueList();
			value.addObject(input.getDoubleByField("value"));
			windows.put(cluster_host_key, value);
		} else {
			LogValueList values = windows.get(cluster_host_key);
			values.addObject(input.getDoubleByField("value"));
			if (values.size() > WINDOW_SIZE)
				values.remove(0);
			windows.put(cluster_host_key, values);
		}
		
		collector.emit(new Values(cluster, host, key, windows.get(cluster_host_key), time,
				// for performance.
				startTime
				));
	}

	public void cleanup() {
		// TODO Auto-generated method stub

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("cluster", "host", "key", "window", "time", "startTime"));
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}