package i2am.benchmark.storm.reservoir;

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

import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.exceptions.JedisDataException;

public class ReservoirSamplingBolt extends BaseRichBolt { 
	
	/* Sampling Parameters */ 
	private int sampleSize;
	private int windowSize;
	private String sampleName = null; 
	private Map<String, String> parameters;
	
	/* RedisKey */
	private String redisKey = null;
	private String sampleKey = "SampleKey";
	private String sampleSizeKey = "SampleSize";
	private String windowSizeKey = "WindowSize"; 
	
	/* Jedis */
	private transient JedisCommandsInstanceContainer jedisContainer;
	private JedisClusterConfig jedisClusterConfig;
	private JedisCommands jedisCommands = null;
	
	private final static Logger logger = LoggerFactory.getLogger(ReservoirSamplingBolt.class);
	private OutputCollector outputCollector = null;
	
	/* Constructor */
	public ReservoirSamplingBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
		this.redisKey = redisKey;
		this.jedisClusterConfig = jedisClusterConfig;
	}
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
		// TODO Auto-generated method stub
		this.outputCollector = outputCollector;
		
		// Connect To Redis 
		if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }
		
		// Get Sampling Parameters
		parameters = jedisCommands.hgetAll(redisKey);
		sampleName = parameters.get(sampleKey);
		sampleSize = Integer.parseInt(parameters.get(sampleSizeKey)); // Get sample size
		windowSize = Integer.parseInt(parameters.get(windowSizeKey)); // Get window size
		jedisCommands.ltrim(sampleName, 0, -99999); // Remove sample list
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
		
		// Production
		int production = ((Number) message.get("production")).intValue();
		
		int probability = sampleSize + 1;
		int count = production%windowSize;
		
		// Reservoir Sampling
		if(count != 0 && count < sampleSize){ 
			jedisCommands.rpush(sampleName, message.toString());
		}
		// Extract Sample When Window Done
		else if(count == 0){ 
			List<String> sampleList = jedisCommands.lrange(sampleName, 0, -1); // Get sample list
			jedisCommands.ltrim(sampleName, 0, -99999); // Remove sample list
			
			for(String data : sampleList){
				
				// Get JSON From SampleList
				try {
					message = (JSONObject) parser.parse(new String(data));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				message.put("sampleFlag", "1");
				message.put("outputTime", System.currentTimeMillis());
				outputCollector.emit(new Values(message.toString())); // Emit
			}
		}
		else{
			probability = (int)(Math.random()*count);
			
			if(probability < sampleSize){
				if(probability < jedisCommands.llen(sampleName)){
					String nonSample = jedisCommands.lindex(sampleName, probability);
					jedisCommands.lset(sampleName, probability, message.toString());
					
					// Get JSON From Non Sample
					try {
						message = (JSONObject) parser.parse(new String(nonSample));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					jedisCommands.rpush(sampleName, message.toString());
				}
			}
			
			message.put("sampleFlag", "0");
			message.put("outputTime", System.currentTimeMillis());
			outputCollector.emit(new Values(message.toString())); // Emit
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("message"));
	}
}
