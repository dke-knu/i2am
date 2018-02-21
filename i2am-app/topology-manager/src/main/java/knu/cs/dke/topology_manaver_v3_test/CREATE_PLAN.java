package knu.cs.dke.topology_manaver_v3_test;

import java.util.Date;

import org.apache.storm.utils.Time;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CREATE_PLAN {

	private JSONObject plan;	
	
	public CREATE_PLAN() {
		plan = new JSONObject();
	}
	
	// Web에서 사용자가 입력하였다고 가정하고 생성
	// 이것은 Web Ui에서 해야할 일임
	public void setCommand() {		
		
		// Command Info.		
		plan.put("commandId", "user01-djWjrnwjWJrn");
		plan.put("commander", "user01");
		plan.put("commandType", "CREATE_PLAN");
		plan.put("commandTime", Time.currentTimeMillis());
		
		// Content Info.
		JSONObject commandContent = new JSONObject();		
		commandContent.put("planId", "user01-001"+new Date());
		commandContent.put("owner", "user01");
		commandContent.put("createTime", Time.currentTimeMillis());		
		
		// Algorithm Info. [Array]
		JSONArray algorithms = new JSONArray();
		
		// Algorithms 1
		JSONObject algorithm1 = new JSONObject();       
		algorithm1.put("algorithmIdx", 1);
		algorithm1.put("algorithmType", "HASH_SAMPLING");
		
		// Algorithm 1 > Parameters
		JSONObject algorithm1_params = new JSONObject();
		algorithm1_params.put("numberOfBucket", 15);
		algorithm1_params.put("selectedBucket", 5);		
		
		algorithm1.put("algorithmParams", algorithm1_params);
		
		/*
		// Algorithms 2
		JSONObject algorithm2 = new JSONObject();       
		algorithm2.put("algorithmIdx", 2);
		algorithm2.put("algorithmType", "PRIORITY_SAMPLING");
		
		// Algorithm 2 > Parameters
		JSONObject algorithm2_params = new JSONObject();
		algorithm2_params.put("sizeOfSample", 100);
		
		algorithm2.put("algorithmParams", algorithm2_params);
		*/
		
		// Set
		algorithms.add(algorithm1);
		//algorithms.add(algorithm2);
		
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
