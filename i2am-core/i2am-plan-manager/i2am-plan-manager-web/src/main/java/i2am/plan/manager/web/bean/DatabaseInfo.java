package i2am.plan.manager.web.bean;

import org.json.simple.JSONObject; 

public class DatabaseInfo {
	public enum DATABASE_TYPE {
		SRC, DST
	};
	private DATABASE_TYPE type;
	private String ip;
	private String port;
	private String id;
	private String pw;
	private String db;
	private String query_or_table;
	
	public DatabaseInfo(DATABASE_TYPE type, String ip, String port, String id, String pw, String db, String query_or_table) {
		this.type = type;
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.pw = pw;
		this.db = db;
		this.query_or_table = query_or_table;
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
		if (type.equals(DATABASE_TYPE.SRC))
			return query_or_table;
		return null;
	}
	public String getDb() {
		return db;
	}
	public String getTable() {
		if (type.equals(DATABASE_TYPE.DST))
			return query_or_table;
		return null;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("databaseIp", ip);
		obj.put("databasePort", port);
		obj.put("databaseId", id);
		obj.put("databasePw", pw);
		obj.put("database", db);
		if (type.equals(DATABASE_TYPE.SRC)) {
			obj.put("query", query_or_table);
		} else {
			obj.put("table", query_or_table);
		}
		return obj;
	}
}
