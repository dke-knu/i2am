package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class SystematicSampling extends Algorithm {
	private int sampleRatio;
	private int windowSize;
	
	public SystematicSampling(int idx, int sampleRatio, int windowSize) {
		super(idx);
		super.type = ALGORITHM_TYPE.SYSTEMATIC_SAMPLING;
		this.sampleRatio = sampleRatio;
		this.windowSize = windowSize;
	}

	public int getSampleRatio() {
		return sampleRatio;
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
		params.put("sampleRatio", getSampleRatio());
		params.put("windowSize", getWindowSize());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
