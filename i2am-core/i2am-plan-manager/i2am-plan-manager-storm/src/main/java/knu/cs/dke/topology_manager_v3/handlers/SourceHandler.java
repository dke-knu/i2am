package knu.cs.dke.topology_manager_v3.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager_v3.SourceList;
import knu.cs.dke.topology_manager_v3.sources.KafkaSource;
import knu.cs.dke.topology_manager_v3.sources.Source;

public class SourceHandler {

	private SourceList sources;	
	private JSONObject command;
	
	public SourceHandler(SourceList sources, String command) throws ParseException {
		
		this.sources = sources;
		JSONParser jsonParser = new JSONParser();
		this.command = (JSONObject) jsonParser.parse(command);			
	}
	
	public void excute() throws ParseException {		
		
		String commandType = (String) command.get("commandType");
		System.out.println("[Source Handler] Command Type: " + commandType);
				
		switch (commandType) {
		
		case "CREATE_SRC":
			createSource();
			break;
		case "DESTROY_SRC":
			break;
		case "ALTER_SRC":
			break;
		case "ACTIVE_SRC":
			break;
		default:
			System.out.println("[Source Handler] Command is not exist.");
			break;			
		}		
	}
	
	public void createSource() {
				
		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		
		// Content Basic Info. 
		String owner = (String) content.get("owner");
		String srcName = (String) content.get("srcName");
		String createdTime = (String) content.get("createdTime");
		String intelliEngine = (String) content.get("usesIntelligentEngine");
		
		String testData = (String) content.get("testData");		
		
		// Source Type.
		String sourceType = (String) content.get("srcType");
				
		switch(sourceType) {
		
		case "kafka":			
			JSONObject kafka = (JSONObject) content.get("kafkaParams");
			String ip = (String) kafka.get("zookeeperIp");
			String port = (String) kafka.get("zookeeperPort");
			String topic = (String) kafka.get("topic");			
			KafkaSource source = new KafkaSource(srcName, createdTime, owner, intelliEngine, testData, sourceType, "N", ip, port, topic);
			
			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter db = new DbAdapter();
			db.addSource(source);			
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
