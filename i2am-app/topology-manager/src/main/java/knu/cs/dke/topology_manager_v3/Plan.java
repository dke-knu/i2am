package knu.cs.dke.topology_manager_v3;

import java.util.List;

import knu.cs.dke.topology_manager_v3.destinations.Destination;
import knu.cs.dke.topology_manager_v3.sources.Source;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;

public class Plan {
	
	private String planID;
	private String owner;
	private String timestamp;
	private List<ASamplingFilteringTopology> lTopologies; // 플랜은 여러 토폴로지로 구성
		
	// ??
	private String sourceKey;
	private String destinationKey;	
	
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
	
	public void setSourceKey(String source) {
		this.sourceKey = source;
	}	
	public String getSourceKey() {
		return this.sourceKey;
	}
	
	public void setDestinationKey(String destination) {
		this.destinationKey = destination;
	}
	
	public String getDestinationKey() {
		return this.destinationKey;
	}
	
	public void submitTopologies() {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.submitTopology();
		}
	}
	
	public void killTopologies() {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.killTopology();
		}		
	}
	
	public void readSource() { }
	
}
