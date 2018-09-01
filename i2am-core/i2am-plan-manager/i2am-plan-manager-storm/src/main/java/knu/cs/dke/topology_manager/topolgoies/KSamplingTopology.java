package knu.cs.dke.topology_manager.topolgoies;

public class KSamplingTopology extends ASamplingFilteringTopology {

	private int samplingRate;
	
	public KSamplingTopology(String createdTime, String plan, int index, String topologyType, int samplingRate) {

		super(createdTime, plan, index, topologyType);		
		this.samplingRate = samplingRate;		
	}
	
	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}
}
