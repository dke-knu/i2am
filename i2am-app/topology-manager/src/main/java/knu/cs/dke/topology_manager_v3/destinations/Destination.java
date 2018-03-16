package knu.cs.dke.topology_manager_v3.destinations;

public abstract class Destination {
	
	// Destination Info.
	private String destinationID; // NAME
	private String owner;
	private String timeStamp;
	
	private String hostIp;
	private String hostPort;		
	
	// System Kafka Info.
	private String systemTopic;
	
	public void setSourceID(String destinationID) {
		this.destinationID = destinationID;
	}
	public String getSourceID() {
		return this.destinationID;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return owner;
	}
	
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getTimeStamp() {
		return this.timeStamp;
	}
	
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
	public void setHostPort(String port) {
		this.hostPort = port;
	}
	
	public void setTopic(String topic) {
		this.systemTopic = topic;
	}
	public String getTopic(String topic) {
		return this.systemTopic;
	}
	
	public abstract void write(); //우리 카프카프카에서 유저의 데스티네이션으루
}
