package i2am.plan.manager.kafka;

import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class I2AMConsumer {

	private KafkaConsumer<String, String> consumer;

	public I2AMConsumer() {
		String brokers = "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092";
		String topic = "swson-test";
		String groupId = "test";

		Properties props = new Properties();
		props.put("bootstrap.servers", brokers);
		props.put("group.id", groupId);
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer",          
				"org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", 
				"org.apache.kafka.common.serialization.StringDeserializer");
		this.consumer = new KafkaConsumer<String, String>(props);
		this.consumer.subscribe(Arrays.asList(topic));
	}

	public void receive(Queue<String> qMessages) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(100);
					for (ConsumerRecord<String, String> record : records) {
						qMessages.offer(record.value());
					}
				}  
			}
		});
		t.start();
	}

	public static void main(String[] args) {
		Queue<String> q = new LinkedBlockingQueue<String>(100); 
		new I2AMConsumer().receive(q);
		
		while (true) {
			String message;
			do {
				message = q.poll();
			} while(message == null);
			System.out.println( message );
		}
	}

}