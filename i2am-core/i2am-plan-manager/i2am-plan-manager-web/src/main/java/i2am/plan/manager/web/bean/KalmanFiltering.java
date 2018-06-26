package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;
import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class KalmanFiltering extends Algorithm {
	private double qVal;
	private double rVal;
	
	public KalmanFiltering(int idx, double qVal, double rVal) {
		super(idx);
		super.type = ALGORITHM_TYPE.KALMAN_FILTERING;
		
		this.qVal = qVal;
		this.rVal = rVal;
	}

	public double getqVal() {
		return qVal;
	}

	public double getrVal() {
		return rVal;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("algorithmIdx", getIdx());
		obj.put("algorithmType", getType().name());
		
		JSONObject params = new JSONObject();
		
		obj.put("algorithmParams", params);
		params.put("qVal", getqVal());
		params.put("rVal", getrVal());
		
		return obj;
	}
}
