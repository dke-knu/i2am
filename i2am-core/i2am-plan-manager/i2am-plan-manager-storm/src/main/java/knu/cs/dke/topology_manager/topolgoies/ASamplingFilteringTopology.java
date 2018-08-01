package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;
import java.util.UUID;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public abstract class ASamplingFilteringTopology {
		
	private String createdTime;
	private String modifiedTime;
	private String status; // enum;
	
	private int index; // 플랜 내의 인덱스
	private String topologyType; // enum
	private String plan; // 어느 플랜에 소속되었는지
		
	private String inputTopic; // 첫 토폴로지는 Source의 정보를 읽어서 셋
	private String outputTopic; // 마지막 토폴로지는 Destination의 정보를 읽어서 셋
		
	private String topologyName;	
	private String redisKey; 	
	
	private RemoteStormController storm;
	
	public ASamplingFilteringTopology(String createdTime, String plan, int index, String topologyType) {
			
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.status = "DEACTIVE";
		this.index = index;
		this.topologyType = topologyType;
		this.plan = plan;
		
		this.topologyName = plan + "-" + UUID.randomUUID().toString();		
		this.inputTopic = UUID.randomUUID().toString();
		this.outputTopic = UUID.randomUUID().toString();		
		this.redisKey = topologyName + "-redis";
		
		try {
			storm = new RemoteStormController();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
	
	public void submitTopology() throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {	
				
		storm.runTopology(this);		
	}
	
	public void killTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException {
	
		storm.killTopology(this.topologyName);
	}
	
	public void avtivateTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException, IOException {
	
		if(!storm.isSubmitted(this.topologyName)) {
			this.submitTopology();
		}
		else {
			this.status = "ACTIVE";
			storm.activateTopology(this.topologyName);
		}
	}
	
	public void deactivateTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException {
			
		this.status = "DEACTIVE";
		storm.deactivateTopology(this.topologyName);
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
