package knu.cs.dke.topology_manager_v3.sources;

public class DBSource extends Source {

	private String Id;
	private String password;
	private String dbName;
	private String tableName;
		
	private String query;	
	
	public DBSource(String ID, String owner, String createTime, String sourceType, String ip, String port,
			String topic, String dbUser, String dbPassword, String dbName, String dbTable, String query) {
		super(ID, owner, createTime, sourceType, ip, port, topic);
		
		this.Id = dbUser;
		this.password = dbPassword;
		this.dbName = dbName;
		this.tableName = dbTable;
		this.query = query;		
	}	
	
	public void setDbId(String Id) {
		this.Id = Id;
	}
	public String getDbId() {
		return this.Id;
	}
	public void setDbPassword(String pw) {
		this.password = pw;
	}
	public String getDbPassword() {
		return this.password;
	}
	public void setDbName(String db) {
		this.dbName = db;
	}
	public String getDbName() {
		return this.dbName;
	}
	public void setTable(String table) {
		this.tableName = table;
	}
	public String getTable() {
		return this.tableName;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getQuery() {
		return this.query;
	}	
	
	@Override
	public void read() {
		// TODO Auto-generated method stub
		
	}	
}
