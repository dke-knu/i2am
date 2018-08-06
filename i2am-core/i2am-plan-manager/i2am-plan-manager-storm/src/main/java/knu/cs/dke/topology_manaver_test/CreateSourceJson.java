package knu.cs.dke.topology_manaver_test;

import java.util.Date;

import org.apache.storm.utils.Time;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreateSourceJson {

	private JSONObject source;

	public CreateSourceJson() {
		source = new JSONObject();
	}

	public void setCommand() {		

		// Command Info.		
		source.put("commandType", "CREATE_SRC");
		source.put("commander", "0KUK@naver.com");
		source.put("commandId", "9d4e1696-40d0-4e3e-b185-7edd8ff967db");
		source.put("commandTime", "2017-11-15 17:32:18");
		
		JSONObject content = new JSONObject();		
		content.put("owner", "0KUK@naver.com");
		content.put("srcName", "Source01");
		content.put("usesIntelligentEngine", "N");
		content.put("createdTime", "2017-11-15 17:32:18");
		content.put("srcType", "KAFKA");	
		content.put("testData", "data");
		
		JSONObject params = new JSONObject();
		params.put("zookeeperIp", "192.168.56.100");
		params.put("zookeeperPort", "2182");
		params.put("topic", "topic01");
		
		content.put("kafkaParams", params);		
		
		source.put("commandContent", content);		
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
