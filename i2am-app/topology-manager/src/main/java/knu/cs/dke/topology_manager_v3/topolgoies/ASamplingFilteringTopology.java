package knu.cs.dke.topology_manager_v3.topolgoies;

import org.apache.storm.thrift.transport.TTransportException;

public abstract class ASamplingFilteringTopology {
		
	private String topologyID;
	// private int index; // List로 관리되기 때문에 ID로 충분할 것 같음 Topic 규칙에 사용?
	private String plan; // 어느 플랜에 소속되었는지
	
	private String kafkaInputTopic; // 첫 토폴로지는 Source의 정보를 읽어서 셋
	private String kafkaoutputTopic; // 마지막 토폴로지는 Destination의 정보를 읽어서 셋
		
	public abstract void submitTopology(); // [ACTIVATE_PLAN] Start Topology
	public abstract void killTopology(); // [DESTROY_PLAN] Stop Topology
	public abstract void activeTopology();
	public abstract void deactiveTopology();
	
	/*
	public ASamplingFilteringTopology(String plan, String inputTopic, String outputTopic) {
	
		this.plan = plan;
		this.kafkaInputTopic = inputTopic;
		this.kafkaoutputTopic = outputTopic;		
	}	
	*/
	
	public String getTopologyID() {
		return topologyID;
	}
	
	public void setTopologyID(String topologyID) {
		this.topologyID = topologyID;
	}	
	
	// Kafka Info는 Redis에 저장되고 주기적으로 확인할 수 있어야하므로 set & get이 필요하나?
	public void setInputKafkaInfo(String ip, short port, String topic) {
		this.setKafkaInputTopic(topic);		
	}
	
	public void setOutputKafkaInfo(String ip, short port, String topic) {
		this.setKafkaoutputTopic(topic);	
	}
	
	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getKafkaInputTopic() {
		return kafkaInputTopic;
	}

	public void setKafkaInputTopic(String kafkaInputTopic) {
		this.kafkaInputTopic = kafkaInputTopic;
	}

	public String getKafkaoutputTopic() {
		return kafkaoutputTopic;
	}

	public void setKafkaoutputTopic(String kafkaoutputTopic) {
		this.kafkaoutputTopic = kafkaoutputTopic;
	}
}
