package knu.cs.dke.topology_manager_v2;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CommandHandler {
	private PlanList plans = null;

	public CommandHandler(PlanList plans) {
		this.plans = plans;
	}

	public String executeCommand(String command) throws ParseException {
		// TODO Auto-generated method stub
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) jsonParser.parse(command);
		
		// for debugging
		String commandType = "create-plan";
		Plan plan = null;
		DbAdapter.getInstance().set("key", "value");
		
		if ("create-plan".equals(commandType)) {
			createPlan(plan);
		} else if ("kill-plan".equals(commandType)) {
			destroyPlan(plan);
		}
		
		return new String();
	}

	private void createPlan(Plan plan) {
		// TODO Auto-generated method stub
		plan.submitTopologies();
		
		plans.add(plan);
	}
	
	private void destroyPlan(Plan plan) {
		// TODO Auto-generated method stub
		plan.killTopologies();
		
		plans.remove(plan);
	}
}
