package knu.cs.dke.topology_manager_v3.sources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class DBSource extends Source {

	private String ip;
	private String port;
	private String userId;
	private String userPassword;
	private String dbName;	
	private String query;	

	public DBSource(String sourceName, String createdTime, String owner, String useIntelliEngine, String testData,
			String srcType, String switchMessaging, String dbIp, String dbPort, String dbId, String dbPassword, String dbName, String dbQuery) {

		super(sourceName, createdTime, owner, useIntelliEngine, "N", testData, srcType, switchMessaging);

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
		String DB_URL = "jdbc:mariadb//" + ip + "/" + dbName;

		// Kafka Config. (Producer)
		// Producer
		String servers = "MN:9092";
		String topic = super.getTransTopic();

		Properties props = new Properties();
		props.put("bootstrap.servers", servers);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");		

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

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

				if(result.next()) {

					String message = "";

					for (int i = 1; i <= columns; i++) {

						String columnValue = result.getString(i);											
						message = message + columnValue + ",";


					}
					message = message.substring(0, message.length()-1);
					producer.send(new ProducerRecord<String, String>(topic, message));
					System.out.println(message);					
				}
				
				Thread.sleep(3000);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
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
