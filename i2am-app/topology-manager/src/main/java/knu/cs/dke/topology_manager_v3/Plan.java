package knu.cs.dke.topology_manager_v3;

import java.util.List;

import knu.cs.dke.topology_manager_v3.destinations.Destination;
import knu.cs.dke.topology_manager_v3.sources.Source;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;

public class Plan {

	private String planName;	
	private String createdTime;
	private String modifiedTime;
	private String status;	
	private String owner;
	private String source;
	private String destination;

	private List<ASamplingFilteringTopology> lTopologies; // 플랜은 여러 토폴로지로 구성

	public Plan(String name, String createdTime, String status, String owner, String source, String destination) {

		this.planName = name;
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.status = status;
		this.owner = owner;
		this.source = source;
		this.destination = destination;		
		this.lTopologies = null;
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

	public void activateTopologies() {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.activeTopology();
		}		
	}

	public void deactivateTopologies() {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.deactiveTopology();
		}			
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTopologies(List<ASamplingFilteringTopology> topologies) {		
		this.lTopologies = topologies;
	}
	public List<ASamplingFilteringTopology> getTopologies() {
		return this.lTopologies;
	}
}
