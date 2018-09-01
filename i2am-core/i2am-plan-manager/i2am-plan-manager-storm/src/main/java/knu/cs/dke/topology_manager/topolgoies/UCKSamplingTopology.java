package knu.cs.dke.topology_manager.topolgoies;

public class UCKSamplingTopology extends ASamplingFilteringTopology {

	private int samplingRate;
	private double ucUnderBound;
	
	public UCKSamplingTopology(String createdTime, String plan, int index, String topologyType, int samplingRate, double uc) {

		super(createdTime, plan, index, topologyType);

		this.samplingRate = samplingRate;
		this.ucUnderBound = uc;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}

	public double getUcUnderBound() {
		return ucUnderBound;
	}

	public void setUcUnderBound(double ucUnderBound) {
		this.ucUnderBound = ucUnderBound;
	}

}
