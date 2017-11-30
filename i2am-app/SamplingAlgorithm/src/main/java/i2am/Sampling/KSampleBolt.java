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

import java.util.Map;


public class KSampleBolt extends BaseRichBolt {
    //private int interval;
    private double samplingRate;
    //private double randomNumber;
    private long count;
    private String sampleName = null;
    private Map<String, String> allParameters;

    /* RedisKey */
    private String redisKey = null;
    private String srKey = "SamplingRateKey";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(SystematicSamplingBolt.class);

    public KSampleBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        count = 0;
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = collector;

        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

		/* Get parameters */
        samplingRate = Integer.parseInt(jedisCommands.hget(redisKey, srKey));

    }

    @Override
    public void execute(Tuple input) {
        String sampleElement="";
        String data = input.getString(0);
        int sampleSize=1;
        double slot=0;
        double prob=0.0;
        double randomNumber = 1.0;

        count++;

        /* KSample */
        if (sampleSize <= (samplingRate*count)){
            collector.emit(new Values(sampleElement));
            slot=0;
            sampleSize++;
        }
        slot+=1.0;

        prob=(1.0/slot);
        randomNumber = Math.random();

        if (prob > randomNumber){
            sampleElement=data;
        }

        if(count == 1000) count = 0; // Overflow Exception



    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}
