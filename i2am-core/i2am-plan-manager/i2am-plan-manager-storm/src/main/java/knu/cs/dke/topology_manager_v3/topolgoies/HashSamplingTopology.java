package knu.cs.dke.topology_manager_v3.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class HashSamplingTopology extends ASamplingFilteringTopology{
		
	private int sampleSize;
	private int windowSize;
	// private int bucketSize;	
	private String hashFunction; // Enum
	
	private RemoteStormController storm;
	
	public HashSamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize,
			String HashFunction) throws TTransportException {

		super(createdTime, plan, index, topologyType);
		
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;	
		// this.bucketSize = bucketSize;
		this.hashFunction = HashFunction;
		
		storm = new RemoteStormController();
	}
	
	@Override
	public void killTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException {
		// TODO Auto-generated method stub
		storm.killTopology(super.getTopologyName());
	}

	@Override
	public void avtivateTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException {
		// TODO Auto-generated method stub
		storm.activateTopology(super.getTopologyName());
		
	}

	@Override
	public void deactivateTopology() throws NotAliveException, AuthorizationException, TException, InterruptedException {
		// TODO Auto-generated method stub
		storm.deactivateTopology(super.getTopologyName());
	}

	@Override
	public void submitTopology() throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {
		// TODO Auto-generated method stub
		storm.runTopology(this);		
	}

	public String getHashFunction() {
		return hashFunction;
	}

	public void setHashFunction(String hashFunction) {
		this.hashFunction = hashFunction;
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
