package knu.cs.dke.topology_manager.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;


public class KafkaSource extends Source {

	private String zookeeperIp;
	private String zookeeperPort;
	private String topic;	

	//private boolean status;
	KafkaConsumer<String, String> consumer;
	KafkaProducer<String, String> producer;
	
	// Create Kafka Source
	public KafkaSource(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine,
			String zookeeperIp, String zookeeperPort, String topic)
	{
		super(sourceName, createdTime, owner, srcType, data, useConceptDrift, useLoadShedding, useIntelliEngine);

		this.zookeeperIp = zookeeperIp;
		this.zookeeperPort = zookeeperPort;
		this.topic = topic;	
	}

	public KafkaSource(String sourceName, String createdTime, String owner, String srcType, ArrayList<SourceSchema> data,
			String useConceptDrift, String useLoadShedding, String useIntelliEngine, String testData, String target,
			String zookeeperIp, String zookeeperPort, String topic)
	{
		super(sourceName, createdTime, owner, srcType, data, useConceptDrift, useLoadShedding, useIntelliEngine, testData, target);

		this.zookeeperIp = zookeeperIp;
		this.zookeeperPort = zookeeperPort;
		this.topic = topic;
	}	

	public void init() {

		// Consumer: Read from User's Source
		// Needed Parameters: server IP&Port, topic name ...
		String read_servers = zookeeperIp + ":" + zookeeperPort;
		String read_topics = topic;
		String groupId = UUID.randomUUID().toString(); // Offset을 초기화 하려면 새로운 이름을 줘야한다. 걍 랜덤!

		// Consumer Props
		Properties consume_props = new Properties();
		consume_props.put("bootstrap.servers", read_servers);
		consume_props.put("group.id", groupId);
		consume_props.put("enable.auto.commit", "true");
		consume_props.put("auto.offset.reset", "earliest");
		consume_props.put("auto.commit.interval.ms", "1000");
		consume_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consume_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		// Producer: To Server Kafka
		String write_server = "114.70.235.43:19092";

		String write_servers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
				+ "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";

		String write_topic = super.getTransTopic();

		Properties produce_props = new Properties();
		produce_props.put("bootstrap.servers", write_servers);
		produce_props.put("acks", "all");
		produce_props.put("retries", 0);
		produce_props.put("batch.size", 16384);
		produce_props.put("linger.ms", 1);
		produce_props.put("buffer.memory", 33554432);
		produce_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		produce_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");	

		////////////////////
		//* Read & Write *///
		//////////////////////
		/////////////////////
		consumer = new KafkaConsumer<String, String>(consume_props);
		consumer.subscribe(Arrays.asList(read_topics));
		
		producer = new KafkaProducer<String, String>(produce_props);		
	}

	@Override
	public void run() {

		// String sourceName = super.getSourceName();
		// boolean status = true;
		this.init();
		
		try {			
			while (true) {

				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);

				for (ConsumerRecord<String, String> record : records) {
					System.out.println(record.value());					
					producer.send(new ProducerRecord<String,String>(this.getTransTopic(), record.value()));				
				}				
				if(Thread.currentThread().isInterrupted()) break;												
			}			
		} catch(WakeupException e) {
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
