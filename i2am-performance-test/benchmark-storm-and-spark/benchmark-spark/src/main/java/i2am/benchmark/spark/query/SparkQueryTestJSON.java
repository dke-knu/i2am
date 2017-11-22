package i2am.benchmark.spark.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SparkQueryTestJSON {	

	private final static Logger logger = Logger.getLogger(SparkQueryTestJSON.class);	

	public static void main(String[] args) throws InterruptedException {
		
		// Args.
		String input_topic = args[0];
		String output_topic = args[1];
		String group = args[2];
		long duration = Long.valueOf(args[3]);

		// MN.eth 9092.
		String[] zookeepers = args[4].split(","); //KAFAK ZOOKEEPER
		short zkPort = Short.parseShort(args[5]);
		
		
		StringBuilder sb = new StringBuilder();
		for(String zookeeper : zookeepers){
			sb.append(zookeeper + ":" + zkPort +",");
		}
		
		String kafkaUrl = sb.substring(0, sb.length()-1);
		
		/*
		String zookeeper_ip = args[4];
		String zookeeper_port = args[5];
		String zk = zookeeper_ip + ":" + zookeeper_port;
		*/


		// Filtering Keywords.				
		String[] input_keywords = args.clone();
		String[] keywords = Arrays.copyOfRange(input_keywords, 6, input_keywords.length);		

		// Context.
		SparkConf conf = new SparkConf().setAppName("Query-Filtering-Test-With-JSON");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaStreamingContext jssc = new JavaStreamingContext(sc, Durations.milliseconds(duration));

		// BroadCast Variables
		Broadcast<List<String>> filter = sc.broadcast(Arrays.asList(keywords));		

		// Kafka Parameter.
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", kafkaUrl);
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", group);
		kafkaParams.put("auto.offset.reset", "earliest");
		kafkaParams.put("enable.auto.commit", false);

		// Make Kafka Producer.		
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaUrl);
		props.put("acks", "1");	
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");		

		// Topics.
		Collection<String> topics = Arrays.asList(input_topic);

		// Streams.
		final JavaInputDStream<ConsumerRecord<String,String>> stream =
				KafkaUtils.createDirectStream(
						jssc,
						LocationStrategies.PreferConsistent(),
						ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
						);		

		// Processing.		

		// Step 1. Current Time.
		// JSON + Input Time.
		JavaDStream<String> lines = stream.map(ConsumerRecord::value);		
		JavaDStream<String> timeLines = lines.map(line -> {	
			
			JSONParser parser = new JSONParser();
			JSONObject messages = (JSONObject) parser.parse(line); 			
			messages.put("inputTime", System.currentTimeMillis());						
			return messages.toJSONString();
		});		


		// Step 2. Filtering for String.
		JavaDStream<String> filtered = timeLines.map( sample -> {	

			JSONParser parser = new JSONParser();
			JSONObject messages = (JSONObject) parser.parse(sample);
			JSONObject tweet = (JSONObject) messages.get("tweet");
			String text = (String) tweet.get("text");			

			for ( String keyword: filter.value() ) {				
				if( text.contains(keyword) ) {
					messages.put("sampleFlag", 1);
					return messages.toString();
				}			
			}				
			messages.put("sampleFlag", 0);
			return messages.toString();
		});

		// Step 3. Out > Kafka, Redis
		filtered.foreachRDD( samples -> {

			samples.foreach( sample -> {
				KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
				JSONParser parser = new JSONParser();
				JSONObject messages = (JSONObject) parser.parse(sample);
				messages.put("outputTime", System.currentTimeMillis());
				producer.send(new ProducerRecord<String, String>(output_topic, messages.toString()));
				producer.close();
			});				
		});	

		// Start.
		jssc.start();
		jssc.awaitTermination();		
		//producer.close();
		//pool.close();		
	}		
}
