package i2am.benchmark.storm.wordcount;

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

public class SplitMessageBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(SplitMessageBolt.class);
	private OutputCollector outputCollector = null;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
		long inputTime = System.currentTimeMillis();
		
		String[] message = tuple.getString(0).split(",");

		if (message.length != 3) {
			logger.error("The message is not correct.");
			return;
		}
		
		int production = -1;
		try {
			production = Integer.parseInt(message[1]);
		} catch (NumberFormatException e) {
			logger.error("The idx is not correct.");
			return;
		}
		String sentence = message[0];
		String createdTime = message[2];
		outputCollector.emit(new Values(production, sentence, createdTime, inputTime));
		outputCollector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("production", "sentence", "created_time", "input_time"));
	}
	
	@Override
    public void cleanup() {
		
		 
    }    
}