package knu.cs.dke.topology_manager.topolgoies;

public class QueryFilteringTopology extends ASamplingFilteringTopology {

	private String query;		

	public QueryFilteringTopology(String createdTime, String plan, int index, String topologyType, String keyword) {
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
