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

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class SystematicSamplingBolt extends BaseRichBolt {
    private int interval;
    private int randomNumber;
    private long count;
    private String sampleName = null;
    private Map<String, String> allParameters;

    /* RedisKey */
    private String redisKey = null;
    private String intervalKey = "Interval";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(SystematicSamplingBolt.class);

    public SystematicSamplingBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        count = 0;
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
        interval = Integer.parseInt(jedisCommands.hget(redisKey, intervalKey));
        randomNumber = (int)(Math.random()*interval);
    }

    @Override
    public void execute(Tuple input) {
        if(count == Long.MAX_VALUE) count = 0; // Overflow Exception

        count++;
        String data = input.getString(0);

        /* Systematic Sampling */
        if(count%interval == randomNumber) {
            collector.emit(new Values(data));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}