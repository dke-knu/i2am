package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class HashSampling extends Algorithm {
	private int sampleRatio;
	private int windowSize;
	private String hashFunction;
	private int bucketSize;
	
	public HashSampling(int idx, int sampleRatio, int windowSize, String hashFunction, int bucketSize) {
		super(idx);
		super.type = ALGORITHM_TYPE.SYSTEMATIC_SAMPLING;
		this.sampleRatio = sampleRatio;
		this.windowSize = windowSize;
		this.hashFunction = hashFunction;
		this.bucketSize = bucketSize;
	}

	public int getSampleRatio() {
		return sampleRatio;
	}

	public int getWindowSize() {
		return windowSize;
	}
	
	public String getHashFunction() {
		return hashFunction;
	}

	public int getBucketSize() {
		return bucketSize;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("sampleRatio", getSampleRatio());
		params.put("windowSize", getWindowSize());
		params.put("hashFunction", getHashFunction());
		params.put("bucketSize", getBucketSize());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
