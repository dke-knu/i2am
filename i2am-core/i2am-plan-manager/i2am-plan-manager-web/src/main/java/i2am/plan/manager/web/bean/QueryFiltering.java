package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

import i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE;

public class QueryFiltering extends Algorithm {
	private String keywords;
	
	public QueryFiltering(int idx, String keywords) {
		super(idx);
		super.type = ALGORITHM_TYPE.QUERY_FILTERING;
		this.keywords = keywords;
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
		params.put("keywords", getKeywords());
		
		obj.put("algorithmParams", params);
		
		return obj;
	}
}
