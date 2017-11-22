package knu.cs.dke.topology_manager_v3.destinations;

public abstract class Destination implements Runnable {
	
	// Destination Info.
	private String destinationName;
	private String createdTime;
	private String modifiedTime;
	private String status;
	
	private String owner;
	
	private String destinationType;	
	private String transTopic;
	
	
	public Destination(String destinationName, String createdTime, String owner, String dstType) {
				
		this.destinationName = destinationName;
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.status = "DEACTIVE";
		this.owner = owner;
		this.destinationType = dstType;
		this.transTopic = owner + "-" + destinationName;
	}

	public String getDestinationName() {
		return destinationName;
	}


	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
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


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getTransTopic() {
		return transTopic;
	}


	public void setTransTopic(String transTopic) {
		this.transTopic = transTopic;
	}


	public String getDestinationType() {
		return destinationType;
	}


	public void setDestinationType(String destinationType) {
		this.destinationType = destinationType;
	}


	public String getOwner() {
		return owner;
	}


	public void setOwner(String owner) {
		this.owner = owner;
	}
}
