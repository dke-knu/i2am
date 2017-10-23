package i2am.benchmark;

import java.util.Properties;
import java.util.Random;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class ProducerRunner implements Runnable {
	private String brokers;
	private String topic;
	private int interval;
	
	private Random rand;
	private String[] sentences = new String[]{
			"the cow jumped over the moon",
			"an apple a day keeps the doctor away",
	        "four score and seven years ago",
	        "snow white and the seven dwarfs",
	        "i am at two with nature"};
	
	public ProducerRunner(String[] brokers, short kafkaPort, String topic, int interval) {
		// TODO Auto-generated constructor stub
		StringBuilder sb = new StringBuilder();
		for (String broker: brokers) {
			sb.append(broker + ":" + kafkaPort + ",");
		}
		this.brokers = sb.substring(0, sb.length()-1);
		this.topic = topic;
		this.interval = interval;
		this.rand = new Random();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Properties props = new Properties();
		props.put("metadata.broker.list", this.brokers);
		props.put("serializer.class", "kafka.serializer.StringEncoder");

		ProducerConfig producerConfig = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(producerConfig);

		for (int production=1; ; production++) {
			String sentence = sentences[rand.nextInt(sentences.length)] +","+ production +","+ System.currentTimeMillis();
			producer.send(new KeyedMessage<String, String>(this.topic, sentence));
			
			try {
				Thread.sleep(this.interval);
			} catch (InterruptedException e) {}
		}
	}
}



