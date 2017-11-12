package knu.cs.dke.topology_manager_v3.sources;

public class KafkaSource extends Source {

	private String topic;
	
	@Override
	public void read() {
		// TODO Auto-generated method stub
		
	}
	
	public void setTopic(String topic) {
		
		this.topic = topic;
	}
	
}
