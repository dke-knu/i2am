package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class UCKSamplingTopology extends ASamplingFilteringTopology {

	private int samplingRate;
	private double ucUnderBound;
	
	public UCKSamplingTopology(String createdTime, String plan, int index, String topologyType, int samplingRate, double uc) throws TTransportException {

		super(createdTime, plan, index, topologyType);

		this.samplingRate = samplingRate;
		this.ucUnderBound = uc;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}

	public double getUcUnderBound() {
		return ucUnderBound;
	}

	public void setUcUnderBound(double ucUnderBound) {
		this.ucUnderBound = ucUnderBound;
	}

}
