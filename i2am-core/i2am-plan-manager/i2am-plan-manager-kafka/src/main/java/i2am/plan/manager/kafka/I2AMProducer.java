package i2am.plan.manager.kafka;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class I2AMProducer {
	private String brokers;
	private String topic;
	
	private Producer<String, String> producer;

	public I2AMProducer() {
		this.brokers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";
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
		I2AMProducer producer = new I2AMProducer();
		try {
			producer.send("test");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



