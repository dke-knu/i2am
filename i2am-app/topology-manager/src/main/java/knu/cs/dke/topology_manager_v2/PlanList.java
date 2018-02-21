package knu.cs.dke.topology_manager_v2;

import java.util.HashMap;
import java.util.Map;

public class PlanList {	
	// singleton
	private volatile static PlanList instance;
	public static PlanList getInstance() {
		if(instance == null) {
			synchronized(PlanList.class) {
				if(instance == null) {
					instance = new PlanList();
				}
			}
		}
		return instance;
	}

	private Map<String, Plan> mPlans;

	private PlanList() {
		mPlans = new HashMap<String, Plan>();
	}
	
	public synchronized Plan get(String planID) {
		return mPlans.get(planID);
	}
	
	public synchronized boolean add(Plan plan) {
		if (mPlans.containsKey(plan.getPlanID())) return false;
		mPlans.put(plan.getPlanID(), plan);
		return true;
	}
	
	public synchronized boolean remove(Plan plan) {
		if (mPlans.containsKey(plan.getPlanID())) return false;
		mPlans.remove(plan.getPlanID());
		return true;
	}
	
	public synchronized int size() {
		return mPlans.size();
	}
}
