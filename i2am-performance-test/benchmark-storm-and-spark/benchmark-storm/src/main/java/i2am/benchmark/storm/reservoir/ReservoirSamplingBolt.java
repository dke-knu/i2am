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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCommands;

public class ReservoirSamplingBolt extends BaseRichBolt { 
	
	/* Sampling Parameter */ 
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
		
		/* Connect Redis*/
		if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }
		
		/* Get parameters */
		parameters = jedisCommands.hgetAll(redisKey);
		sampleName = parameters.get(sampleKey);
		sampleSize = Integer.parseInt(parameters.get(sampleSizeKey)); // Get sample size
		windowSize = Integer.parseInt(parameters.get(windowSizeKey)); // Get window size
		jedisCommands.ltrim(sampleName, 0, -99999); // Remove sample list
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		
		int production = tuple.getIntegerByField("production");
		String sentence = tuple.getStringByField("sentence");
		String createdTime = tuple.getStringByField("created_time");
		long inputTime = tuple.getLongByField("input_time");
		int probability = sampleSize + 1;
		
		if(production%windowSize < sampleSize){
			jedisCommands.rpush(sampleName, new String(sentence + "," + production + "," + createdTime + "," + inputTime));
		}
		else{
			probability = (int)(Math.random()*production%windowSize);
			
			if(probability < sampleSize){
				jedisCommands.lset(sampleName, probability, new String(sentence + "," + production + "," + createdTime + "," + inputTime));
			}
		}
		
		if(production%windowSize == 0){
			List<String> sampleList = jedisCommands.lrange(sampleName, 0, -1); // Get sample list
			
			outputCollector.emit(new Values(sampleList)); // Emit
			
			jedisCommands.ltrim(sampleName, 0, -99999); // Remove sample list
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("sampleList"));
	}

}
