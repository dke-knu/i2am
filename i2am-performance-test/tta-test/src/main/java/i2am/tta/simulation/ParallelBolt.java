package i2am.tta.simulation;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(ParallelBolt.class);
	private OutputCollector outputCollector = null;
	private int sleepTime;
	
	public ParallelBolt(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		logger.info("prepare");
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
		outputCollector.emit(new Values(tuple.getStringByField("tuple"), 
				tuple.getLongByField("start-time")));
		try {Thread.sleep(sleepTime);
		} catch (InterruptedException e) {}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tuple", "start-time"));
	}
	
	@Override
    public void cleanup() {
		logger.info("cleanup");
    }    
}