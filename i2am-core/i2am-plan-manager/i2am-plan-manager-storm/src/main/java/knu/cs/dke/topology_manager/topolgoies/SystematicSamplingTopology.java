package knu.cs.dke.topology_manager.topolgoies;

public class SystematicSamplingTopology extends ASamplingFilteringTopology {

	private int interval;
	
	public SystematicSamplingTopology(String createdTime, String plan, int index, String topologyType, int interval) {

		super(createdTime, plan, index, topologyType);
		
		this.interval = interval;
	}

	public int getInterval() {
		return interval;
	}


	public void setInterval(int interval) {
		this.interval = interval;
	}
}
