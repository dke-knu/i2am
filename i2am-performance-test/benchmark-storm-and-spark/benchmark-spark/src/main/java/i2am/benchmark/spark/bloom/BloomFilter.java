package i2am.benchmark.spark.bloom;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class BloomFilter implements Serializable {

	int bucketSize;
	List<Boolean> buckets;
	HashFunction hashFunction = new HashFunction();

	BloomFilter(int bucketSize){

		this.bucketSize = bucketSize; 
		buckets = new ArrayList<Boolean>();

		for(int i = 0; i < bucketSize; i++){
			buckets.add(false);
		}
	}

	void registData(String data) throws UnsupportedEncodingException{

		int hashCode = 0;

		hashCode = hashFunction.javaHashFunction(data);
		buckets.set(hashCode%bucketSize, true);

		hashCode = hashFunction.xxHash32(data);
		buckets.set(hashCode%bucketSize, true);

		hashCode = hashFunction.JSHash(data);
		buckets.set(hashCode%bucketSize, true);
	}

	boolean filtering(String data) throws UnsupportedEncodingException{

		boolean flag = false;

		int hashCode1 = 0;
		int hashCode2 = 0;
		int hashCode3 = 0;

		hashCode1 = hashFunction.javaHashFunction(data) % bucketSize;
		hashCode2 = hashFunction.xxHash32(data) % bucketSize;
		hashCode3 = hashFunction.JSHash(data) % bucketSize;

		if(buckets.get(hashCode1) && buckets.get(hashCode2) && buckets.get(hashCode3)){
			flag = true;
		}
		return flag;
	}	
}
