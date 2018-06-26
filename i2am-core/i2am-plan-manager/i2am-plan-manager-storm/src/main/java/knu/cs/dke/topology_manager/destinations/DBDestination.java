package knu.cs.dke.topology_manager.destinations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class DBDestination extends Destination {

	private String Ip;
	private String port;
	private String userId;
	private String userPassword;
	private String dbName;
	private String tableName;	

	public DBDestination(String destinationName, String createdTime, String owner, String dstType,
			String dbIp, String dbPort, String dbId, String dbPassword, String dbName, String tableName) {

		super(destinationName, createdTime, owner, dstType);

		this.Ip = dbIp;
		this.port = dbPort;
		this.userId = dbId;
		this.userPassword = dbPassword;
		this.dbName = dbName;
		this.tableName = tableName;		
	}

	@Override
	public void run() {

		// transtopic을 읽어서 
		// 외부 DB로 쓴당
		// DB에서 ACTIVE 체크

		// Kafka(Consumer) to Database
		// Kafka Config
		// Consumer: Read from User's Source
		// Needed Parameters: server IP&Port, topic name ...
		String servers = "MN:9092";
		String topics = super.getTransTopic();
		String groupId = super.getDestinationName();

		Properties props = new Properties();
		props.put("bootstrap.servers", servers);
		props.put("group.id", groupId);
		props.put("enable.auto.commit", "false");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("auto.offset.reset", "earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList(topics));

		// DB Config.
		Connection connection = null;
		Statement stmt = null;

		// DB Connect.
		try {

			// Register JDBC driver
			Class.forName("org.mariadb.jdbc.Driver");

			// Open a Connection
			System.out.println("Connecting to a selected database...");

			// connection = DriverManager.getConnection("jdbc:mariadb://127.0.0.1/test_database", "user1", "1234");
			connection = DriverManager.getConnection("jdbc:mariadb://" + Ip + "/" + dbName, userId, userPassword);

			System.out.println("Connected database successfully...");

			// Execute a query
			System.out.println("Creating table in given database...");
			stmt = connection.createStatement();
		}
		catch(Exception e ) {
			e.printStackTrace();
		}

		// Send.
		try {
			while(true) {

				try {
					// Consume.
					ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);

					for (ConsumerRecord<String, String> record : records) {
						// System.out.println(record.value());	
						// Send to DB!
						String query = "insert into " + tableName + " values ('" + record.key() + "', '" + record.value() + "')";
						stmt.executeUpdate(query);
					}			
					// System.out.println("DB Check");

				} catch(Exception e) {
					e.printStackTrace();
				} 
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			consumer.close();			
			// stmt.close();
			// connection.close();
		}
		
	}

	public String getIp() {
		return Ip;
	}

	public void setIp(String ip) {
		Ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
