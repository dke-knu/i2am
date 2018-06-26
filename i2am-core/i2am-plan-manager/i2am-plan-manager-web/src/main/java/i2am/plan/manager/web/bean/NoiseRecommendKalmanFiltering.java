package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;
import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class NoiseRecommendKalmanFiltering extends Algorithm {
	private double qVal;
	
	public NoiseRecommendKalmanFiltering(int idx, double qVal) {
		super(idx);
		super.type = ALGORITHM_TYPE.NOISE_RECOMMEND_KALMAN_FILTERING;
		
		this.qVal = qVal;
	}

	public double getqVal() {
		return qVal;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		params.put("qVal", getqVal());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
