package knu.cs.dke.topology_manager_v3.sources;

public abstract class Source {
	
	// Source Info.
	private String sourceID; // NAME	
	private String owner;	
	private String createdTime;
	private String modifiedTime;
	private boolean status = false;	// 데이터 베이스에서 상태를 읽어 DeActive면 멈추어야 한돠...
	private String sourceType; // CUSTOM, KAFKA, DATABASE
	
	private String hostIp; // Kafka는 주키퍼
	private String hostPort;	
	
	// Recommend engine.
	// private boolean recommend = false;	
	// private boolean switchMessaging = false;
	// private String testData;
	
	// System topic;	
	private String systemTopic;
		
	public Source(String ID, String owner, String createTime, String sourceType, String ip, String port, String topic) {

		this.sourceID = ID;
		this.owner = owner;
		this.createdTime = createTime;
		this.modifiedTime = createTime;
		this.sourceType = sourceType;
		
		this.status = false;
		
		this.hostIp = ip;
		this.hostPort = port;	
		
		this.systemTopic = topic;
	}	
	
	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}
	public String getSourceID() {
		return this.sourceID;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return owner;
	}
	
	public void setTimeStamp(String timeStamp) {
		this.createdTime = timeStamp;
	}
	public String getTimeStamp() {
		return this.createdTime;
	}
	
	public void setModifiedTime(String timeStamp) {
		this.modifiedTime = timeStamp;
	}
	public String getModifiedTime() {
		return this.modifiedTime;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
	public boolean getStatus() {
		return this.status;
	}
	
	public void setSourceType(String type) {
		this.sourceType = type;
	}
	public String getSourceType() {
		return this.sourceType;
	}
	
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
	public String getHostIp() {
		return this.hostIp;
	}
	
	public void setHostPort(String port) {
		this.hostPort = port;
	}
	public String getHostPort() {
		return this.hostPort;
	}
	
	public void setWriteTopic(String topic) {
		this.systemTopic = topic;
	}
	public String getWriteTopic() {
		return this.systemTopic;
	}
	
	public abstract void read(); // 읽어서 우리 시스템의 Kafka로 !!	
	
}
