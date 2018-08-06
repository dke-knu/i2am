package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class BloomFiltering extends Algorithm {
	private int bucketSize;
	private String keywords;
	
	public BloomFiltering(int idx, int bucketSize, String keywords) {
		super(idx);
		super.type = ALGORITHM_TYPE.BLOOM_FILTERING;
		this.bucketSize = bucketSize;
		this.keywords = keywords;
	}

	public int getBucketSize() {
		return bucketSize;
	}

	public String getKeywords() {
		return keywords;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("bucketSize", getBucketSize());
		params.put("keywords", getKeywords());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
