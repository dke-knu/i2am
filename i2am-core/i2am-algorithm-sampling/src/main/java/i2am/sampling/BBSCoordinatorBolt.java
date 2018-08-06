package i2am.sampling;

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
import redis.clients.jedis.JedisCommands;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class BBSCoordinatorBolt extends BaseRichBolt {
    private int sampleSize;
    private int windowSize;
    private String sampleName = null;
    private String preSampleName = null;
    private Map<String, String> allParameters;

    /* RedisKey */
    private String redisKey = null;
    private String roundKey = "Round";
    private String sampleKey = "SampleKey";
    private String preSampleKey = "PreSampleKey";
    private String sampleSizeKey = "SampleSize";
    private String windowSizeKey = "WindowSize";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    public BBSCoordinatorBolt(String redisKey, JedisClusterConfig jedisClusterConfig) {
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

        allParameters = jedisCommands.hgetAll(redisKey);
        sampleName = allParameters.get(sampleKey);
        preSampleName = allParameters.get(preSampleKey);
        sampleSize = Integer.parseInt(allParameters.get(sampleSizeKey)); // Get sample size
        windowSize = Integer.parseInt(allParameters.get(windowSizeKey)); // Get window size
        jedisCommands.ltrim(sampleName, 0, -99999);
        jedisCommands.ltrim(preSampleName, 0, -99999);
        jedisCommands.hset(redisKey, roundKey, "0");
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getStringByField("data");
        int count = input.getIntegerByField("count");
        int bitNumber = input.getIntegerByField("bitNumber");

        if(bitNumber == 0){
            int redisSampleSize = jedisCommands.rpush(sampleName, data).intValue();

            if(redisSampleSize == sampleSize){
                jedisCommands.hincrBy(redisKey, roundKey, 1);
                distributeSample(redisSampleSize);
            }
        }
        else{
            jedisCommands.rpush(preSampleName, data);
        }

        if(count%windowSize == 0){
            List<String> sampleList = jedisCommands.lrange(sampleName, 0, -1); // Get sample list
            List<String> preSampleList = jedisCommands.lrange(preSampleName, 0, -1); // Get presample list

            sampleList.addAll(preSampleList);

            collector.emit(new Values(sampleList)); // Emit
            jedisCommands.ltrim(sampleName, -0, -99999); // Remove sample list
            jedisCommands.ltrim(preSampleName, -0, -99999);
            jedisCommands.hset(redisKey, roundKey, "0");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sampleList"));
    }

    public void distributeSample(int redisSampleSize){
        jedisCommands.ltrim(preSampleName, 0, -99999);

        for(int i = 0; i < redisSampleSize; i++){
            Random random = new Random();
            String data = jedisCommands.lpop(sampleName);
            if(random.nextBoolean()){
                jedisCommands.rpush(preSampleName, data);
            }
            else{
                jedisCommands.rpush(sampleName, data);
            }
        }
    }
}