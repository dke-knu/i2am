package i2am.benchmark.spark.bloom;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

class HashFunction implements Serializable {

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

	int JSHash(String data){
		int hashCode = 1315423911;

		for(int i = 0; i < data.length(); i++){
			hashCode ^= ((hashCode << 5) + data.charAt(i) + (hashCode >> 2));
		}

		hashCode = Math.abs(hashCode);

		return hashCode;
	}
}