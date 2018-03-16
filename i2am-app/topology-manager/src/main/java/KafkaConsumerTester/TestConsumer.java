package KafkaConsumerTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class TestConsumer {

	public static void main(String[] args) {

		// Consumer: Read from User's Source
		// Needed Parameters: server IP&Port, topic name ...
		String servers = "MN:9092";
		String topics = "query-in";
		String groupId = "test"; // Offset을 초기화 하려면 새로운 이름을 줘야한다.
		
		Properties props = new Properties();
		props.put("bootstrap.servers", servers);
		props.put("group.id", groupId);
		props.put("enable.auto.commit", "false");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("auto.offset.reset", "earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topics));
		
		try {			
			while (true) {
				
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
			
				for (ConsumerRecord<String, String> record : records) {					
					System.out.println(record.value());					
				}
			}			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			consumer.close();
		}
	}
}