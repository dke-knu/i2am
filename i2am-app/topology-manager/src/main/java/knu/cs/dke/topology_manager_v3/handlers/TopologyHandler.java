package knu.cs.dke.topology_manager_v3.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import knu.cs.dke.topology_manager_v3.DestinationList;
import knu.cs.dke.topology_manager_v3.Plan;
import knu.cs.dke.topology_manager_v3.PlanList;
import knu.cs.dke.topology_manager_v3.SourceList;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.HashSamplingTopology;

public class TopologyHandler {

	private String command;
	private PlanList plans;
	private SourceList sources;
	private DestinationList destination;	
	
	public TopologyHandler(String command, PlanList plans, SourceList sources, DestinationList destinations) {		
		this.command = command;		
		this.plans = plans;
		this.sources = sources; 
		this.destination = destinations;		
	}
	
	private void createPlan(JSONObject jsonCommand) {		
		
		Plan plan = new Plan();
		
		// Command Info. > 명령엔 필요 없는 정보. 로깅 정도만 해야하남?
		// plan.setPlanID((String) jsonCommand.get("commandId"));
		// plan.setOwner((String) jsonCommand.get("commander"));		
		// plan.setTimestamp((String) jsonCommand.get("commandTime"));
				
		// Topologies[Algorithms] Info.
		JSONObject content = (JSONObject) jsonCommand.get("commandContent");
	
		plan.setPlanID((String) content.get("planId"));
		plan.setOwner((String) content.get("owner"));
		plan.setTimestamp((String) content.get("createTime").toString());
				
		// Algorithms
		JSONArray algorithms = (JSONArray) content.get("algorithms");
		
		List<ASamplingFilteringTopology> topologies = new ArrayList<>();
		
		int algorithmsSize = algorithms.size();
		
		for ( int i=0; i<algorithmsSize; i++ ) {
			
			JSONObject temp = (JSONObject) algorithms.get(i);
			String algorithmType = (String) temp.get("algorithmType");
			
			switch(algorithmType) {
			
			case "HASH_SAMPLING":				
				JSONObject params = (JSONObject) temp.get("algorithmParams");
				Long numberOfBucket = (Long) params.get("numberOfBucket");
				Long selectedBucket = (Long) params.get("selectedBucket");				
				ASamplingFilteringTopology hash = new HashSamplingTopology(numberOfBucket.intValue(), selectedBucket.intValue());
				hash.setTopologyID("planId" + String.valueOf(i));
				topologies.add(hash);
				break;			
			}
		}		
		plan.setlTopologies(topologies);		
		
		if (plans.add(plan)) {
			System.out.println("[Command Handler] Plan is Created!");
		} else {
			System.out.println("[Command Handler] Plan ID is already exist!");
		}
		
		plan.submitTopologies();
		System.out.println("[Command Handler] Plan Start!");		
	}
	
	private void destroyPlan(Plan plan) {
		// TODO Auto-generated method stub
		plan.killTopologies();		
		plans.remove(plan);
	}
}
