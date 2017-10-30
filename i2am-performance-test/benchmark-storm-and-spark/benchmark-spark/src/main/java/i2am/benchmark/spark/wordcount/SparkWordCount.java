package i2am.benchmark.spark.wordcount;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

public class SparkWordCount {	

	private final static Logger logger = Logger.getLogger(PerformanceTestSpark.class);

	public static void main(String[] args) throws InterruptedException {

		// Args.
		String input_topic = args[0];
		String output_topic = args[1];
		String group = args[2];
		long duration = Long.valueOf(args[3]);

		// MN.eth 9092
		String zookeeper_ip = args[4];
		String zookeeper_port = args[5];
		String zk = zookeeper_ip + ":" + zookeeper_port;

		// Context.
		SparkConf conf = new SparkConf().setAppName("kafka-test");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaStreamingContext jssc = new JavaStreamingContext(sc, Durations.milliseconds(duration));

		// Kafka Parameter.
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", zk);
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", group);
		kafkaParams.put("auto.offset.reset", "earliest");
		kafkaParams.put("enable.auto.commit", false);

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
		JavaDStream<String> lines = stream.map(ConsumerRecord::value);		
		JavaDStream<String> timeLines = lines.map(line -> line + "," + System.currentTimeMillis());

		// Step 2. Word Count.
		JavaDStream<String> wordCounts = timeLines.map( line -> {

			System.out.println("##### " + line);
			logger.error("@@@@@ " + line);

			String[] commands = line.split(","); // Split Command 
			String[] words = commands[0].split(" "); // Split Sentence

			// Word Count
			Map<String, Integer> counts = new TreeMap<String, Integer>();

			for(String word: words) {							
				Integer count = counts.get(word);
				if ( count == null ) count = 0;
				count++;
				counts.put(word, count);							
			}						

			// Formatting
			String output = "{";
			Boolean first = true;		

			for(String key: counts.keySet()) {			
				if (first) {		
					output = output + key + "=" + counts.get(key);
					first = false;				
				}
				else {
					output = output + ":" + key + "=" + counts.get(key) ;
				}									
			}

			// output = output + "}" + "," + commands[1] + "," + commands[2] + "," + commands[3] + "," + System.currentTimeMillis();
			output = output + "}" + "," + commands[1] + "," + commands[2] + "," + commands[3];
			return output;			
		});	

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

		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

		wordCounts.print();	 

		// Send to Kafka.
		wordCounts.foreachRDD(								
				output -> {										
					for( String tuple: output.collect() ) {							
						producer.send(new ProducerRecord<String, String>(output_topic, tuple+","+System.currentTimeMillis()));
					}								
				});		

		// Start.
		jssc.start();
		jssc.awaitTermination();
		producer.close();
	}		
}
