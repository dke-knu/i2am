package knu.cs.dke.topology_manager_v3.topolgoies;

public abstract class ASamplingFilteringTopology {
		
	private String createdTime;
	private String modifiedTime;
	private String status; // enum;
	
	private int index; //	
	private String topologyType; // enum
	private String plan; // 어느 플랜에 소속되었는지
		
	private String inputTopic; // 첫 토폴로지는 Source의 정보를 읽어서 셋
	private String outputTopic; // 마지막 토폴로지는 Destination의 정보를 읽어서 셋
		
	public abstract void submitTopology(); // [ACTIVATE_PLAN] Start Topology
	public abstract void killTopology(); // [DESTROY_PLAN] Stop Topology
	public abstract void activeTopology();
	public abstract void deactiveTopology();
		
	public ASamplingFilteringTopology(String createdTime, String plan, int index, String topologyType) {
	
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.status = "DEACTIVE";
		this.index = index;
		this.topologyType = topologyType;
		this.plan = plan;
		
		this.inputTopic = plan + "-input-" + index;
		this.outputTopic = plan + "-output-" + index;		
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
}
