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
	
	private RemoteStormController storm;
	
	public UCKSamplingTopology(String createdTime, String plan, int index, String topologyType, int samplingRate, double uc) throws TTransportException {

		super(createdTime, plan, index, topologyType);

		this.samplingRate = samplingRate;
		this.ucUnderBound = uc;
		
		storm = new RemoteStormController();
	}

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
