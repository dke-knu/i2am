package knu.cs.dke.topology_manager;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import knu.cs.dke.topology_manager.topolgoies.ASamplingFilteringTopology;

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
	
	public synchronized Plan get(String owner, String planName) {
		
		String planId =  owner + planName;
		
		return mPlans.get(planId);
	}
	
	public synchronized boolean add(Plan plan) {
		
		String planId = plan.getOwner() + plan.getPlanName(); 
		
		if (mPlans.containsKey(planId)) return false;
		mPlans.put(planId, plan);
		return true;
	}
	
	public synchronized boolean remove(Plan plan) {
		
		String planId = plan.getOwner() + plan.getPlanName(); 
		
		if (!mPlans.containsKey(planId)) return false;
		mPlans.remove(planId);
		return true;
	}
	
	public synchronized int size() {
		return mPlans.size();
	}
	
	public synchronized boolean set(Plan changedPlan) {		
		
		String planId = changedPlan.getOwner() + changedPlan.getPlanName(); 
		// 값이 있으면 Update, 없으면 Add 됨..
		if (!mPlans.containsKey(planId)) return false;
		mPlans.put(planId, changedPlan);		
		return true;
	}
	
	public synchronized void printSummary() {
		
		System.out.println("\n[Plan List Summary]\n");
		System.out.println("Map Size: " + mPlans.size() + "\n");
				
		int i = 0;
		for(String key: mPlans.keySet()) {			
			System.out.println("[Plan " + i + "]");
			System.out.println("Plan Name: " + mPlans.get(key).getPlanName());			
			i++;
			
			List<ASamplingFilteringTopology> topologies = mPlans.get(key).getTopologies();
			
			for(int j=0; j<topologies.size(); j++ ) {
							
				System.out.println("[Topology " + j + "]");				
				System.out.println("\t" + topologies.get(j).getPlan());
				System.out.println("\t" + topologies.get(j).getIndex());
				System.out.println("\t" + topologies.get(j).getTopologyType());
				System.out.println("\n");
			}
		}
	}
}
