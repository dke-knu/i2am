package knu.cs.dke.topology_manaver_test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.*;

public class LsTaskTester {

	public static LinkedList<Integer> list = new LinkedList<Integer>();

	public static void main(String[] args) throws InterruptedException, IOException {

		String brokers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,"
				+ "114.70.235.43:19095,114.70.235.43:19096,114.70.235.43:19097,"
				+ "114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";
		String groupId = UUID.randomUUID().toString();

		Properties conProps = new Properties();
		conProps.put("bootstrap.servers", brokers);
		conProps.put("group.id", groupId);
		conProps.put("enable.auto.commit", "true");
		conProps.put("auto.commit.interval.ms", "1000");
		conProps.put("session.timeout.ms", "30000");
		conProps.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
		conProps.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");

		Properties proProps = new Properties();
		proProps.put("bootstrap.servers", brokers);
		proProps.put("acks", "all");
		proProps.put("retries", 0);
		proProps.put("batch.size", 16384);
		proProps.put("linger.ms", 1);
		proProps.put("buffer.memory", 33554432);
		proProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		proProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		String readTopic = "firstTopic";
		String writeTopic = "secondTopic";
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(conProps);
		consumer.subscribe(Arrays.asList(readTopic));
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(proProps);


		while(true) {
			ConsumerRecords<String, String> records = consumer.poll(100);

			for (ConsumerRecord<String, String> record : records) {
				String[] msg = record.value().split(",");
//				String tmp =record.value().substring(0, record.value().indexOf(msg[msg.length-2])-1);
				String tmp =record.value().substring(0, record.value().indexOf(msg[msg.length-3])-1);

				String[] arr = tmp.split(",");
				Arrays.sort(arr);

				String str = Arrays.toString(arr);
				str = str.replace("[","");
				str = str.replace("]","");

//				str = str+","+msg[msg.length-2]+","+msg[msg.length-1];
				str = str+","+msg[msg.length-3]+","+msg[msg.length-2]+","+msg[msg.length-1];

				producer.send(new ProducerRecord<String,String>(writeTopic, str ));

			}
		}
	}
}
