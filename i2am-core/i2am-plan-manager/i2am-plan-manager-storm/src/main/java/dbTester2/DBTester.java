package dbTester2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class DBTester {

	// JDBC driver name and Database URL
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
	static final String DB_URL = "jdbc:mariadb//127.0.0.1/Unnamed";

	// Database user Info.			
	static final String user = "user";
	static final String password = "1234";


	public static void main(String[] args)  { 

		// Kafka(Consumer) to Database

		// Kafka Config
		// Consumer: Read from User's Source
		// Needed Parameters: server IP&Port, topic name ...
		String servers = "MN:9092";
		String topics = "db-topic";
		String groupId = "test"; // Offset을 초기화 하려면 새로운 이름을 줘야한다.

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
			connection = DriverManager.getConnection("jdbc:mariadb://127.0.0.1/test_database", "user1", "1234");
			System.out.println("Connected database successfully...");

			// Execute a query
			System.out.println("Creating table in given database...");
			stmt = connection.createStatement();
			
			
			
		}
		catch(Exception e ) {
			e.printStackTrace();
		}
		
		while(true) {

			try {
			
				// Consume.
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);

				for (ConsumerRecord<String, String> record : records) {					

					System.out.println(record.value());	

					// Send to DB!
					String query = "insert into test_output_table values ('" + record.key() + "', '" + record.value() + "')";
					stmt.executeUpdate(query);
				}			
				
				System.out.println("DB Check");

			} catch(Exception e) {
				e.printStackTrace();
			} 
		}
		
		// consumer.close();
		// stmt.close();
		// connection.close();
	}
}
