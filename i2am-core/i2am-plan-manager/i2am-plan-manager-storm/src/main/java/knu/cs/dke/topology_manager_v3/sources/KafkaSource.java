package knu.cs.dke.topology_manager_v3.sources;

public class KafkaSource extends Source {
	
	private String zookeeperIp;
	private String zookeeperPort;
	private String topic;
	
	// Create Kafka Source
	public KafkaSource (String sourceName, String createdTime, String owner, String useIntelliEngine, String testData,
			String srcType, String switchMessaging, String zookeeperIp, String zookeeperPort, String topic) {		
		super(sourceName, createdTime, owner, useIntelliEngine, testData, srcType, switchMessaging);		
		this.setZookeeperIp(zookeeperIp);
		this.setZookeeperPort(zookeeperPort);
		this.topic = topic;		
	}
	
	public void read() {		
		
		
		
	}
	
	public void setTopic(String topic) {		
		this.topic = topic;
	}
	
	public String getTopic() { 
		return this.topic;
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
