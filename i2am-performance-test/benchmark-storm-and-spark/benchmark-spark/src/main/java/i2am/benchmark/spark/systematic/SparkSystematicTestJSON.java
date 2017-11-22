package i2am.benchmark.spark.systematic;

import java.io.IOException;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;


public class SparkSystematicTestJSON {	

	private final static Logger logger = Logger.getLogger(SparkSystematicTestJSON.class);

	public static void main(String[] args) throws InterruptedException, IOException {

		// Redis Cluster.		
		Set<HostAndPort> redisNodes = new HashSet<HostAndPort>();
		redisNodes.add(new HostAndPort("192.168.0.100", 17000));
		redisNodes.add(new HostAndPort("192.168.0.101", 17001));
		// redisNodes.add(new HostAndPort("192.168.0.102", 17002));
		redisNodes.add(new HostAndPort("192.168.0.103", 17003));
		redisNodes.add(new HostAndPort("192.168.0.104", 17004));
		redisNodes.add(new HostAndPort("192.168.0.105", 17005));
		redisNodes.add(new HostAndPort("192.168.0.106", 17006));
		// redisNodes.add(new HostAndPort("192.168.0.107", 17007));
		// redisNodes.add(new HostAndPort("192.168.0.108", 17008));		

		JedisCluster jc_params = new JedisCluster(redisNodes);

		// Args.
		String input_topic = args[0];
		String output_topic = args[1];
		String group = args[2];
		long duration = Long.valueOf(args[3]);

		// MN.eth 9092
		/*
		String zookeeper_ip = args[4];
		String zookeeper_port = args[5];
		String zk = zookeeper_ip + ":" + zookeeper_port;
		 */

		String[] zookeepers = args[4].split(","); //KAFAK ZOOKEEPER
		short zkPort = Short.parseShort(args[5]);


		StringBuilder sb = new StringBuilder();
		for(String zookeeper : zookeepers){
			sb.append(zookeeper + ":" + zkPort +",");
		}

		String kafkaUrl = sb.substring(0, sb.length()-1);


		// Sampling Parameters.
		Map<String, String> parameters = jc_params.hgetAll(args[6]);
		String sample_key = parameters.get("SampleKey");	
		int sample_size = Integer.parseInt(parameters.get("SampleSize"));
		int window_size = Integer.parseInt(parameters.get("WindowSize"));		
		jc_params.ltrim(sample_key, 0, -99999);		

		int interval = window_size/sample_size;
		int randomNumber = (int)Math.random()%interval;	

		jc_params.close();

		// Context.
		SparkConf conf = new SparkConf().setAppName("Systematic-Sampling-Test-With-JSON");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaStreamingContext jssc = new JavaStreamingContext(sc, Durations.milliseconds(duration));

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
		JavaDStream<String> lines = stream.map(ConsumerRecord::value);		
		JavaDStream<String> timeLines = lines.map(line -> {
			JSONParser parser = new JSONParser();
			JSONObject messages = (JSONObject) parser.parse(line);
			messages.put("inputTime", System.currentTimeMillis());
			return messages.toJSONString();			
		});


		// Step 2. Sampling.
		timeLines.foreachRDD( samples -> {

			samples.foreach( sample -> {

				JedisCluster jc = new JedisCluster(redisNodes);	

				JSONParser parser = new JSONParser();
				JSONObject messages = (JSONObject) parser.parse(sample);

				int index = ((Number) messages.get("production")).intValue();

				if( (index%window_size)%interval == randomNumber ) { // 샘플일 경우 -> Redis 임시 저장				
					jc.rpush(sample_key, sample);
				}				
				else { // 샘플이 아닐 경우 -> Kafka로 바로 전송
					KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);					
					messages.put("sampleFlag", 0);
					messages.put("outputTime", System.currentTimeMillis());					
					producer.send(new ProducerRecord<String, String>(output_topic, messages.toString()));					
					producer.close();
				}

				if( index % window_size == 0 ) { // 윈도우가 가득 참! -> Redis에 있는 샘플을 Kafka로 전송!

					KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
					List<String> sampleList = jc.lrange(sample_key, 0, -1);
					jc.ltrim(sample_key, 0, -99999);

					for( String result: sampleList ) {	
						JSONObject json = (JSONObject) parser.parse(result);
						json.put("sampleFlag", 1);
						json.put("outputTime", System.currentTimeMillis());
						producer.send(new ProducerRecord<String, String>(output_topic, json.toString()));
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
