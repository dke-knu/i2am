package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class PrioritySamplingTopology extends ASamplingFilteringTopology {

	private int sampleSize;
	private int windowSize;
	private int target;
	
	public PrioritySamplingTopology(String createdTime, String plan, int index, String topologyType, int sampleSize, int windowSize, int target) throws TTransportException {

		super(createdTime, plan, index, topologyType);
		this.sampleSize = sampleSize;
		this.windowSize = windowSize;	
		this.target = target;
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

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}
}
