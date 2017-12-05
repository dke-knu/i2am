package knu.cs.dke.topology_manager_v3.sources;

import java.util.Arrays;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;


public class KafkaSource extends Source {

	private String zookeeperIp;
	private String zookeeperPort;
	private String topic;	
		
	private boolean status;
	
	// Create Kafka Source
	public KafkaSource (String sourceName, String createdTime, String owner, String useIntelliEngine, String testData,
			String srcType, String switchMessaging, String zookeeperIp, String zookeeperPort, String topic) {		
		
		super(sourceName, createdTime, owner, useIntelliEngine, "N", testData, srcType, switchMessaging);
		
		this.setZookeeperIp(zookeeperIp);
		this.setZookeeperPort(zookeeperPort);
		this.topic = topic;		
	}

	@Override
	public void run() {

		// Consumer: Read from User's Source
		// Needed Parameters: server IP&Port, topic name ...
		String read_servers = zookeeperIp + ":" + zookeeperPort;
		String read_topics = topic;
		String groupId = UUID.randomUUID().toString(); // Offset을 초기화 하려면 새로운 이름을 줘야한다. 걍 랜덤!

		Properties consumer_props = new Properties();
		consumer_props.put("bootstrap.servers", read_servers); // From User
		consumer_props.put("group.id", groupId);
		// consumer_props.put("enable.auto.commit", "false");
		consumer_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumer_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumer_props.put("auto.offset.reset", "earliest");

		// Producer: To Server Kafka
		String write_servers = "MN:9092";
		String write_topic = super.getTransTopic();

		Properties producer_props = new Properties();
		producer_props.put("bootstrap.servers", write_servers); // To System
		producer_props.put("acks", "all");
		// producer_props.put("retries", 0);
		// producer_props.put("batch.size", 16384);
		// producer_props.put("linger.ms", 1);
		// producer_props.put("buffer.memory", 33554432);
		producer_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producer_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");		

		////////////////////
		//* Read & Write *///
		//////////////////////
		/////////////////////

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumer_props);
		consumer.subscribe(Arrays.asList(read_topics));

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(producer_props);		

		String sourceName = super.getSourceName();
		// boolean status = true;				
		
		try {			
			
			while (!Thread.currentThread().isInterrupted()) {
				
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				for (ConsumerRecord<String, String> record : records) {
					// System.out.println(record.value());
					producer.send(new ProducerRecord<String,String>(write_topic, record.value()));
				}					
				Thread.sleep(100);
			}			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			consumer.close();
			producer.close();
		}
	}

	public void setTopic(String topic) {		
		this.topic = topic;
	}

	public String getTopic() { 
		return this.topic;
	}

	public String getZookeeperIp() {
		return zookeeperIp;
	}

	public void setZookeeperIp(String zookeeperIp) {
		this.zookeeperIp = zookeeperIp;
	}

	public String getZookeeperPort() {
		return zookeeperPort;
	}

	public void setZookeeperPort(String zookeeperPort) {
		this.zookeeperPort = zookeeperPort;
	}

}
