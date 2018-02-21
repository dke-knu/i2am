package org.dynamic.anomaly.detection.storm;

import java.util.Date;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class ADParserBolt implements IRichBolt {
	private OutputCollector collector;
	
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		// for performance.
		long startTime = new Date().getTime();
		
		String logline = input.getString(0);
		String[] log = logline.split(",");
		
		if ("".equals(logline) || logline==null || log.length!=5)	return ;
		
		String cluster = log[0].trim();
		String host = log[1].trim();
		String key = log[2].trim();
		double value = Double.parseDouble(log[3].trim());
		long time = Long.parseLong(log[4].trim());
		
		collector.emit(new Values(cluster, host, key, value, time,
				// for performance.
				startTime));
		collector.ack(input);
	}

	public void cleanup() {
		// TODO Auto-generated method stub

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("cluster", "host", "key", "value", "time", "startTime"));
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
