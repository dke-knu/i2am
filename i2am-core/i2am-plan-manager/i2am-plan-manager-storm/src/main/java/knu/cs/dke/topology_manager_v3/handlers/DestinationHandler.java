package knu.cs.dke.topology_manager_v3.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager_v3.DestinationList;
import knu.cs.dke.topology_manager_v3.destinations.KafkaDestination;

public class DestinationHandler {

	private DestinationList destinations;
	private JSONObject command;	

	public DestinationHandler(DestinationList destinations, String command) throws ParseException {

		this.destinations = destinations;

		JSONParser jsonParser = new JSONParser();
		this.command = (JSONObject) jsonParser.parse(command);
	}

	public void excute() {

		String commandType = (String) command.get("commandType");
		System.out.println("[Source Handler] Command Type: " + commandType);

		switch (commandType) {

		case "CREATE_DST":
			createDestination();
			break;
		case "DESTROY_DST":
			break;
		case "ALTER_DST":
			break;
		case "ACTIVE_DST":
			break;
		default:
			System.out.println("[Source Handler] Command is not exist.");
			break;			
		}		
	}

	public void createDestination() {

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");

		// Content Basic info.
		String owner = (String) content.get("owner");
		String dstName = (String) content.get("dstName");
		String createdTime = (String) content.get("createdTime");

		String destinationType = (String) content.get("dstType");

		switch(destinationType) {

		case "KAFKA":		
			JSONObject kafka = (JSONObject) content.get("kafkaParams");
			String ip = (String) kafka.get("zookeeperIp");
			String port = (String) kafka.get("zookeeperPort");
			String topic = (String) kafka.get("topic");
			
			KafkaDestination destination = new KafkaDestination(dstName, createdTime, owner, destinationType, ip, port, topic);
			
			destinations.add(destination);
			
			DbAdapter db = new DbAdapter();
			db.addDestination(destination);
			
			break;
			
		default :
			break;
		}
	}

	public void destroyDestination() { }

	public void alterDestination() { }	

}
