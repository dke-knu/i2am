package i2am.benchmark.spark.reservoir;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;


public class SparkReservoirTest {	

	private final static Logger logger = Logger.getLogger(SparkReservoirTest.class);
	//private static JedisPool pool;	

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

		// Sampling Parameters.
		int sample_size = Integer.parseInt(args[6]);
		int window_size = Integer.parseInt(args[7]);

		// Redis conf.
		String redis_key = args[8];

		Set<HostAndPort> redisNodes = new HashSet<HostAndPort>();
		redisNodes.add(new HostAndPort("192.168.0.100", 17000));
		redisNodes.add(new HostAndPort("192.168.0.101", 17001));
		redisNodes.add(new HostAndPort("192.168.0.102", 17002));
		redisNodes.add(new HostAndPort("192.168.0.103", 17003));
		redisNodes.add(new HostAndPort("192.168.0.104", 17004));
		redisNodes.add(new HostAndPort("192.168.0.105", 17005));
		redisNodes.add(new HostAndPort("192.168.0.106", 17006));
		redisNodes.add(new HostAndPort("192.168.0.107", 17007));
		redisNodes.add(new HostAndPort("192.168.0.108", 17008));		

		// Context.
		SparkConf conf = new SparkConf().setAppName("Reservoir-Sampling-Test");
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

		// Step 2. Sampling.
		timeLines.foreachRDD( samples -> {

			samples.foreach( sample -> {

				JedisCluster jc = new JedisCluster(redisNodes);				

				String[] commands = sample.split(",");

				int index = Integer.parseInt(commands[1]);
				int prob = sample_size + 1;
				int count = index % window_size;				

				if( count != 0 && count < sample_size ) {

					jc.rpush(redis_key, sample);
				}
				else if( count == 0 ) { // Window Size.

					KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
					List<String> sampleList = jc.lrange(redis_key, 0, -1);
					jc.ltrim(redis_key, 0, -99999);

					for( String result: sampleList ) {					
						producer.send(new ProducerRecord<String, String>(output_topic, "1:" + result + "," + System.currentTimeMillis()));
					}
					producer.close();
				}	
				else {
					
					prob = (int)(Math.random()*count);
					KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

					if ( prob < sample_size ) {
						if ( prob < jc.llen(redis_key) ) {
							
							String notSample = "0:" + jc.lindex(redis_key, prob) + "," + System.currentTimeMillis();
							jc.lset(redis_key, prob, sample);						
							producer.send(new ProducerRecord<String, String>(output_topic, notSample));
						}
						else {
							jc.rpush(redis_key, sample);
						}
					}
					else {												
						producer.send(new ProducerRecord<String, String>(output_topic, "0:" + sample + "," + System.currentTimeMillis()));
					}
					producer.close();
				}				
				jc.close();
			});				
		});	

		// Start.
		jssc.start();
		jssc.awaitTermination();		
		//producer.close();
		//pool.close();		
	}		
}
