package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class BloomFilteringTopology extends ASamplingFilteringTopology{

	// Parameters
	private int bucketSize;
	private String keywords;
	private int target;	
	
	public BloomFilteringTopology(String createdTime, String plan, int index, String topologyType, int bucketSize, String keywords, int target) throws TTransportException {

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
