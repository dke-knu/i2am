package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class HashSampling extends Algorithm {
	private int sampleRatio;
	private int windowSize;
	private String hashFunction;
	
	public HashSampling(int idx, int sampleRatio, int windowSize, String hashFunction) {
		super(idx);
		super.type = ALGORITHM_TYPE.HASH_SAMPLING;
		this.sampleRatio = sampleRatio;
		this.windowSize = windowSize;
		this.hashFunction = hashFunction;
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

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("sampleRatio", getSampleRatio());
		params.put("windowSize", getWindowSize());
		params.put("hashFunction", getHashFunction());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
