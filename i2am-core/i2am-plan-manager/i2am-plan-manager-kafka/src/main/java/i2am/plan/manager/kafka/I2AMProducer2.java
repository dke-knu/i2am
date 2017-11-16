package i2am.plan.manager.kafka;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class I2AMProducer2 {
	private String brokers;
	private String topic;
	
	private Producer<String, String> producer;

	public I2AMProducer2() {
		this.brokers = "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092";
		this.topic = "swson-test";
		
		Properties props = new Properties();
		props.put("metadata.broker.list", this.brokers);
		props.put("serializer.class", "kafka.serializer.StringEncoder");

		ProducerConfig producerConfig = new ProducerConfig(props);
		producer = new Producer<String, String>(producerConfig);
	}

	public void send(String message) {
		producer.send(new KeyedMessage<String, String>(this.topic, message));
	}

	public static void main(String[] args) {
		I2AMProducer2 producer = new I2AMProducer2();
		try {
			producer.send("test22");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



