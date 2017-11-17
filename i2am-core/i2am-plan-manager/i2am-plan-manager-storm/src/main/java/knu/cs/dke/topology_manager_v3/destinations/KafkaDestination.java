package knu.cs.dke.topology_manager_v3.destinations;

public class KafkaDestination extends Destination {

	private String topic;	
	private String zookeeperIp;
	private String zookeeperPort;
	
	public KafkaDestination(String destinationName, String createdTime, String owner, String dstType,
			String zookeeperIp, String zookeeperPort, String topic ) {
		
		super(destinationName, createdTime, owner, dstType);
		
		this.zookeeperIp = zookeeperIp;
		this.zookeeperPort = zookeeperPort;
		this.topic = topic;	
	}	
	
	@Override
	public void write() {
		// TODO Auto-generated method stub		
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getZookeeperIp() {
		return zookeeperIp;
	}

	public void setZookeeperIp(String zookeeperIp) {
		this.zookeeperIp = zookeeperIp;
	}

	public String getZookeeperPort() {
		return zookeeperPort;
	}

	public void setZookeeperPort(String zookeeperPort) {
		this.zookeeperPort = zookeeperPort;
	}

}
