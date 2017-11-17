package i2am.benchmark.storm.vs.rdma;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONSpout extends BaseRichSpout {
	private final static Logger logger = LoggerFactory.getLogger(JSONSpout.class);
	private SpoutOutputCollector collector = null;
	private int interval;
	private int production;
	private Random rand;
	
	
	public JSONSpout(int interval){
		this.interval = interval;
		this.production = 0;
	}
	
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		// TODO Auto-generated method stub
		this.collector = collector;
		this.rand = new Random();
	}

	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		long startTime = System.currentTimeMillis();
		production++;
		
		JSONObject jSentence = new JSONObject();
		jSentence.put("tweet", randomTweet());
		jSentence.put("production", production);
		jSentence.put("startTime", startTime);
		
		collector.emit(new Values(jSentence.toString()));
		
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}
	
	public JSONObject randomTweet() {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(
					new FileReader("/data/tweetdata1000/twit_data" + (rand.nextInt(1000)+1) + ".json"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
}
