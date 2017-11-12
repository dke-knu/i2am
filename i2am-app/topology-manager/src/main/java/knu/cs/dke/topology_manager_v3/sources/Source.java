package knu.cs.dke.topology_manager_v3.sources;

public class Source {
	
	private String sourceID;	
	private String owner;
	private String timeStamp;
	
	private String hostIp;
	private String hostPort;		
	
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
	
	public void read() {}
}
