package i2am.benchmark.storm.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainerBuilder;
import org.apache.storm.redis.common.container.JedisCommandsInstanceContainer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
import redis.clients.jedis.JedisCommands;

public class QueryFilteringBolt extends BaseRichBolt { 
	
	/* WordList to Filter */
	List<String> dataArray;

	private final static Logger logger = LoggerFactory.getLogger(QueryFilteringBolt.class);
	private OutputCollector outputCollector = null;
	
	/* Constructor */
	public QueryFilteringBolt(List<String> dataArray){
		this.dataArray = dataArray;
	}
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
		// TODO Auto-generated method stub
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub

		// Get JSON From Tuple
		JSONParser parser = new JSONParser();
		JSONObject message = new JSONObject();
		
		try {
			message = (JSONObject) parser.parse(new String(tuple.getString(0)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Get Tweet Text
		JSONObject tweet = (JSONObject) message.get("tweet");
		String text = (String) tweet.get("text").toString();
		
		// Query Filtering
		boolean flag = false;
		for(String data : dataArray){
			if(text.contains(data)){
				flag = true;
				break;
			}
		}
		
		// Put Flag
		if(flag){
			message.put("filterFlag", "1");
		}else{
			message.put("filterFlag", "0");
		}
		
		message.put("outputTime", System.currentTimeMillis());
		outputCollector.emit(new Values(message.toString())); // Emit
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("message"));
	}
}