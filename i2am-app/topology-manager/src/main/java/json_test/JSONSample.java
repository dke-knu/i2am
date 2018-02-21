package json_test;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONSample {

	private JSONObject plan;	
	
	public JSONSample() {
		plan = new JSONObject();
	}
	
	// Web에서 사용자가 입력하였다고 가정하고 생성
	// 이것은 Web Ui에서 해야할 일임
	public void setCommand() {		
		
		// Command Info.		
		plan.put("commandId", "user01-djWjrnwjWJrn");
		plan.put("commander", "user01");
		plan.put("commandType", "CREATE_PLAN");
		
		plan.put("production", 1);
		
		long time = System.currentTimeMillis();
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String time_str = dayTime.format(new Date(time));
		plan.put("commandTime", time_str);
		
		// Content Info.
		JSONObject commandContent = new JSONObject();		
		commandContent.put("planId", "user01-001");
		commandContent.put("owner", "user01");
		commandContent.put("createTime", time_str);		
		
		// Algorithm Info. [Array]
		JSONArray algorithms = new JSONArray();
		
		// Algorithms 1
		JSONObject algorithm1 = new JSONObject();       
		algorithm1.put("algorithmIdx", 1);
		algorithm1.put("algorithmType", "HASH_SAMPLING");
		
		// Algorithm 1 > Parameters
		JSONObject algorithm1_params = new JSONObject();
		algorithm1_params.put("numberOfBucket", 15);
		algorithm1_params.put("selectBucket", 5);		
		
		algorithm1.put("algorithmParams", algorithm1_params);
		
		// Algorithms 2
		JSONObject algorithm2 = new JSONObject();       
		algorithm2.put("algorithmIdx", 2);
		algorithm2.put("algorithmType", "PRIORITY_SAMPLING");
		
		// Algorithm 2 > Parameters
		JSONObject algorithm2_params = new JSONObject();
		algorithm2_params.put("sizeOfSample", 100);
		
		algorithm2.put("algorithmParams", algorithm2_params);
		
		// Set
		algorithms.add(algorithm1);
		algorithms.add(algorithm2);
		
		commandContent.put("algorithms", algorithms);			
		
		plan.put("commandContent", commandContent);
	}	

	public JSONObject getCommand() {
		return plan;
	}
	
	public String getStringPlan() {		
		String result = plan.toString();
		return result;
	}
	
	public void printJSON() {		
		String result = plan.toString();
		System.out.println(result);
	}
}
