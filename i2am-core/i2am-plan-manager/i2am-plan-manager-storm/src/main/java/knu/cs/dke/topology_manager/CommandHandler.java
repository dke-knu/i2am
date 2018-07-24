package knu.cs.dke.topology_manager;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager.handlers.DestinationHandler;
import knu.cs.dke.topology_manager.handlers.SourceHandler;
import knu.cs.dke.topology_manager.handlers.TopologyHandler;

public class CommandHandler {

	private PlanList plans = null;
	private SourceList sources = null;
	private DestinationList destinations = null;
	
	public CommandHandler(PlanList plans, SourceList sources, DestinationList destinations) {
		this.plans = plans;
		this.sources = sources; 
		this.destinations = destinations;
	}

	public String executeCommand(String input_command) throws ParseException, NotAliveException, AuthorizationException, TException, InterruptedException, IOException {

		// Json에서 Command Type만 확인하여 해당 명령어를 호출하면서 Json 넘겨버리기!!
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) jsonParser.parse(input_command);		
		System.out.println("[Command Handler] Received Command: " + input_command);
		
		String commandType = (String) jsonCommand.get("commandType");
		System.out.println("[Command Handler] Command Type: " + commandType);
				
		switch (commandType) 
		{
		
		// Command for Call Topology Handler !
		case "CREATE_PLAN":
		case "DESTROY_PLAN":
		case "CHANGE_STATUS_OF_PLAN":
		case "EDIT_PLAN": // To-Do
			TopologyHandler th = new TopologyHandler(input_command, plans, sources, destinations);
			th.excute();
			break;	
	
		// Command for Call Source Handler !!	
		case "CREATE_SRC":			
		case "DESTROY_SRC": 	
		case "CHANGE_STATUS_OF_SRC":
		case "EDIT_SOURCE": // To-Do			
			SourceHandler sh = new SourceHandler(sources, input_command);
			sh.excute();			
			break;
			
		// Command for Call Destination Handler !!!
		case "CREATE_DST":			
		case "DESTROY_DST":
		case "CHANGE_STATUS_OF_DST":
		case "EDIT_DESTINATION":
			DestinationHandler dh = new DestinationHandler(destinations, input_command);
			dh.excute();			
			break;
			
		// Command for Intelligent Engine !!!!
//		case "new-algorithm":
//			SourceHandler na = new SourceHandler(sources, input_command);
//			na.updateRecommendation();
//			break;
			
		// Command is not identified !!!!!
		default:
			System.out.println("[Command Handler] Command type does not exist.");
			break;			
		}		
		
		return new String();
	}	
}
