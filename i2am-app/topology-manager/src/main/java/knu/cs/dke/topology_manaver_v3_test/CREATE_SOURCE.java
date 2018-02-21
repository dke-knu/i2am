package knu.cs.dke.topology_manaver_v3_test;

import java.util.Date;

import org.apache.storm.utils.Time;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CREATE_SOURCE {

	private JSONObject source;

	public CREATE_SOURCE() {
		source = new JSONObject();
	}

	public void setCommand() {		

		// Command Info.		
		source.put("commandType", "CREATE_SOURCE");
		source.put("sourceName", "user01-source");
		source.put("createTime", Time.currentTimeMillis());
		source.put("modifiedTime", Time.currentTimeMillis());
		source.put("status", "INACTIVE");
		source.put("owner", "user01");
		source.put("type", "kafka");
		
		// Kafka 라면 주키퍼 IP Port Topic 추가적으로 필요
		// Database 라면 IP Port User Password DB Table Query 추가적으로 필요		
		// ??
	}	

	public JSONObject getCommand() {
		return source;
	}

	public String getStringPlan() {		
		String result = source.toString();
		return result;
	}

	public void printJSON() {		
		String result = source.toString();
		System.out.println(result);
	}

}
