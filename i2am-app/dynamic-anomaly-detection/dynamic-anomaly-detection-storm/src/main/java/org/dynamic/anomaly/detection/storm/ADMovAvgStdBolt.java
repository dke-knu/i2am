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

public class ADMovAvgStdBolt implements IRichBolt {
	private OutputCollector collector;

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("cluster", "host", "key", "value", "mvAvg", "mvStd", "time", "startTime"));
	}

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		int windowSize = 0;
		double sum = 0;
		double sqr_sum = 0;

		@SuppressWarnings("unchecked")
		List<Double> values = ((LogValueList) input.getValueByField("window")).getList();
		if (values.size() <= 0)	return ;
		
		for (double v: values) {
			windowSize += 1;
			sum += v;
			sqr_sum += Math.pow(v, 2);
		}
		
		String cluster = input.getStringByField("cluster");
		String host = input.getStringByField("host");
		String key = input.getStringByField("key");
		double currentValue = values.get(values.size()-1);
		long time = input.getLongByField("time");
		// for performance.
		long startTime = input.getLongByField("startTime");
		
		double mvAvg = sum / windowSize;
		double mvStd = Math.sqrt( (sqr_sum / windowSize) - Math.pow(mvAvg, 2) );
		mvStd = !Double.isNaN(mvStd)?mvStd:0;
		
		StringBuffer sb = new StringBuffer();
		for (double d : values) {
			sb.append(d + ",");
		}
		System.out.println("************************************");
		System.out.println("1818181818 : " + host + "," + mvAvg + "," + mvStd + "," + sb.toString());
		System.out.println("************************************");
		
		collector.emit(new Values(cluster, host, key, currentValue, 
				Math.round(mvAvg*10000)/10000.0, Math.round(mvStd*10000)/10000.0, time,
				// for performance.
				startTime
				));
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}