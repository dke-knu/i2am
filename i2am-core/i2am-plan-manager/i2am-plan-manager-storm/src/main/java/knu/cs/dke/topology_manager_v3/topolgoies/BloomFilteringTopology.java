package knu.cs.dke.topology_manager_v3.topolgoies;

public class BloomFilteringTopology extends ASamplingFilteringTopology{

	// Parameters
	String[] keywords;


	public BloomFilteringTopology(String createdTime, String plan, int index, String topologyType, String[] keywords) {

		super(createdTime, plan, index, topologyType);
		this.keywords = keywords;		
	}

	@Override
	public void submitTopology() {
		// TODO Auto-generated method stub		
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
