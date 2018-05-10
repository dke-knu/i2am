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
    private int samplingRate;
    //private double randomNumber;
    private long count;
    private String sampleElement="";

    /* RedisKey */
    private String redisKey = null;
    private String srKey = "SamplingRate";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(KSampleBolt.class);

    public KSampleBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        count = 0;
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;

        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
            logger.info("Jedis Connection");
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

		/* Get parameters */
        logger.info("############# KSAMPLEBOLT");
        logger.info(redisKey);
        logger.info(srKey);

        logger.info(jedisCommands.hget(redisKey, srKey));

        samplingRate = Integer.parseInt(jedisCommands.hget(redisKey, srKey));
    }

    @Override
    public void execute(Tuple input) {

        String data = input.getString(0);
        count++;
        double slot;
        double prob=0.0;
        double randomNumber = 1.0;

        if ( (count%samplingRate)==0) slot=samplingRate;
        else slot = count%samplingRate;

        /* KSample */
        prob=(1.0/slot);
        randomNumber = Math.random();

        /*
        logger.info("##########HJKIM Data: " + data);
        logger.info("##########HJKIM Prob: " + prob);
        logger.info("##########HJKIM Rand: " + randomNumber);
        */

        if (prob > randomNumber){
            sampleElement=data;
        }

        logger.info("##########HJKIM dataEle: " + sampleElement);

        if(count%samplingRate == 0){
            collector.emit(new Values(sampleElement));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}