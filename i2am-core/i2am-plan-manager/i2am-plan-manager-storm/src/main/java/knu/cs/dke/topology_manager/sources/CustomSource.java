package knu.cs.dke.topology_manager.sources;

import java.util.ArrayList;

public class CustomSource extends Source{
	
	public CustomSource(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine)
	{
		super(sourceName, createdTime, owner, srcType, data, useConceptDrift, useLoadShedding, useIntelliEngine);
	}
	
	public CustomSource(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine, String testData, String target)
	{
		super(sourceName, createdTime, owner, srcType, data, useConceptDrift, useLoadShedding, useIntelliEngine, testData, target);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub		
	}

}
