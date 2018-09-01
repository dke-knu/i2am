package knu.cs.dke.topology_manager.topolgoies;

public class BloomFilteringTopology extends ASamplingFilteringTopology {

	// Parameters
	private int bucketSize;
	private String keywords;
	private int target;	
	
	public BloomFilteringTopology(String createdTime, String plan, int index, String topologyType, int bucketSize, String keywords, int target) {

		super(createdTime, plan, index, topologyType);
		
		this.bucketSize = bucketSize;
		this.keywords = keywords;
		this.target = target;		
	}

	public int getBucketSize() {
		return bucketSize;
	}

	public void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}	
}
