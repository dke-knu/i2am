package i2am.tta.ksample;

import java.util.Map;
import java.util.Random;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KSampleBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(KSampleBolt.class);
	
	//private int interval;
	private int samplingRate;
	//private double randomNumber;
	private int count = 0;
	private String sampleElement="";

	private OutputCollector collector;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
		this.collector = collector;

		samplingRate = 10;
	}

	@Override
	public void execute(Tuple input) {
		count = increaseToLimit(count, samplingRate);

		/* KSample */
		if ( (1.0/count) > new Random().nextDouble() ) {
			if(count!=1) collector.emit(new Values(sampleElement, 0, input.getLongByField("start-time"))); // no sampleElement
			sampleElement = input.getStringByField("data");
		}else{
			collector.emit(new Values(input.getStringByField("data"), 0, input.getLongByField("start-time"))); // no sampleElement
		}

		if (count == samplingRate) {
			collector.emit(new Values(sampleElement, 1, input.getLongByField("start-time"))); // real sampleElement
		}

	}

	private int increaseToLimit(int count, int limit) {
		return ++count > limit ? 1 : count;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("data", "sampleFlag", "start-time"));
	}
}
