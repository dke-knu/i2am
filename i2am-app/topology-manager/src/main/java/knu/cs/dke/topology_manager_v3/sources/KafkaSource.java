package knu.cs.dke.topology_manager_v3.sources;

public class KafkaSource extends Source {

	private String topic;

	// Create Kafka Source
	public KafkaSource(String ID, String owner, String createTime, String sourceType, String ip, String port,
			String sourceTopic, String systemTopic) {
		
		super(ID, owner, createTime, sourceType, ip, port, systemTopic);
		this.topic = sourceTopic;
	}
	
	public void read() {
		// TODO Auto-generated method stub
		
	}
	
	public void setTopic(String topic) {		
		this.topic = topic;
	}
	
	public String getTopic() { 
		return this.topic;
	}
	
}
