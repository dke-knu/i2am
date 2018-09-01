package knu.cs.dke.topology_manager.topolgoies;

public class HashSamplingTopology extends ASamplingFilteringTopology{
		
	private int sampleSize;
	private int windowSize;
	private int target;
	
	public HashSamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize, int target) {

		super(createdTime, plan, index, topologyType);
		
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;	
		this.target = target;		
	}
	
	public int getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}		
}
