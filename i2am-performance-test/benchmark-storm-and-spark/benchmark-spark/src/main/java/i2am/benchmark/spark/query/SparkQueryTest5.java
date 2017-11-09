package i2am.benchmark.spark.query;

import java.util.ArrayList;
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


public class SparkQueryTest5 {	

	private final static Logger logger = Logger.getLogger(SparkQueryTest5.class);	

	public static void main(String[] args) throws InterruptedException {

		// Args.
		String input_topic = args[0];
		String output_topic = args[1];
		String group = args[2];
		long duration = Long.valueOf(args[3]);

		// MN.eth 9092.
		String zookeeper_ip = args[4];
		String zookeeper_port = args[5];
		String zk = zookeeper_ip + ":" + zookeeper_port;

		// Filtering Keywords.				
		String[] input_keywords = args.clone();
		String[] keywords = Arrays.copyOfRange(input_keywords, 6, input_keywords.length);		

		// Context.
		SparkConf conf = new SparkConf().setAppName("kafka-test");
		conf.set("spark.streaming.concurrentJobs", "4");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaStreamingContext jssc = new JavaStreamingContext(sc, Durations.milliseconds(duration));
		
				

		// BroadCast Variables
		Broadcast<List<String>> filter = sc.broadcast(Arrays.asList(keywords));		

		// Kafka Parameter.
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", zk);
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", group);
		kafkaParams.put("auto.offset.reset", "earliest");
		kafkaParams.put("enable.auto.commit", false);

		// Make Kafka Producer.		
		Properties props = new Properties();
		props.put("bootstrap.servers", zk);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");		

		// Topics.
		Collection<String> topics = Arrays.asList(input_topic);

		// Streams.
		int numStreams = 8;
		
		List<JavaInputDStream<ConsumerRecord<String,String>>> kafkaStreams = new ArrayList<>(numStreams);
		
		for ( int i=0; i<numStreams; i++ ) {
			kafkaStreams.add(KafkaUtils.createDirectStream(
						jssc,
						LocationStrategies.PreferConsistent(),
						ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)));			
		}
		
		JavaDStream<ConsumerRecord<String, String>> subStreams = (JavaDStream<ConsumerRecord<String, String>>) kafkaStreams.subList(1, kafkaStreams.size());
		JavaDStream<ConsumerRecord<String,String>> stream = kafkaStreams.get(0).union(subStreams);
		
		
		// Processing.		

		// Step 1. Current Time.
		JavaDStream<String> lines = stream.map(ConsumerRecord::value);		
		JavaDStream<String> timeLines = lines.map(line -> line + "," + System.currentTimeMillis());

		// Step 2. Filtering for String.
		JavaDStream<String> filtered = timeLines.map( sample -> {			
			String[] commands = sample.split(",");			
			for ( String keyword: filter.value() ) {				
				if( commands[0].contains(keyword) ) {					
					return "1:" + sample;
				}							
			}			
			return "0:" + sample;
		});

		// Step 3. Out > Kafka, Redis
		filtered.foreachRDD( samples -> {

			samples.foreach( sample -> {

				KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);				
				String out = sample + "," + System.currentTimeMillis();				
				producer.send(new ProducerRecord<String, String>(output_topic, out));		
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
