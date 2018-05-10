package i2am.Declaring;

import i2am.Common.DbAdapter;
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
import java.util.HashMap;
import java.util.Map;

public class PriorityDeclaringBolt extends BaseRichBolt {
    private int count;
    private int windowSize;
    private int targetIndex;
    private String topologyName;
    private DbAdapter dbAdapter;
    private Map<String, Integer> dataMap; // It saves data's weight

    /* Redis */
    private String redisKey;
    private String windowSizeKey = "WindowSize";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(PriorityDeclaringBolt.class);

    public PriorityDeclaringBolt(String topologyName, String redisKey, JedisClusterConfig jedisClusterConfig){
        count = 0;
        this.topologyName = topologyName;
        dbAdapter = new DbAdapter();
        dataMap = new HashMap<String, Integer>();
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
            dbAdapter.connect();
            targetIndex = dbAdapter.getTargetIndex(dbAdapter.getTarget(topologyName));
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
        int weight;

        if(dataMap.containsKey(target)){
            weight = dataMap.get(target) + 1;
            dataMap.replace(target, weight);
        }else{
            weight = 1;
            dataMap.put(target, 1);
        }

        collector.emit(new Values(data, count, weight));

        if(count == windowSize){
            count = 0;
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "count", "weight"));
    }
}