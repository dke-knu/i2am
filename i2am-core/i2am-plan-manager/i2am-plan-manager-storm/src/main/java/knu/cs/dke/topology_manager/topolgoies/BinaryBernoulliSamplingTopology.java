package knu.cs.dke.topology_manager.topolgoies;

public class BinaryBernoulliSamplingTopology extends ASamplingFilteringTopology {

	private int sampleSize;
	private int windowSize;
	
	private String preSampleKey;
	
	public BinaryBernoulliSamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize) {
		
		super(createdTime, plan, index, topologyType);
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;				
		
		this.preSampleKey = super.getRedisKey() + "preSample";		
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

	public String getPreSampleKey() {
		return preSampleKey;
	}

	public void setPreSampleKey(String preSampleKey) {
		this.preSampleKey = preSampleKey;
	}
}
