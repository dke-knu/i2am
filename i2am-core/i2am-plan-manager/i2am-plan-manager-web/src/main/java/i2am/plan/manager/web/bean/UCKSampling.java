package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class UCKSampling extends Algorithm {
	private int samplingRate;
	private double ucUnderBound;
	
	public UCKSampling(int idx, int samplingRate, double ucUnderBound) {
		super(idx);
		super.type = ALGORITHM_TYPE.UC_K_SAMPLING;
		this.samplingRate = samplingRate;
		this.ucUnderBound = ucUnderBound;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public double getUcUnderBound() {
		return ucUnderBound;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("samplingRate", getSamplingRate());
		params.put("ucUnderBound", getUcUnderBound());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
