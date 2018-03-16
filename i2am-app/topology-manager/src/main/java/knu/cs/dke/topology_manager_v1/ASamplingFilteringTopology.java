package knu.cs.dke.topology_manager_v1;

public abstract class ASamplingFilteringTopology {
	public void setInputKafkaInfo(String ip, short port, String topic) {
		// TODO Auto-generated method stub
		
	}
	public void setOutputKafkaInfo(String ip, short port, String topic) {
		// TODO Auto-generated method stub
		
	}
	public abstract void submitTopology();
	public abstract void killTopology();
}
