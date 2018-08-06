package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class KSampling extends Algorithm {
	private int samplingRate;
	
	public KSampling(int idx, int samplingRate) {
		super(idx);
		super.type = ALGORITHM_TYPE.K_SAMPLING;
		this.samplingRate = samplingRate;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("samplingRate", getSamplingRate());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
