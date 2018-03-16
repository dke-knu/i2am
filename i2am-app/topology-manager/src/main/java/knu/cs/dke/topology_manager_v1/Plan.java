package knu.cs.dke.topology_manager_v1;

import java.util.List;

public class Plan {
	private String planID;
	private String owner;
	private String timestamp;
	private List<ASamplingFilteringTopology> lTopologies;
	
	public String getPlanID() {
		return planID;
	}
	public void setPlanID(String planID) {
		this.planID = planID;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public List<ASamplingFilteringTopology> getlTopologies() {
		return lTopologies;
	}
	public void setlTopologies(List<ASamplingFilteringTopology> lTopologies) {
		this.lTopologies = lTopologies;
	}
	public void submitTopologies() {
		// TODO Auto-generated method stub
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.submitTopology();
		}
	}
	public void killTopologies() {
		// TODO Auto-generated method stub
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.killTopology();
		}
		
	}
}
