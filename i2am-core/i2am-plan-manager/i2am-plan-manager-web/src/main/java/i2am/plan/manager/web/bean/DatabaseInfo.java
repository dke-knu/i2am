package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject;

public class DatabaseInfo {
	private String ip;
	private String port;
	private String id;
	private String pw;
	private String query;
	private String db;
	private String table;
	
	public DatabaseInfo(String ip, String port, String id, String pw, String query) {
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.pw = pw;
		this.query = query;
	}
	
	public DatabaseInfo(String ip, String port, String id, String pw, String db, String table) {
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.pw = pw;
		this.db = db;
		this.table = table;
	}

	public String getIp() {
		return ip;
	}
	public String getPort() {
		return port;
	}
	public String getId() {
		return id;
	}
	public String getPw() {
		return pw;
	}
	public String getQuery() {
		return query;
	}
	public String getDb() {
		return db;
	}
	public String getTable() {
		return table;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("databaseIp", ip);
		obj.put("databasePort", port);
		obj.put("databaseId", id);
		obj.put("databasePw", pw);
		if (query != null) {
			obj.put("query", query);
		} else {
			obj.put("database", db);
			obj.put("table", table);
		}
		return obj;
	}
}
