package knu.cs.dke.topology_manager.topolgoies;

import java.io.IOException;
import java.util.List;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;

public class QueryFilteringTopology extends ASamplingFilteringTopology {

	private String query;		
	
	private RemoteStormController storm;
	
	public QueryFilteringTopology(String createdTime, String plan, int index, String topologyType, String keyword) throws TTransportException {
		super(createdTime, plan, index, topologyType);
		
		this.query = keyword;		
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
		
		// Parameter Formatting		
		storm.runTopology(this);		
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String keywords) {
		this.query = keywords;
	}	
	
}
