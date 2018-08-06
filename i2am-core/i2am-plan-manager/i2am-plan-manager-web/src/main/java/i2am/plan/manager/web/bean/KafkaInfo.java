package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

public class KafkaInfo { 
	private String zkIp;
	private String zkPort;
	private String topic;
	
	public KafkaInfo(String zkIp, String zkPort, String topic) {
		this.zkIp = zkIp;
		this.zkPort = zkPort;
		this.topic = topic;
	}
	
	public String getZkIp() {
		return zkIp;
	}
	public String getZkPort() {
		return zkPort;
	}
	public String getTopic() {
		return topic;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("zookeeperIp", zkIp);
		obj.put("zookeeperPort", zkPort);
		obj.put("topic", topic);
		return obj;
	}
}
