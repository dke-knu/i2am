package knu.cs.dke.topology_manager.sources;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Source extends Thread {
	
	// Basic Info.
	private String owner;
	private String sourceName;
	private String createdTime;	
	private String modifiedTime;	
	private String srcType;
	private String status;
	
	// System topic	
	private String transTopic;	
	
	// Data Scheme
	private ArrayList<SourceSchema> data;
	
	// Option 1: Intelligent Engine.	
	private String useIntelliEngine;
	private String testData;
	private String target;
	
	// Option 2: LoadShedding.
	private String useLoadShedding;	
	private String switchMessaging;	
	
	// Option 3: ConceptDrift
	private String useConceptDrift;
	
	public Source(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine) {
		
		// Required
		this.sourceName = sourceName;
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.owner = owner;
		this.srcType = srcType;		
		this.status = "DEACTIVE"; // 초기값
		
		this.transTopic = UUID.randomUUID().toString();
		
		this.data = data;
		
		// Option 1: Intelligent Engine. --> No!!
		this.useIntelliEngine = "N";
		this.testData = null;
		this.target = null;
		
		// Option 2: LoadShedding.
		this.useLoadShedding = useLoadShedding;		
		this.switchMessaging = "N";
		
		// Option 3: Concept Drift
		this.useConceptDrift = useConceptDrift;
	}	
	
	public Source(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine, String testData, String target) {
		
		// Required
		this.sourceName = sourceName;
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.owner = owner;
		this.srcType = srcType;		
		this.status = "DEACTIVE"; // 초기값
		
		this.transTopic = UUID.randomUUID().toString();
		
		this.data = data;
		
		// Option 1: Intelligent Engine. --> No!!
		this.useIntelliEngine = useIntelliEngine;
		this.testData = testData;
		this.target = target;
		
		// Option 2: LoadShedding.
		this.useLoadShedding = useLoadShedding;		
		this.switchMessaging = "N";
		
		// Option 3: Concept Drift
		this.useConceptDrift = useConceptDrift;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSourceName() {
		return this.sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUseIntelliEngine() {
		return useIntelliEngine;
	}

	public void setUseIntelliEngine(String useIntelliEngine) {
		this.useIntelliEngine = useIntelliEngine;
	}

	public String getUseLoadShedding() {
		return useLoadShedding;
	}

	public void setUseLoadShedding(String useLoadShedding) {
		this.useLoadShedding = useLoadShedding;
	}

	public String getTestData() {
		return testData;
	}

	public void setTestData(String testData) {
		this.testData = testData;
	}

	public String getSrcType() {
		return srcType;
	}

	public void setSrcType(String srcType) {
		this.srcType = srcType;
	}

	public String getSwitchMessaging() {
		return switchMessaging;
	}

	public void setSwitchMessaging(String switchMessaging) {
		this.switchMessaging = switchMessaging;
	}

	public String getTransTopic() {
		return transTopic;
	}

	public void setTransTopic(String outTopic) {
		this.transTopic = outTopic;
	}


	public ArrayList<SourceSchema> getData() {
		return data;
	}


	public void setData(ArrayList<SourceSchema> data) {
		this.data = data;
	}
	
	public String getTarget() {
		return target;
	}


	public void setTarget(String target) {
		this.target = target;
	}


	public String getUseConceptDrift() {
		return useConceptDrift;
	}


	public void setUseConceptDrift(String useConceptDrift) {
		this.useConceptDrift = useConceptDrift;
	}		
}
