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

	public QueryFilteringTopology(String createdTime, String plan, int index, String topologyType, String keyword) throws TTransportException {
		super(createdTime, plan, index, topologyType);
		
		this.query = keyword;					
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String keywords) {
		this.query = keywords;
	}		
}
