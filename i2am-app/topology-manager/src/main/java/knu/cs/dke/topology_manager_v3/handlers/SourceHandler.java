package knu.cs.dke.topology_manager_v3.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager_v3.SourceList;
import knu.cs.dke.topology_manager_v3.sources.KafkaSource;
import knu.cs.dke.topology_manager_v3.sources.Source;

public class SourceHandler {

	private SourceList sources;	
	
	public SourceHandler(SourceList sources) {
		this.sources = sources;
	}
	
	public void excute(String command) throws ParseException {
		
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) jsonParser.parse(command);		
		System.out.println("[Command Handler] Received Command: " + command);
		
		String commandType = (String) jsonCommand.get("commandType");
		System.out.println("[Command Handler] Command Type: " + commandType);
		
		
		switch (commandType) {
		
		case "CREATE_SOURCE":
			createSource(jsonCommand);
		case "DESTROY_SOURCE":
			break;
		case "ALTER_SOURCE":
			break;
		case "ACTIVE_SOURCE":
			break;
		default:
			System.out.println("[Source Handler] Command is not exist.");
			break;
		
			
		}		
	}
	
	public void createSource(JSONObject sourceInfo) {
		
		String sourceName = (String) sourceInfo.get("sourceName") ;
		String createTime = (String) sourceInfo.get("createTime");
		String status = (String) sourceInfo.get("status");
		String owner = (String) sourceInfo.get("owner");
		String type = (String) sourceInfo.get("type");
		
		switch(type) {
		
		case "Kafka":
			Source source = new KafkaSource(sourceName, owner, createTime, type, "ip", "port", "topic-in", "topic-out");
			sources.add(source);
			break;
			
		case "Database":
			break;
			
		case "Custom":
			break;
			
		default:
			System.out.println("[Source Handler] Source Type Error.");
			break;
		}		
	}
	
	public void destroySource(String sourceKey) {
		sources.remove(sources.get(sourceKey));		
	}
	
	public void alterSource() { }
	
	public void activeSource() { }
	
	public void deactiveSource() { }
}
