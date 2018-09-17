package knu.cs.dke.topology_manaver_test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaTester {

	public static void main(String[] args) throws InterruptedException {


		String topic = "745cabf3-86ec-4d85-8642-f506f34f16a5";
		String groupId = UUID.randomUUID().toString(); 

		//String server = "114.70.235.43:9092";

		String servers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
				+ "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";

		// Consumer Props
		Properties consume_props = new Properties();
		consume_props.put("bootstrap.servers", servers);
		consume_props.put("group.id", groupId);
		consume_props.put("enable.auto.commit", "true");
		consume_props.put("auto.offset.reset", "earliest");
		consume_props.put("auto.commit.interval.ms", "1000");
		consume_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consume_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		// Producer Props
		Properties produce_props = new Properties();
		produce_props.put("bootstrap.servers", servers);
		produce_props.put("acks", "all");
		produce_props.put("retries", 0);
		produce_props.put("batch.size", 16384);
		produce_props.put("linger.ms", 1);
		produce_props.put("buffer.memory", 33554432);
		produce_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		produce_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");


		//KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consume_props);
		Producer<String, String> producer = new KafkaProducer<>(produce_props);
		
		
		Consumer<String, String> consumer = new KafkaConsumer<>(consume_props);
		consumer.subscribe(Arrays.asList(topic));
		
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				
				System.out.println(record.value());
				//producer.send(new ProducerRecord<String, String>("5ece46bb-7668-44e6-9b8e-3a1cd2621959", record.value()));
				//Thread.sleep(1000);
			}
				
		}
		
		/*SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
		Date currentTime;
		String mTime;
		
		while (true) {
						
			currentTime = new Date();
			mTime = mSimpleDateFormat.format(currentTime);
			System.out.println(mTime);
			producer.send(new ProducerRecord<String, String>("topic0725", mTime));
			Thread.sleep(1000);
			
		}	*/	

	}
}
