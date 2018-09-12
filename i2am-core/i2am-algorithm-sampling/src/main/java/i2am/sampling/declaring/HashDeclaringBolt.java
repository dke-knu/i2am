package i2am.sampling.declaring;

import i2am.sampling.common.DbAdapter;
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
import java.sql.SQLException;
import java.util.Map;

public class HashDeclaringBolt extends BaseRichBolt {
    private int count;
    private int windowSize;
    private int targetIndex;
    private String topologyName;

    /* Redis */
    private String redisKey;
    private String windowSizeKey = "WindowSize";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(DeclaringBolt.class);

    public HashDeclaringBolt(String topologyName, String redisKey, JedisClusterConfig jedisClusterConfig){
        count = 0;
        this.topologyName = topologyName;
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        /* Connect to Redis */
        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

        try {
            targetIndex = DbAdapter.getInstance().getTargetIndex(topologyName, "HASH_SAMPLING");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        windowSize = Integer.parseInt(jedisCommands.hget(redisKey, windowSizeKey));
    }

    @Override
    public void execute(Tuple input) {
        count++;
        String data = input.getString(0);
        String target = data.split(",")[targetIndex];
        collector.emit(new Values(data, count, target));

        if(count == windowSize){
            count = 0;
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "count", "target"));
    }
}