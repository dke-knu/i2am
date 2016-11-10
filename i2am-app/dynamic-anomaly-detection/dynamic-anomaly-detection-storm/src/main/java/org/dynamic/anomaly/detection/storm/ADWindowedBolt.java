package org.dynamic.anomaly.detection.storm;

import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class ADWindowedBolt implements IRichBolt {
	private OutputCollector collector;

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("cluster", "host", "key2", "value", "mvAvg", "mvStd", "time"));
	}

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		int windowSize = 0;
		double sum = 0;
		double sqr_sum = 0;

		@SuppressWarnings("unchecked")
		List<Tuple> tuples = (List<Tuple>) input.getValueByField("window");
		if (tuples.size() <= 0)	return ;
		
		System.out.println("###");
		for(Tuple tuple: tuples) {
			windowSize += 1;
			System.out.print(tuple.getStringByField("cluster"));
			System.out.print(" - " + tuple.getStringByField("host"));
			System.out.print(" - " + tuple.getStringByField("key"));
			System.out.println(" - " + tuple.getLongByField("time"));
			double v = tuple.getDoubleByField("value");
			sum += v;
			sqr_sum += Math.pow(v, 2);
		}
		System.out.println("@@@");
		
		Tuple lastTuple = tuples.get(tuples.size()-1);
		String cluster = lastTuple.getStringByField("cluster");
		String host = lastTuple.getStringByField("host");
		String key = lastTuple.getStringByField("key");
		double value = lastTuple.getDoubleByField("value");
		long time = lastTuple.getLongByField("time");
		
		double mvAvg = sum / windowSize;
		double variance = (sqr_sum / windowSize) - Math.pow(mvAvg, 2);
		
		collector.emit(new Values(cluster, host, key, value, mvAvg, Math.sqrt(variance), time));
		
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}