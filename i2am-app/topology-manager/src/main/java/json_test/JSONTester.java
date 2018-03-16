package json_test;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONTester {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub

		// GET JSON - String 형태로 JSON이 입력된당.
		JSONSample json = new JSONSample();
		json.setCommand();
		//json.printJSON();
		
		// PARSE JSON
		String json_result = json.getCommand().toJSONString();
		
		
		//  String에서 다시 JSON 형태로 바꾼당.
		JSONParser parser = new JSONParser();
		JSONObject parsed = (JSONObject) parser.parse(json_result);		
				
		// Command Type
		System.out.println(parsed.get("commandId"));
		System.out.println(parsed.get("commander"));
		System.out.println(parsed.get("commandType"));
		System.out.println(parsed.get("commandTime"));
		System.out.println(parsed.get("commandContent"));		
		
		// Command Content
		JSONObject contents = (JSONObject) parsed.get("commandContent");		
		System.out.println(contents.get("planId"));
		System.out.println(contents.get("owner"));
		System.out.println(contents.get("createTime"));
				
		JSONArray algorithms = (JSONArray) contents.get("algorithms");
		
		int a = 
		
		
		//algorithms.
		
		
	}

}
