package knu.cs.dke.topology_manager;

import java.io.IOException;
import java.util.List;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;

import knu.cs.dke.topology_manager.topolgoies.ASamplingFilteringTopology;

public class Plan {

	private String planName;	
	private String createdTime;
	private String modifiedTime;
	private String status;	
	private String owner;
	private String source;
	private String destination;
	
	private List<ASamplingFilteringTopology> lTopologies; // 플랜은 여러 토폴로지로 구성
	private boolean submitted; // Storm에 Submit 되어있는지 여부. 
	
	public Plan(String name, String createdTime, String status, String owner, String source, String destination) {

		this.planName = name;
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.status = status;
		this.owner = owner;
		this.source = source;
		this.destination = destination;		
		this.lTopologies = null;
		this.submitted = false; 
	}	

	public void submitTopologies() throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.submitTopology();
			
		}
		submitted = true;
	}

	public void killTopologies() throws NotAliveException, AuthorizationException, TException, InterruptedException {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.killTopology();
		}
		submitted = false;
	}

	public void activateTopologies() throws NotAliveException, AuthorizationException, TException, InterruptedException {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.avtivateTopology();
		}		
	}

	public void deactivateTopologies() throws NotAliveException, AuthorizationException, TException, InterruptedException {
		// TODO Auto-generated method stub
		if (lTopologies == null || lTopologies.isEmpty()) return;
		for (ASamplingFilteringTopology topology: lTopologies) {
			topology.deactivateTopology();
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

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public void setTopologies(List<ASamplingFilteringTopology> topologies) {		
		this.lTopologies = topologies;
	}
	public List<ASamplingFilteringTopology> getTopologies() {
		return this.lTopologies;
	}
}
