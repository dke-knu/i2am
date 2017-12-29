package knu.cs.dke.topology_manager_v3;

import java.util.HashMap;
import java.util.Map;

public class PlanList {
	
	// singleton - 플랜리스트 > 플랜 > 토폴로지
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
		if (mPlans.containsKey(plan.getPlanName())) return false;
		mPlans.put(plan.getPlanName(), plan);
		return true;
	}
	
	public synchronized boolean remove(Plan plan) {
		if (!mPlans.containsKey(plan.getPlanName())) return false;
		mPlans.remove(plan.getPlanName());
		return true;
	}
	
	public synchronized int size() {
		return mPlans.size();
	}
	
	public synchronized boolean set(Plan changedPlan) {		
		// 값이 있으면 Update, 없으면 Add 됨..
		if (!mPlans.containsKey(changedPlan.getPlanName())) return false;
		mPlans.put(changedPlan.getPlanName(), changedPlan);		
		return true;
	}
}
