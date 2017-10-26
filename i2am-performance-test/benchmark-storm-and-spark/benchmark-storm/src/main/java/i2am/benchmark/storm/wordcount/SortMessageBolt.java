package i2am.benchmark.storm.wordcount;

import java.util.Map;
import java.util.TreeMap;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SortMessageBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(SortMessageBolt.class);
	private OutputCollector outputCollector = null;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
		int production = tuple.getIntegerByField("production");
		Map<String, Integer> wordcount = (Map) tuple.getValueByField("wordcount");
		String createdTime = tuple.getStringByField("created_time");
		long inputTime = tuple.getLongByField("input_time");
		
		TreeMap<String, Integer> sortedWordCount = new TreeMap<String, Integer>(wordcount);
		
		outputCollector.emit(new Values(production, sortedWordCount.toString().replace(",", ":"), createdTime, inputTime));
		outputCollector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("production", "wordcount", "created_time", "input_time"));
	}
	
	@Override
    public void cleanup() {
    }    
}