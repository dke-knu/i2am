package i2am.benchmark.kafka;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
			// Old version: random sentence.
			// String sentence = randomSentence() +","+ production +","+ System.currentTimeMillis();
			
			// New version: random tweet.
			JSONObject jSentence = new JSONObject();
			jSentence.put("tweet", randomTweet());
			jSentence.put("production", production);
			jSentence.put("createdTime", System.currentTimeMillis());
			String sentence = jSentence.toJSONString();
			producer.send(new KeyedMessage<String, String>(this.topic, sentence));
			
			try {
				Thread.sleep(this.interval);
			} catch (InterruptedException e) {}
		}
	}
	
	public String randomSentence() {
		return sentences[rand.nextInt(sentences.length)];
	}
	
	public JSONObject randomTweet() {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(
					new FileReader("/data/tweetdata1000/twit_data" + (rand.nextInt(sentences.length)+1) + ".json"));
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
}



