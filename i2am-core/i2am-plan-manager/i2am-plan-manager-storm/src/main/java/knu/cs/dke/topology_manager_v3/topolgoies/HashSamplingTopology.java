package knu.cs.dke.topology_manager_v3.topolgoies;

public class HashSamplingTopology extends ASamplingFilteringTopology{
		
	private int sampleSize;
	private int windowSize;
	private int bucketSize;
	private int selectedBucket;	
	private String hashFunction; // Enum
	
	public HashSamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize,
			int bucketSize, int bucketNumber, String HashFunction) {

		super(createdTime, plan, index, topologyType);
		
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;				
		
		this.bucketSize = bucketSize;
		this.selectedBucket = bucketNumber;
		this.hashFunction = HashFunction;
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
