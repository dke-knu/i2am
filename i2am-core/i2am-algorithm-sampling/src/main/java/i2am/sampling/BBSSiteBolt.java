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
import scala.util.Random;

import java.util.Map;

public class BBSSiteBolt extends BaseRichBolt{
    private int windowSize;
    private Map<String, String> allParameters;

    private OutputCollector collector;

    /* Redis */
    private String redisKey;
    private String windowSizeKey = "WindowSize";

    /* Jedis */
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisCommands jedisCommands = null;

    public BBSSiteBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
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
        windowSize = Integer.parseInt(allParameters.get(windowSizeKey)); // Get window size
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getStringByField("data");
        int count = input.getIntegerByField("count");
        int round = input.getIntegerByField("round");
        int bitNumber = bitGenerate(round);

        if(count%windowSize == 0) collector.emit(new Values(data, count, bitNumber)); // Emit
        if(bitNumber < 2) collector.emit(new Values(data, count, bitNumber)); // Emit
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "count", "bitNumber"));
    }

    public int bitGenerate(int round){
        int maxNumber = (int)Math.pow(2 ,round+1);

        return  (int)(Math.random()*maxNumber);
    }
}