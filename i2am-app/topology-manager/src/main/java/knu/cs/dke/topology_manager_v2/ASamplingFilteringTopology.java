package knu.cs.dke.topology_manager_v2;

public abstract class ASamplingFilteringTopology {
	private String topologyID;
	public String getTopologyID() {
		return topologyID;
	}
	public void setTopologyID(String topologyID) {
		this.topologyID = topologyID;
	}
	
	public void setInputKafkaInfo(String ip, short port, String topic) {
		// TODO Auto-generated method stub
		
	}
	public void setOutputKafkaInfo(String ip, short port, String topic) {
		// TODO Auto-generated method stub
		
	}
	public abstract void submitTopology();
	public abstract void killTopology();
}
