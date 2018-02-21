package knu.cs.dke.topology_manager_v3.topolgoies;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.thrift.TException;

import knu.cs.dke.topology_manager_v3.handlers.RemoteStormController;

public class HashSamplingTopology extends ASamplingFilteringTopology{

	private int numberOfBucket;
	private int selectedBucket;
	// private int hashFunction;
	
	public HashSamplingTopology(int numberOfBucket, int selectedBucket) {
		this.numberOfBucket = numberOfBucket;
		this.selectedBucket = selectedBucket;
	}	
	
	@Override
	public void submitTopology() {
		// TODO Auto-generated method stub	
		RemoteStormController rsc = new RemoteStormController();
		
		try {
			rsc.runTopology("HASH_SAMPLING", this.getTopologyID());
		} catch (InvalidTopologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
