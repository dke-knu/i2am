package knu.cs.dke.topology_manager_v3.topolgoies;

public class SystematicSamplingTopology extends ASamplingFilteringTopology {

	private int sampleSize;
	private int windowSize;
	
	public SystematicSamplingTopology(int sampleSize, int windowSize) {
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;
	}
	
	@Override
	public void submitTopology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void killTopology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activeTopology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactiveTopology() {
		// TODO Auto-generated method stub
		
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

}
