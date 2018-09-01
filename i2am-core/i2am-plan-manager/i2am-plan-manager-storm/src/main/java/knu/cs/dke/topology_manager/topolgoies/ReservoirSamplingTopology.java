package knu.cs.dke.topology_manager.topolgoies;

public class ReservoirSamplingTopology extends ASamplingFilteringTopology {

	private int sampleSize;
	private int windowSize;
	
	public ReservoirSamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize) {

		super(createdTime, plan, index, topologyType);
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;	
	}	

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}
}
