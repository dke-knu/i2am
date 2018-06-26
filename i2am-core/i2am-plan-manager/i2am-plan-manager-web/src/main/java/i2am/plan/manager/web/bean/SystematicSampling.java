package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class SystematicSampling extends Algorithm {
	private int interval;
	
	public SystematicSampling(int idx, int interval) {
		super(idx);
		super.type = ALGORITHM_TYPE.SYSTEMATIC_SAMPLING;
		this.interval = interval;
	}
	
	public int getInterval() {
		return interval;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("interval", getInterval());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
