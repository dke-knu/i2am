package knu.cs.dke.topology_manager_v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CommandHandler implements Runnable {
	private Queue<String> qCommands = null;
	private List<Plan> lPlans = null;

	public CommandHandler(Queue<String> qCommands) {
		this.qCommands = qCommands;
		this.lPlans = new ArrayList<Plan>();
	}

	public void run() {
		while (true) {
			// Queue can't be used without sleep.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (qCommands.isEmpty()) continue;
			String command = qCommands.remove();
			try {
				executeCommand(command);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	private void executeCommand(String command) throws ParseException {
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
	}

	private void createPlan(Plan plan) {
		// TODO Auto-generated method stub
		plan.submitTopologies();
		
		lPlans.add(plan);
	}
	
	private void destroyPlan(Plan plan) {
		// TODO Auto-generated method stub
		plan.killTopologies();
		
		lPlans.remove(plan);
	}
}
