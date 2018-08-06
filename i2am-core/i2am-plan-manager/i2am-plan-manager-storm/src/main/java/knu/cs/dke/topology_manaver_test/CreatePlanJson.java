package knu.cs.dke.topology_manaver_test;

import java.util.Date;

import org.apache.storm.utils.Time;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreatePlanJson {

	private JSONObject plan;	
	
	public CreatePlanJson() {
		plan = new JSONObject();
	}
	
	// Web에서 사용자가 입력하였다고 가정하고 생성
	// 이것은 Web Ui에서 해야할 일임
	public void setCommand() {		
		
		// Command Info.		
		plan.put("commandId", "3df510c4-f465-4ecf-bef2-c99b8c1bb173");
		plan.put("commander", "0KUK@naver.com");
		plan.put("commandType", "CREATE_PLAN");
		plan.put("commandTime", "2017-11-15 21:56:59");
		
		// Content Info.
		JSONObject commandContent = new JSONObject();		
		commandContent.put("owner", "0KUK@naver.com");
		commandContent.put("planName", "myPlan");		
		commandContent.put("createdTime", "2017-11-15 21:56:59");		
	
		commandContent.put("srcName", "SRC2");
		commandContent.put("dstName", "DST2");
		
		JSONArray algorithms = new JSONArray();
		
		// Algorithms 1
		JSONObject algorithm1 = new JSONObject();       
		algorithm1.put("algorithmIdx", 1);
		algorithm1.put("algorithmType", "BINARY_BERNOULLI_SAMPLING");
		
		// Algorithm 1 > Parameters
		JSONObject algorithm1_params = new JSONObject();
		algorithm1_params.put("windowSize", 100);
		algorithm1_params.put("sampleSize", 20);		
		
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
