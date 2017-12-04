package i2am.Sampling;

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

import java.util.List;
import java.util.Map;

public class HashSamplingBolt extends BaseRichBolt{
    private int sampleSize;
    private int windowSize;
    private int bucketSize;
    private int randomNumber;
    private String sampleName = null;
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

    public HashSamplingBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
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
        int count = input.getIntegerByField("count");

        int hashCode = data.hashCode()%bucketSize;
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