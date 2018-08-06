package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public abstract class Algorithm {
	private int idx;
	protected ALGORITHM_TYPE type;
	
	public Algorithm(int idx) {
		this.idx = idx;
	}

	public int getIdx() {
		return idx;
	}

	public ALGORITHM_TYPE getType() {
		return type;
	}
	
	public abstract JSONObject toJSONObject();
}
