package KafkaTester;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class kafkaToKafka {

	public static void main(String[] args) {

		// Consumer: Read from User's Source
		// Needed Parameters: server IP&Port, topic name ...
		String read_servers = "MN:9092";
		String read_topics = "test";
		String groupId = "test"; // Offset을 초기화 하려면 새로운 이름을 줘야한다.

		Properties consumer_props = new Properties();
		consumer_props.put("bootstrap.servers", read_servers);
		consumer_props.put("group.id", groupId);
		consumer_props.put("enable.auto.commit", "false");
		consumer_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumer_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumer_props.put("auto.offset.reset", "earliest");

		// Producer
		String write_servers = "MN:9092";
		String write_topic = "test_out";

		Properties producer_props = new Properties();
		producer_props.put("bootstrap.servers", write_servers);
		producer_props.put("acks", "all");
		producer_props.put("retries", 0);
		producer_props.put("batch.size", 16384);
		producer_props.put("linger.ms", 1);
		producer_props.put("buffer.memory", 33554432);
		producer_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producer_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");		

		////////////////////
		//* Read & Write *///
		//////////////////////
		/////////////////////

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumer_props);
		consumer.subscribe(Arrays.asList(read_topics));

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(producer_props);		

		try {			
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				for (ConsumerRecord<String, String> record : records) {
					System.out.println(record.value());
					producer.send(new ProducerRecord<String,String>(write_topic, record.value()+" :)"));
				}
			}			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			consumer.close();
			producer.close();
		}

	}
}
