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

public class ConcatenateMessageBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(ConcatenateMessageBolt.class);
	private OutputCollector outputCollector = null;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
		int production = tuple.getIntegerByField("production");
		String wordcount = tuple.getStringByField("wordcount");
		String createdTime = tuple.getStringByField("created_time");
		long inputTime = tuple.getLongByField("input_time");
		long outputTime = System.currentTimeMillis();
		
		outputCollector.emit(new Values(new String(wordcount + "," + production + "," + createdTime + "," + inputTime + "," + outputTime)));
		outputCollector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}
	
	@Override
    public void cleanup() {
    }    
}