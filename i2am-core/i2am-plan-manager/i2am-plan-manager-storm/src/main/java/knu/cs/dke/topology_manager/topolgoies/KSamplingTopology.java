package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class KSamplingTopology extends ASamplingFilteringTopology {

	private int samplingRate;
	
	public KSamplingTopology(String createdTime, String plan, int index, String topologyType, int samplingRate) throws TTransportException {

		super(createdTime, plan, index, topologyType);		
		this.samplingRate = samplingRate;		
	}
	
	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}
}
