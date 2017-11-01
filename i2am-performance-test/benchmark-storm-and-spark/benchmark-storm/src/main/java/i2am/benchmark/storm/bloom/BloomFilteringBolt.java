package i2am.benchmark.storm.bloom;

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

public class BloomFilteringBolt extends BaseRichBolt { 
	
	/* Parameters */
	int bucketSize = 0;
	String redisKey = null;
	String bucketSizeKey = "BucketSize";
	List<String> words;
	private Map<String, String> parameters;
	
	private final static Logger logger = LoggerFactory.getLogger(BloomFilteringBolt.class);
	private OutputCollector outputCollector = null;
	
	/* Jedis */
	private transient JedisCommandsInstanceContainer jedisContainer;
	private JedisClusterConfig jedisClusterConfig;
	private JedisCommands jedisCommands = null;
	
	/* Constructor */
	public BloomFilteringBolt(List<String> words, String redisKey, JedisClusterConfig jedisClusterConfig){
		this.words = words;
		this.redisKey = redisKey;
		this.jedisClusterConfig = jedisClusterConfig;
		

		/* Connect Redis*/
		if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }
		
		/* Get parameters */
		parameters = jedisCommands.hgetAll(redisKey);
		bucketSize = Integer.parseInt(parameters.get(bucketSizeKey));
	}
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
		// TODO Auto-generated method stub
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields(""));
	}
}

class BloomFilter{
	int bucketSize;
	List<Boolean> buckets;
	HashFunction hashFunction = new HashFunction();
	
	BloomFilter(int bucketSize){
		this.bucketSize = bucketSize; 
		buckets = new ArrayList<Boolean>();
		
		for(int i = 0; i < bucketSize; i++){
			buckets.add(false);
		}
	}
	
	void registData(String data) throws UnsupportedEncodingException{
		int hashCode = 0;
		
		hashCode = hashFunction.javaHashFunction(data);
		buckets.set(hashCode%bucketSize, true);
		
		hashCode = hashFunction.xxHash32(data);
		buckets.set(hashCode%bucketSize, true);
		
		hashCode = hashFunction.JSHash(data);
		buckets.set(hashCode%bucketSize, true);
	}
	
	void filtering(String data){
		
		
	}
}

class HashFunction{
	
	HashFunction(){}
	
	int javaHashFunction(String data){
		int hashCode = data.hashCode();
		hashCode = Math.abs(hashCode);
		
		return data.hashCode();
	}
	
	int xxHash32(String data) throws UnsupportedEncodingException{
		byte[] byteData = data.getBytes("euc-kr");
		
		XXHashFactory factory = XXHashFactory.fastestInstance();
		XXHash32 hash32 = factory.hash32();
		int seed = 0x9747b28c;
		
		int hashCode = hash32.hash(byteData,  0, byteData.length, seed);
		hashCode = Math.abs(hashCode);
		
		return hashCode;
	}
	
	int JSHash(String data){
		int hashCode = 1315423911;
		
		for(int i = 0; i < data.length(); i++){
			hashCode ^= ((hashCode << 5) + data.charAt(i) + (hashCode >> 2));
		}
		
		hashCode = Math.abs(hashCode);
		
		return hashCode;
	}
}