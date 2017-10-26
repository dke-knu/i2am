package i2am.benchmark.storm.wordcount;

import java.util.HashMap;
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

public class WordCountBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(WordCountBolt.class);
	private OutputCollector outputCollector = null;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
		int production = tuple.getIntegerByField("production");
		String sentence = tuple.getStringByField("sentence");
		String createdTime = tuple.getStringByField("created_time");
		long inputTime = tuple.getLongByField("input_time");
		
		Map<String, Integer> wordcount = new HashMap<>();
		for (String word: sentence.split(" ")) {
			if (wordcount.containsKey(word))
				wordcount.put(word, wordcount.get(word)+1);
			else
				wordcount.put(word, 1);
		}
		
		outputCollector.emit(new Values(production, wordcount, createdTime, inputTime));
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