package knu.cs.dke.topology_manager_v3.sources;

public abstract class Source {
	
	// Basic Info.
	private String owner;
	private String sourceName;
	private String createdTime;	
	private String modifiedTime;
	
	// Source Info.
	private String status;
	private String useIntelliEngine;
	private String useLoadShedding;	
	
	private String testData;
	private String srcType;	
	private String switchMessaging;
	
	// System topic;	
	private String transTopic;
	
	public abstract void read();

	public Source(String sourceName, String createdTime, String owner, String useIntelliEngine, String testData,
			String srcType, String switchMessaging) {
	
		this.sourceName = sourceName;
		this.createdTime = createdTime;
		this.modifiedTime = createdTime;
		this.owner = owner;
		
		this.status = "DEACTIVE"; // 초기값은 DEACTIVE
		this.useIntelliEngine = useIntelliEngine;
		this.useLoadShedding = "N"; // 미구현
		this.testData = testData; // UseLoadShedding이 Yes일 경우
		
		this.srcType = srcType;
		this.switchMessaging = switchMessaging;		
		
		this.transTopic = owner + "-" + sourceName;
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
}
