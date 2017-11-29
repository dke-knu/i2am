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

	public I2AMConsumer(String id, String dstName) {
		String brokers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,"
				+ "114.70.235.43:19095,114.70.235.43:19096,114.70.235.43:19097,"
				+ "114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";
		String topic = getInputTopic(id, dstName);

		Properties props = new Properties();
		props.put("bootstrap.servers", brokers);
		props.put("group.id", "test");
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
	
	public String getInputTopic(String id, String dstName) {
		return DbAdapter.getInstance().getOutputTopic(id, dstName);
	}
}