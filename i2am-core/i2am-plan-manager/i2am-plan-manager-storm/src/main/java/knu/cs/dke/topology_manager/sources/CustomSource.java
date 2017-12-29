package knu.cs.dke.topology_manager.sources;

public class CustomSource extends Source{
		
	public CustomSource(String sourceName, String createdTime, String owner, String useIntelliEngine,
			String useLoadShedding, String testData, String srcType, String switchMessaging) {
		
		super(sourceName, createdTime, owner, useIntelliEngine, "N", testData, srcType, switchMessaging);

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
