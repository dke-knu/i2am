package knu.cs.dke.topology_manager.destinations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import i2am.plan.manager.kafka.I2AMConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;

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
        // Consumer: Read from User's Source
        // Needed Parameters: server IP&Port, topic name ...
        String read_server = "114.70.235.43:19092";

        String read_servers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
                + "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";

        String read_topics = super.getTransTopic();
        String groupId = UUID.randomUUID().toString(); // Offset을 초기화 하려면 새로운 이름을 줘야한다. 걍 랜덤!

//		// Consumer Props
//		Properties consume_props = new Properties();
//		consume_props.put("bootstrap.servers", read_servers);
//		consume_props.put("group.id", groupId);
//		consume_props.put("enable.auto.commit", "true");
//		consume_props.put("auto.offset.reset", "earliest");
//		consume_props.put("auto.commit.interval.ms", "1000");
//		consume_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//		consume_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

//		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consume_props);
//		consumer.subscribe(Arrays.asList(super.getTransTopic()));

        I2AMConsumer consumer = new I2AMConsumer(super.getOwner(), super.getDestinationName());

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send.
        try {
            // Consume.
            Queue<String> q = new LinkedBlockingQueue<String>(100);
            consumer.receive(q);

            while (true) {
                String message;
                while(!q.isEmpty()){
                    message = q.poll();
                    //send to DB!
                    String query = "insert into " + tableName + " values ('" + message + "')";
                    stmt.executeUpdate(query);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
