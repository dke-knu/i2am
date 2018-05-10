package i2am.Filtering;

import i2am.Common.DbAdapter;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
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

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BloomFilteringBolt extends BaseRichBolt {
    private int bucketSize;
    private String keywords;
    private List<String> wordArray; // Filter List
    private BloomFilter bloomFilter; // Bloom Filter
    private String topologyName;
    private String[] hashFunctions;
    private DbAdapter dbAdapter;

    /* RedisKey */
    private String redisKey = null;
    private String keywordsKey = "Keywords";
    private String bucketSizeKey = "BucketSize";

    /* Jedis */
    private transient JedisCommandsInstanceContainer jedisContainer;
    private JedisClusterConfig jedisClusterConfig;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(BloomFilteringBolt.class);

    public BloomFilteringBolt(String redisKey, JedisClusterConfig jedisClusterConfig, String topologyName){
        this.wordArray = wordArray;
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
        this.topologyName = topologyName;
        dbAdapter = new DbAdapter();
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

        try {
            dbAdapter.connect();
            hashFunctions = dbAdapter.getBloomHashFunction(topologyName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        bucketSize = Integer.parseInt(jedisCommands.hget(redisKey, bucketSizeKey));
        keywords = jedisCommands.hget(redisKey, keywordsKey);
        bloomFilter = new BloomFilter(bucketSize, hashFunctions);
        for(String word: keywords.split(" ")){
            try {
                bloomFilter.registData(word);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);
        boolean flag = false;

        String[] words  = data.split(" ");
        for(String word : words){
            try {
                flag = bloomFilter.filtering(word);
                if(flag){
                    collector.emit(new Values(data));
                    break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}

/* Bloom Filter Class */
class BloomFilter{
    private int bucketSize;
    private String[] hashFunctions;
    private List<Boolean> buckets;
    private HashFunction hashFunction = new HashFunction();

    BloomFilter(int bucketSize, String[] hashFunctions){
        this.bucketSize = bucketSize;
        this.hashFunctions = hashFunctions;
        buckets = new ArrayList<Boolean>();

        for(int i = 0; i < bucketSize; i++){
            buckets.add(false);
        }
    }

    // Regeist Data to Filter
    void registData(String data) throws UnsupportedEncodingException {
        int hashCode = 0;

        hashCode = hashFunction.javaHashFunction(data);
        buckets.set(hashCode%bucketSize, true);

        hashCode = hashFunction.xxHash32(data);
        buckets.set(hashCode%bucketSize, true);

        hashCode = hashFunction.jsHash(data);
        buckets.set(hashCode%bucketSize, true);
    }

    // Filtering
    boolean filtering(String data) throws UnsupportedEncodingException {
        boolean flag = false;
        int hashCode1 = 0;
        int hashCode2 = 0;
        int hashCode3 = 0;

        hashCode1 = hashFunction.javaHashFunction(data);
        hashCode2 = hashFunction.xxHash32(data);
        hashCode3 = hashFunction.jsHash(data);

        if(buckets.get(hashCode1%bucketSize) && buckets.get(hashCode2%bucketSize) && buckets.get(hashCode3%bucketSize)){
            flag = true;
        }

        return flag;
    }
}

/* Hash Filter Class */
class HashFunction{
    HashFunction(){}

    int javaHashFunction(String data){
        int hashCode = data.hashCode();
        hashCode = Math.abs(hashCode);

        return hashCode;
    }

    int xxHash32(String data) throws UnsupportedEncodingException{
        byte[] byteData = data.getBytes("euc-kr");

        XXHashFactory factory = XXHashFactory.fastestInstance();
        XXHash32 hash32 = factory.hash32();
        int seed = 0x9747b28c;

        int hashCode = hash32.hash(byteData,  0, byteData.length, seed);
        hashCode = Math.abs(hashCode);

        return hashCode;
    }

    int jsHash(String data){
        int hashCode = 1315423911;

        for(int i = 0; i < data.length(); i++){
            hashCode ^= ((hashCode << 5) + data.charAt(i) + (hashCode >> 2));
        }

        hashCode = Math.abs(hashCode);

        return hashCode;
    }
}