package i2am.tta.bloom;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

public class BloomFilteringBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(BloomFilteringBolt.class);
	
	private BloomFilter bloomFilter;
	private final int BUCKET_SIZE = 10;
	private final String HASH_FUNCTIONS[] = new String[]{"javaHashFunction", "xxHash32", "jsHash"};
	private final String KEYWORDS = "alice bob";

	private OutputCollector collector;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.bloomFilter = new BloomFilter(BUCKET_SIZE, HASH_FUNCTIONS);
		for(String word: KEYWORDS.split(" ")){
			try {
				bloomFilter.registering(word);
			} catch (UnsupportedEncodingException e) {
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
			if (flag) collector.emit(new Values(data, 1, input.getLongByField("start-time")));
			else collector.emit(new Values(data, 0, input.getLongByField("start-time"))); 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("data", "sampleFlag", "start-time"));
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
	void registering(String data) throws UnsupportedEncodingException {
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
	boolean filtering(String data) throws UnsupportedEncodingException {
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