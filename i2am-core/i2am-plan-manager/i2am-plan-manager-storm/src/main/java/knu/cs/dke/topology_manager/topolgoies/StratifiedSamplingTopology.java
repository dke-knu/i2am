package knu.cs.dke.topology_manager_v3.topolgoies;

public class StratifiedSamplingTopology extends ASamplingFilteringTopology {

	private int sampleSize;
	private int windowSize;
	
	public StratifiedSamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize) {

		super(createdTime, plan, index, topologyType);
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;				
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

	@Override
	public void killTopology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void avtivateTopology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivateTopology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitTopology() {
		// TODO Auto-generated method stub
		
	}

}
