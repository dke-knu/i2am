package i2am.filtering;

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

import i2am.filtering.common.DbAdapter;
import redis.clients.jedis.JedisCommands;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BloomFilteringBolt extends BaseRichBolt {
    private int bucketSize;
    private String keywords;
    private BloomFilter bloomFilter; // Bloom Filter
    private String topologyName;
    private String hashFunctions[];

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
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
        this.topologyName = topologyName;
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
            hashFunctions = DbAdapter.getInstance().getBloomHashFunction(topologyName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        bucketSize = Integer.parseInt(jedisCommands.hget(redisKey, bucketSizeKey));
        keywords = jedisCommands.hget(redisKey, keywordsKey);
        bloomFilter = new BloomFilter(bucketSize, hashFunctions);
        for(String word: keywords.split(" ")){
            try {
                bloomFilter.registering(word);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getStringByField("data");
        String target = input.getStringByField("target");
        boolean flag = false;

        try {
            flag = bloomFilter.filtering(target);
            if(flag){
                collector.emit(new Values(data));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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
    private int hashNumber;
    private HashFunction hashFunction = new HashFunction();

    BloomFilter(int bucketSize, String[] hashFunctions){
        this.bucketSize = bucketSize;
        this.hashFunctions = hashFunctions;
        this.hashNumber = 3;
        buckets = new ArrayList<Boolean>();

        for(int i = 0; i < bucketSize; i++){
            buckets.add(false);
        }
    }

    // Registering data
    void registering(String data) throws UnsupportedEncodingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int hashCode = 0;

        for(int i = 0; i < hashNumber; i++){
            if(hashFunctions[i].equals("javaHashFunction")){
                hashCode = hashFunction.javaHashFunction(data);
            }
            else if(hashFunctions[i].equals("xxHash32")){
                hashCode = hashFunction.xxHash32(data);
            }
            else if(hashFunctions[i].equals("jsHash")){
                hashCode = hashFunction.jsHash(data);
            }
            buckets.set(hashCode%bucketSize, true);
        }
    }

    // Filtering
    boolean filtering(String data) throws UnsupportedEncodingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        boolean flag = false;
        int hashCode[] = {0, 0, 0};

        for(int i = 0; i < hashNumber; i++){
            if(hashFunctions[i].equals("javaHashFunction")){
                hashCode[i] = hashFunction.javaHashFunction(data);
            }
            else if(hashFunctions[i].equals("xxHash32")){
                hashCode[i] = hashFunction.xxHash32(data);
            }
            else if(hashFunctions[i].equals("jsHash")){
                hashCode[i] = hashFunction.jsHash(data);
            }
        }

        if(buckets.get(hashCode[0]%bucketSize) && buckets.get(hashCode[1]%bucketSize) && buckets.get(hashCode[2]%bucketSize)){
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