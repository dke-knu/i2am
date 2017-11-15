package knu.cs.dke.topology_manager_v3.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager_v3.DestinationList;
import knu.cs.dke.topology_manager_v3.Plan;
import knu.cs.dke.topology_manager_v3.PlanList;
import knu.cs.dke.topology_manager_v3.SourceList;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.HashSamplingTopology;

public class TopologyHandler {

	private JSONObject command;
	private PlanList plans;
	private SourceList sources;
	private DestinationList destination;	

	public TopologyHandler(String command, PlanList plans, SourceList sources, DestinationList destinations) throws ParseException {		
		
		JSONParser jsonParser = new JSONParser();
		this.command = (JSONObject) jsonParser.parse(command);		
		this.plans = plans;
		this.sources = sources; 
		this.destination = destinations;		
	}

	public void excute() throws ParseException {		

		String commandType = (String) command.get("commandType");
		System.out.println("[Topology Handler] Command Type: " + commandType);

		switch (commandType) {

		case "CREATE_PLAN":
			createPlan();
			break;
		case "DESTROY_PLAN":
			break;
		case "ALTER_PLAN":
			break;
		case "ACTIVE_PLAN":
			break;
		default:
			System.out.println("[Topology Handler] Command is not exist.");
			break;			
		}		
	}

	private void createPlan() {		
		
		// Content
		JSONObject content = (JSONObject) command.get("commandContent");		
		
		String owner = (String) content.get("owner");
		String planName = (String) content.get("planName");
		String source = (String) content.get("srcName");
		String destination = (String) content.get("dstName");
		String createdTime = (String) content.get("createdTime");
				
		JSONArray algorithms = (JSONArray) content.get("algorithms");
		List<ASamplingFilteringTopology> topologies = null;		
		
		for( int i=0; i < algorithms.size(); i++ ) {
			
			JSONObject algorithm = (JSONObject) algorithms.get(i);
			
			String type = (String) algorithm.get("algorithmType");
			
			switch(type) {
			
			case "BINARY_BERNOULLI_SAMPLING":
				break;
			case "HASH_SAMPLING":
				break;
			case "PRIORITY_SAMPLING":
				break;
			case "RESERVOIR_SAMPLING":
				break;
			case "STRATIFIED_SAMPLING":
				break;
			case "SYSTEMATIC_SAMPLING":
				break;				
			default:
				System.out.println("[Topology Hander] Algoritm Type Error");
				break;
			
			}			 
		}		
		
	}

	private void destroyPlan(Plan plan) {
		// TODO Auto-generated method stub
		plan.killTopologies();		
		plans.remove(plan);
	}
}
