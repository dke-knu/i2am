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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
import redis.clients.jedis.JedisCommands;

public class QueryFilteringBolt extends BaseRichBolt { 
	
	/* Parameters */
	List<String> dataArray;

	private final static Logger logger = LoggerFactory.getLogger(QueryFilteringBolt.class);
	private OutputCollector outputCollector = null;
	
	/* Jedis */

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
		int production = tuple.getIntegerByField("production");
		String sentence = tuple.getStringByField("sentence");
		String createdTime = tuple.getStringByField("created_time");
		long inputTime = tuple.getLongByField("input_time");
		
		boolean flag = false;
	
		for(String data : dataArray){
			if(sentence.contains(data)){
				flag = true;
				break;
			}
		}
		
		if(flag){
			outputCollector.emit(new Values(new String("1:" + sentence + "," + production + "," + createdTime + "," + inputTime + "," + System.currentTimeMillis())));
		}else{
			outputCollector.emit(new Values(new String("0:" + sentence + "," + production + "," + createdTime + "," + inputTime + "," + System.currentTimeMillis())));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("message"));
	}
}