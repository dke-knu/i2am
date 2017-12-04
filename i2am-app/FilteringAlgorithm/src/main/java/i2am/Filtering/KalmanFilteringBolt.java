package i2am.Filtering;

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

/**
 * Created by sbpark on 2017-12-04.
 */
public class KalmanFilteringBolt extends BaseRichBolt {
    private double x = 0, P = 1000, R, Q;
    /* RedisKey */
    private String redisKey = null;
    private String QValueKey = "Q_val";
    private String RValueKey = "R_val";

    /* Jedis */
    private transient JedisCommandsInstanceContainer jedisContainer;
    private JedisClusterConfig jedisClusterConfig;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(KalmanFilteringBolt.class);

    public KalmanFilteringBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        /* Get Q and R value from user(redis) */
        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

        Q = Double.parseDouble(jedisCommands.hget(redisKey, QValueKey));
        R = Double.parseDouble(jedisCommands.hget(redisKey, RValueKey));

    }

    @Override
    public void execute(Tuple input) {
        double x_present = Double.parseDouble(input.getString(0));
        double x_next, P_next, K, z, H = 1;

        x_next = x;
        P_next = P+ Q; 	//Q: white noise --> by environment
        K = P_next*H / (H*H*P_next + R);	//kalman gain

        z = x_present;
        x = x_next + K*(z - H*x_next);		// filtered data
        P = (1 - K*H)*P_next;

        collector.emit(new Values(Double.toString(x)));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}
