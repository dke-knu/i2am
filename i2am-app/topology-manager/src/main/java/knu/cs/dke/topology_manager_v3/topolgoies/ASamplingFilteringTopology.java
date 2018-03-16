package knu.cs.dke.topology_manager_v3.topolgoies;

public abstract class ASamplingFilteringTopology {
		
	private String topologyID;
	// private int index; // List로 관리되기 때문에 ID로 충분할 것 같음
	private String plan; // 어느 플랜에 소속되었는지?? 
	private String kafkaInputTopic;
	private String kafkaoutputTopic;	
	
	public String getTopologyID() {
		return topologyID;
	}
	
	public void setTopologyID(String topologyID) {
		this.topologyID = topologyID;
	}	
	
	// Kafka Info는 Redis에 저장되고 주기적으로 확인할 수 있어야하므로 set & get이 필요하나?
	public void setInputKafkaInfo(String ip, short port, String topic) {
		this.kafkaInputTopic = topic;		
	}
	
	public void setOutputKafkaInfo(String ip, short port, String topic) {
		this.kafkaoutputTopic = topic;	
	}
	
	public abstract void submitTopology(); // [ACTIVATE_PLAN] Start Topology
	public abstract void killTopology(); // [DESTROY_PLAN] Stop Topology
	public abstract void activeTopology();
	public abstract void deactiveTopology();

	
}
