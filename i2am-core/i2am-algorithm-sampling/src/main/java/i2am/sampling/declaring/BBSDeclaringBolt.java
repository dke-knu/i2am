package i2am.sampling.declaring;

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

import java.util.Map;

public class BBSDeclaringBolt extends BaseRichBolt {
    private int count;
    private int windowSize;

    /* Redis */
    private String redisKey;
    private String windowSizeKey = "WindowSize";
    private String roundKey = "Round";

    /* Jedis */
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    public BBSDeclaringBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        count = 0;
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

        windowSize = Integer.parseInt(jedisCommands.hget(redisKey, windowSizeKey));
    }

    @Override
    public void execute(Tuple input) {
        count++;
        String data = input.getString(0);
        int round = Integer.parseInt(jedisCommands.hget(redisKey, roundKey));
        collector.emit(new Values(data, count, round));

        if(count == windowSize){
            count = 0;
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "count", "round"));
    }
}