package i2am.plan.manager.web.bean;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import i2am.plan.manager.web.bean.DatabaseInfo.DATABASE_TYPE; 

public class DataScheme {

	String[] data;
	
	public DataScheme(String[] data) {				
		this.data = data;
	}	
	
	public JSONArray toJSONArray() {
		
		JSONArray obj = new JSONArray();		
				
		for(int i=0; i<data.length; i++) {
			
			String[] columns = data[i].split(",");
			
			JSONObject temp = new JSONObject(); 
			
			temp.put("column_index", columns[0]);
			temp.put("column_name", columns[1]);
			temp.put("column_type", columns[2]);
			
			obj.add(temp);
		}		
		
		return obj;
	}	
}
