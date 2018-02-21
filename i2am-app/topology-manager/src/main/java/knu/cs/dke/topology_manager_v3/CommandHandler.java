package knu.cs.dke.topology_manager_v3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager_v3.handlers.SourceHandler;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.HashSamplingTopology;

public class CommandHandler {

	private PlanList plans = null;
	private SourceList sources = null;
	private DestinationList destination = null;
	
	public CommandHandler(PlanList plans, SourceList sources, DestinationList destinations) {
		this.plans = plans;
		this.sources = sources; 
		this.destination = destinations;
	}

	public String executeCommand(String input_command) throws ParseException {

		// Json에서 Command Type만 확인하여 해당 명령어를 호출하면서 Json 넘겨버리기!!
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) jsonParser.parse(input_command);		
		System.out.println("[Command Handler] Received Command: " + input_command);
		
		String commandType = (String) jsonCommand.get("commandType");
		System.out.println("[Command Handler] Command Type: " + commandType);
				
		switch (commandType) 
		{
		case "CREATE_PLAN":			
		case "DESTROY_PLAN":
			// Call Topology Handler
			// Returned Plan.
			break;
			
		case "CREATE_SOURCE":
		case "DESTROY_SOURCE":
			SourceHandler sh = new SourceHandler(sources);
			// SourceHandler sh = new SourceHandler();			
			// Call Source Handler
			break;
			
		case "CREATE_DESTINATION":
		case "DESTROY_DESTINATION":
			// Call Destination Handler
			break;
			
		default:
			System.out.println("[Command Handler] Command type does not exist.");
			break;
			
		}		
		
		return new String();
	}	
}
