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

import java.util.List;
import java.util.Map;

public class QueryFilteringBolt extends BaseRichBolt{
    private String keywords;

    /* RedisKey */
    private String redisKey = null;
    private String keywordsKey = "Keywords";

    /* Jedis */
    private transient JedisCommandsInstanceContainer jedisContainer;
    private JedisClusterConfig jedisClusterConfig;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(QueryFilteringBolt.class);

    public QueryFilteringBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
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

        keywords = jedisCommands.hget(redisKey, keywordsKey);
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);

        /* Query Filtering */
        for(String word : keywords.split(" ")){
            if(data.contains(word)){
                collector.emit(new Values(data));
                break;
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}
