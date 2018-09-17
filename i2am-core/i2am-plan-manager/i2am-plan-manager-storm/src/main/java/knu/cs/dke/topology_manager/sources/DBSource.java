package knu.cs.dke.topology_manager.sources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import i2am.plan.manager.kafka.I2AMProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class DBSource extends Source {

	private String ip;
	private String port;
	private String userId;
	private String userPassword;
	private String dbName;	
	private String query;	

	public DBSource(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine,
			String dbIp, String dbPort, String dbId, String dbPassword, String dbName, String dbQuery)
	{
		super(sourceName, createdTime, owner, srcType, data, useConceptDrift, useLoadShedding, useIntelliEngine);

		this.ip = dbIp;
		this.port = dbPort;
		this.userId = dbId;
		this.userPassword = dbPassword;
		this.dbName = dbName;
		this.query = dbQuery;		
	}

	public DBSource(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine, String testData, String target,
			String dbIp, String dbPort, String dbId, String dbPassword, String dbName, String dbQuery)
	{
		super(sourceName, createdTime, owner, srcType, data, useConceptDrift, useLoadShedding, useIntelliEngine, testData, target);

		this.ip = dbIp;
		this.port = dbPort;
		this.userId = dbId;
		this.userPassword = dbPassword;
		this.dbName = dbName;
		this.query = dbQuery;		
	}

	@Override
	public void run() {

		// JDBC driver name and Database URL
		String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
		String DB_URL = "jdbc:mariadb://" + ip + "/" + dbName;

		I2AMProducer producer = new I2AMProducer(super.getOwner(), super.getSourceName());
		
		// DB Config.
		Connection connection = null;
		Statement stmt = null;

		try {
			// Register JDBC driver
			Class.forName("org.mariadb.jdbc.Driver");

			// Open a Connection
			System.out.println("Connecting to a selected database...");

			connection = DriverManager.getConnection(DB_URL, userId, userPassword);
			System.out.println("Connected database successfully...");

			//STEP 4: Execute a query
			System.out.println("Creating table in given database...");
			stmt = connection.createStatement();
		} 
		catch( Exception e ) {
			e.printStackTrace();
		}

		try {
			while(true) {

				stmt.executeUpdate(query);
				ResultSet result = stmt.getResultSet();
				ResultSetMetaData rsmd = result.getMetaData();

				int columns = rsmd.getColumnCount();

				while(result.next()) {

					String message = "";

					for (int i = 1; i <= columns; i++) {

						String columnValue = result.getString(i);											
						message = message + columnValue + ",";
					}

					message = message.substring(0, message.length()-1);
					producer.send(message);

					// System.out.println(message);		
				}	
				Thread.sleep(3000);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}finally {
			producer.close();
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}		
}
