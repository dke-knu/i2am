package org.dynamic.anomaly.detection.storm;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

public class ADWindowedBolt extends BaseWindowedBolt {
	private OutputCollector collector;

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("log", "mvAvg", "mvStd"));
		
	}

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(TupleWindow inputWindow) {
		double input = 0;
		int windowSize = 0;
		double sum = 0;
		double sqr_sum = 0;
		
		for(Tuple tuple: inputWindow.get()){
			windowSize += 1;
			input = tuple.getDouble(0);
			sum += input;
			sqr_sum += Math.pow(input, 2);
		}
		
		double mvAvg = sum / windowSize;
		double variance = (sqr_sum / windowSize) - Math.pow(mvAvg, 2);
		
		collector.emit(new Values(input, mvAvg, Math.sqrt(variance)));
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

}