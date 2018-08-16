package i2am.sampling;

import i2am.sampling.common.DbAdapter;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class HashSamplingBolt extends BaseRichBolt{
    private int sampleSize;
    private int windowSize;
    private int bucketSize;
    private int randomNumber;
    private String sampleName = null;
    private String hashFunction;
    private HashFunction hashFunctionClass;
    private String topologyName;
    private Map<String, String> allParameters;

    /* RedisKey */
    private String redisKey = null;
    private String sampleKey = "SampleKey";
    private String sampleSizeKey = "SampleSize";
    private String windowSizeKey = "WindowSize";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(HashSamplingBolt.class);

    public HashSamplingBolt(String topologyName, String redisKey, JedisClusterConfig jedisClusterConfig){
        this.topologyName = topologyName;
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        hashFunctionClass = new HashFunction();

        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

        try {
            hashFunction = DbAdapter.getInstance().getHashFunction(topologyName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

		/* Get parameters */
        allParameters = jedisCommands.hgetAll(redisKey);
        sampleName = allParameters.get(sampleKey);
        sampleSize = Integer.parseInt(allParameters.get(sampleSizeKey)); // Get sample size
        windowSize = Integer.parseInt(allParameters.get(windowSizeKey)); // Get window size
        bucketSize = windowSize/sampleSize;
        jedisCommands.ltrim(sampleName, 0, -99999); // Remove sample list
        randomNumber = (int)(Math.random()*bucketSize);
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getStringByField("data");
        String target = input.getStringByField("target");
        int count = input.getIntegerByField("count");

        int hashCode = 0;
        try {
            if(hashFunction.equals("javaHashFunction")){
                hashCode = ((int)hashFunctionClass.javaHashFunction(data))%bucketSize;
            }
            else if(hashFunction.equals("xxHash32")){
                hashCode = ((int)hashFunctionClass.xxHash32(data))%bucketSize;
            }
            else if(hashFunction.equals("jsHash")) {
                hashCode = ((int)hashFunctionClass.jsHash(data))%bucketSize;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (hashCode == randomNumber){
            jedisCommands.rpush(sampleName, data);
        }

        if(count%windowSize == 0){
            List<String> sampleList = jedisCommands.lrange(sampleName, 0, -1); // Get sample list
            jedisCommands.ltrim(sampleName, 0, -99999); // Remove sample list
            collector.emit(new Values(sampleList)); // Emit
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sampleList"));
    }
}

class HashFunction{
    HashFunction(){}

    int javaHashFunction(String data){
        int hashCode = data.hashCode();
        hashCode = Math.abs(hashCode);

        return hashCode;
    }

    int xxHash32(String data) throws UnsupportedEncodingException {
        byte[] byteData = data.getBytes("euc-kr");

        XXHashFactory factory = XXHashFactory.fastestInstance();
        XXHash32 hash32 = factory.hash32();
        int seed = 0x9747b28c;

        int hashCode = hash32.hash(byteData,  0, byteData.length, seed);
        hashCode = Math.abs(hashCode);

        return hashCode;
    }

    int jsHash(String data){
        int hashCode = 1315423911;

        for(int i = 0; i < data.length(); i++){
            hashCode ^= ((hashCode << 5) + data.charAt(i) + (hashCode >> 2));
        }

        hashCode = Math.abs(hashCode);

        return hashCode;
    }
}