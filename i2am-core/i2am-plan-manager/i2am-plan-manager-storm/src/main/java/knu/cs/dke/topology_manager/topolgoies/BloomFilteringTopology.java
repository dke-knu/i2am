package knu.cs.dke.topology_manager.topolgoies;

public class BloomFilteringTopology extends ASamplingFilteringTopology {

	// Parameters
	private int bucketSize;
	private String keywords;
	private int target;	
	
	private String hashFunction1;
	private String hashFunction2;
	private String hashFunction3;
	
	public BloomFilteringTopology(String createdTime, String plan, int index, String topologyType, int bucketSize, String keywords, int target,
			String hashFunction1, String hashFunction2, String hashFunction3) {

		super(createdTime, plan, index, topologyType);
		
		this.bucketSize = bucketSize;
		this.keywords = keywords;
		this.target = target;		
		
		this.hashFunction1 = hashFunction1;
		this.hashFunction2 = hashFunction2;
		this.hashFunction3 = hashFunction3;		
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

	public String getHashFunction1() {
		return hashFunction1;
	}

	public void setHashFunction1(String hashFunction1) {
		this.hashFunction1 = hashFunction1;
	}

	public String getHashFunction2() {
		return hashFunction2;
	}

	public void setHashFunction2(String hashFunction2) {
		this.hashFunction2 = hashFunction2;
	}

	public String getHashFunction3() {
		return hashFunction3;
	}

	public void setHashFunction3(String hashFunction3) {
		this.hashFunction3 = hashFunction3;
	}	
}
