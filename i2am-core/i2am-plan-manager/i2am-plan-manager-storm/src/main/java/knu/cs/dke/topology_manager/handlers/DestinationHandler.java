package knu.cs.dke.topology_manager.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager.DestinationList;
import knu.cs.dke.topology_manager.destinations.CustomDestination;
import knu.cs.dke.topology_manager.destinations.DBDestination;
import knu.cs.dke.topology_manager.destinations.Destination;
import knu.cs.dke.topology_manager.destinations.KafkaDestination;
import knu.cs.dke.topology_manager.sources.Source;

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
		System.out.println("[Destination Handler] Command Type: " + commandType);

		switch (commandType) {

		case "CREATE_DST":
			this.createDestination();
			break;

		case "CHANGE_STATUS_OF_DST":				
			JSONObject content = (JSONObject) command.get("commandContent");			
			String after = (String) content.get("after");

			if (after.equals("ACTIVE")) this.activeDestination();
			else if (after.equals("DEACTIVE")) this.deactiveDestination();
			break;

		case "DESTROY_DST":
			this.destroySource();
			break;

		case "ALTER_DST":
			break;
		
		default:
			System.out.println("[Destination Handler] Command is not exist.");
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

			DbAdapter.getInstance().addDestination(destination);

			break;

		case "DATABASE":
			JSONObject database = (JSONObject) content.get("databaseParams");
			String dbIp = (String) database.get("databaseIp");
			String dbPort = (String) database.get("databasePort");
			String dbId = (String) database.get("databaseId");
			String dbPassword = (String) database.get("databasePw");
			String dbName = (String) database.get("database");
			String dbTable = (String) database.get("table");	
			
			DBDestination db_destination = new DBDestination(dstName, createdTime, owner, destinationType, dbIp, dbPort, dbId, dbPassword, dbName, dbTable);

			destinations.add(db_destination);

			DbAdapter.getInstance().addDestination(db_destination);
			
			break;
			
		case "CUSTOM":
			CustomDestination custom_destination = new CustomDestination(dstName, createdTime, owner, destinationType);
			
			destinations.add(custom_destination);
			
			DbAdapter.getInstance().addDestination(custom_destination);
			
			break;
			
		default :
			break;
		}
		
		DbAdapter.getInstance().addLog(owner, "INFO", "Destination is created." + " (" + dstName + ")");
	}

	public void destroySource() {
		
		// Parse Content!
		JSONObject content = (JSONObject) command.get("commandContent");
				
		// Get Information
		String planName = (String) content.get("dstName");
		String owner = (String) content.get("owner");
				
		// Delete!
		Destination temp = destinations.get(owner, planName);				
		destinations.remove(temp);
			
		DbAdapter.getInstance().removeDestination(temp);
		DbAdapter.getInstance().addLog(owner, "INFO", "Destination is destroyed." + " (" + temp.getDestinationName() + ")");
	}
	public void alterDestination() { }	

	public void activeDestination() {		

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		
		String owner = (String) content.get("owner");
		String name = (String) content.get("dstName");
		

		Destination destination = destinations.get(owner, name);
		destination.setStatus("ACTIVE");		

		DbAdapter.getInstance().changeDestinationStatus(destination);

		destinations.set(destination);
		
		// Thread Start.
		Thread run = new Thread(destination, destination.getDestinationName());
		destinations.addThread(run);
		run.start();
		
		System.out.println("[Destination Handler] " + destination.getName() + " is Started!");	
		DbAdapter.getInstance().addLog(owner, "INFO", "Destination is activated." + " (" + name + ")");
	}

	public void deactiveDestination() {

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");		
		String owner = (String) content.get("owner");
		String name = (String) content.get("dstName");

		Destination destination = destinations.get(owner, name);
		destination.setStatus("DEACTIVE");		
		
		DbAdapter.getInstance().changeDestinationStatus(destination);

		destinations.set(destination);
		
		Thread run = destinations.getThread(destination.getDestinationName());
		run.interrupt();
		
		System.out.println("[Destination Handler]" + destination.getName() + " is stopped! - " + destination.isAlive() );
		DbAdapter.getInstance().addLog(owner, "INFO", "Destination is deactivated." + " (" + name + ")");
	}
}
