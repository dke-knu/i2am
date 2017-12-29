package knu.cs.dke.topology_manager_v3.topolgoies;

import java.io.IOException;
import java.util.UUID;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;

public abstract class ASamplingFilteringTopology {
		
	private String createdTime;
	private String modifiedTime;
	private String status; // enum;
	
	private int index; //	
	private String topologyType; // enum
	private String plan; // 어느 플랜에 소속되었는지
		
	private String inputTopic; // 첫 토폴로지는 Source의 정보를 읽어서 셋
	private String outputTopic; // 마지막 토폴로지는 Destination의 정보를 읽어서 셋
		
	private String topologyName;	
	private String redisKey; 
	
	public abstract void submitTopology() throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException;
	public abstract void killTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException;
	public abstract void avtivateTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException;
	public abstract void deactivateTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException;	
	
	public ASamplingFilteringTopology(String createdTime, String plan, int index, String topologyType) {
	
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.status = "DEACTIVE";
		this.index = index;
		this.topologyType = topologyType;
		this.plan = plan;
		
		this.topologyName = plan + "-" + UUID.randomUUID().toString();		
		this.inputTopic = topologyName + "-input";
		this.outputTopic = topologyName + "-output";		
		this.redisKey = topologyName + "-redis";
		
	}	
	public String getModifiedTime() {
		return modifiedTime;
	}
	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	public String getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getTopologyType() {
		return topologyType;
	}
	public void setTopologyType(String topologyType) {
		this.topologyType = topologyType;
	}
	public String getPlan() {
		return plan;
	}
	public void setPlan(String plan) {
		this.plan = plan;
	}
	public String getInputTopic() {
		return inputTopic;
	}
	public void setInputTopic(String inputTopic) {
		this.inputTopic = inputTopic;
	}
	public String getOutputTopic() {
		return outputTopic;
	}
	public void setOutputTopic(String outputTopic) {
		this.outputTopic = outputTopic;
	}
	public String getTopologyName() {
		return topologyName;
	}
	public void setTopologyName(String topologyName) {
		this.topologyName = topologyName;
	}
	public String getRedisKey() {
		return redisKey;
	}
	public void setRedisKey(String redisKey) {
		this.redisKey = redisKey;
	}	
}
