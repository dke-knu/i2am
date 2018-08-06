package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class StratifiedSampling extends Algorithm {
	private int sampleSize;
	private int windowSize;
	
	public StratifiedSampling(int idx, int sampleSize, int windowSize) {
		super(idx);
		super.type = ALGORITHM_TYPE.STRATIFIED_SAMPLING;
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("sampleSize", getSampleSize());
		params.put("windowSize", getWindowSize());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
