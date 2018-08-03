package i2am.filtering;

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
    private double x, P, R, Q, A, H;
    /* RedisKey */
    private String redisKey = null;
    private String initXValueKey = "Init_x_val";
    private String initPValueKey = "Init_P_val";
    private String AValueKey = "A_val";
    private String HValueKey = "H_val";
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

        x = Double.parseDouble(jedisCommands.hget(redisKey, initXValueKey));
        P = Double.parseDouble(jedisCommands.hget(redisKey, initPValueKey));
        A = Double.parseDouble(jedisCommands.hget(redisKey, AValueKey));
        H = Double.parseDouble(jedisCommands.hget(redisKey, HValueKey));
        Q = Double.parseDouble(jedisCommands.hget(redisKey, QValueKey));
        R = Double.parseDouble(jedisCommands.hget(redisKey, RValueKey));
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getStringByField("data");
        int targetIndex = input.getIntegerByField("targetIndex");
        double x_present = Double.parseDouble(input.getStringByField("target"));
        double x_next, P_next, K, z;

        x_next = A * x;
        P_next = P+ Q; 	//Q: white noise --> by environment
        K = P_next * H / (H * H * P_next + R);	//kalman gain

        z = H * x_present;
        x = x_next + K*(z - H*x_next);		// filtered data
        P = (1 - K*H)*P_next;

       String switchedData = switchTarget(data, targetIndex, x);

        collector.emit(new Values(switchedData));
    }

    public String switchTarget(String data, int targetIndex, Double x){
        String dataArray[] = data.split(",");
        dataArray[targetIndex] = Double.toString(x);
        String switchedData = "";
        for(String string: dataArray){
            switchedData = switchedData + "," + string;
        }
        switchedData = switchedData.replaceFirst(",", "");
        return switchedData;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}
