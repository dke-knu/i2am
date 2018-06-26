package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class SystematicSamplingTopology extends ASamplingFilteringTopology {

	private int interval;
	
	private RemoteStormController storm;
	
	public SystematicSamplingTopology(String createdTime, String plan, int index, String topologyType, int interval) throws TTransportException {

		super(createdTime, plan, index, topologyType);
		
		this.interval = interval;
				
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


	public int getInterval() {
		return interval;
	}


	public void setInterval(int interval) {
		this.interval = interval;
	}

}
