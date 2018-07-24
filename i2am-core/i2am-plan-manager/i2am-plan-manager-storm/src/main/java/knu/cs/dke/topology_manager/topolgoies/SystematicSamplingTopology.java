package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class SystematicSamplingTopology extends ASamplingFilteringTopology {

	private int interval;
	
	public SystematicSamplingTopology(String createdTime, String plan, int index, String topologyType, int interval) throws TTransportException {

		super(createdTime, plan, index, topologyType);
		
		this.interval = interval;
	}

	public int getInterval() {
		return interval;
	}


	public void setInterval(int interval) {
		this.interval = interval;
	}
}
