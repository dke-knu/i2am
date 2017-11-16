package knu.cs.dke.topology_manager_v3.topolgoies;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

import knu.cs.dke.topology_manager_v3.handlers.RemoteStormController;
import submitter.RemoteSubmitter;

public class HashSamplingTopology extends ASamplingFilteringTopology{
	
	private int sampleSize;
	private int windowSize;
	private int bucketSize;
	private int selectedBucket;	
	private String hashFunction; // Enum
	
	
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

	public int getSelectedBucket() {
		return selectedBucket;
	}

	public void setSelectedBucket(int selectedBucket) {
		this.selectedBucket = selectedBucket;
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

	public int getBucketSize() {
		return bucketSize;
	}

	public void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	public String getHashFunction() {
		return hashFunction;
	}

	public void setHashFunction(String hashFunction) {
		this.hashFunction = hashFunction;
	}
}
