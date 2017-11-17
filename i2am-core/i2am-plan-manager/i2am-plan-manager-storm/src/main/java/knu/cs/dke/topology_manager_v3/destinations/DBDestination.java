package knu.cs.dke.topology_manager_v3.destinations;

public class DBDestination extends Destination {
	
	private String id;
	private String password;
	private String dbName;
	private String tableName;	
	
	private String query;
	
		
	public DBDestination(String destinationName, String createdTime, String owner, String dstType,
			String dbuser, String dbPassword, String dbName, String dbTable, String query) {
	
		super(destinationName, createdTime, owner, dstType);
		
		this.id = dbuser;
		this.password = dbPassword;
		this.dbName = dbName;
		this.tableName = dbTable;
		this.query = query;
	}

	public String getPassword() {
		return password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDbName() {
		return dbName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public void write() {
		// TODO Auto-generated method stub
		
	}
}
