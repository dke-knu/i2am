package i2am.benchmark.spark.reservoir;

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


public class SparkReservoirTestJSON {	

	private final static Logger logger = Logger.getLogger(SparkReservoirTestJSON.class);
	//private static JedisPool pool;	

	public static void main(String[] args) throws InterruptedException, IOException {

		// Redis Cluster.
		Set<HostAndPort> redisNodes = new HashSet<HostAndPort>();
		redisNodes.add(new HostAndPort("MN", 17000));
		redisNodes.add(new HostAndPort("MN", 17001));
		redisNodes.add(new HostAndPort("MN", 17002));
		redisNodes.add(new HostAndPort("MN", 17003));
		redisNodes.add(new HostAndPort("MN", 17004));
		redisNodes.add(new HostAndPort("MN", 17005));
		redisNodes.add(new HostAndPort("MN", 17006));
		redisNodes.add(new HostAndPort("MN", 17007));
		redisNodes.add(new HostAndPort("MN", 17008));	
				
		JedisCluster jc_params = new JedisCluster(redisNodes);
				
		// Args.
		String input_topic = args[0];
		String output_topic = args[1];
		String group = args[2];
		long duration = Long.valueOf(args[3]);

		// MN.eth 9092
		String zookeeper_ip = args[4];
		String zookeeper_port = args[5];
		String zk = zookeeper_ip + ":" + zookeeper_port;

		// Get Parameters From Redis
		Map<String, String> parameters = jc_params.hgetAll(args[6]); // Redis Key.
		String sample_key = parameters.get("SampleKey");
		int sample_size = Integer.parseInt(parameters.get("SampleSize"));
		int window_size = Integer.parseInt(parameters.get("WindowSize"));		
		jc_params.ltrim(sample_key, 0, -99999);		
		
		jc_params.close();
		
		// Context.
		SparkConf conf = new SparkConf().setAppName("Reservoir-Sampling-Test-With-JSON");
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
				int prob = sample_size + 1;
				int count = index % window_size;				

				if( count != 0 && count < sample_size ) {  // 샘플이 비어있으면 무조건 뽑힘 -> Redis 저장
					jc.rpush(sample_key, sample);
				}
				else if( count == 0 ) { // Window가 가득참 -> Redis에 저장된 샘플을 Kafka 전송

					KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
					List<String> sampleList = jc.lrange(sample_key, 0, -1);
					jc.ltrim(sample_key, 0, -99999);

					JSONObject temp;					
					for( String result: sampleList ) {	
						temp = (JSONObject) parser.parse(result);
						temp.put("sampleFlag", 1);
						temp.put("outputTime", System.currentTimeMillis());
						producer.send(new ProducerRecord<String, String>(output_topic, temp.toString()));
					}
					producer.close();
				}	
				else { // 윈도우 진행중, 샘플은 가득참! -> 확률을 계산 -> 결과에 따라 Redis에 있는 데이터와 교체
					
					prob = (int)(Math.random()*count); // 확률 계산
					KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);					
					
					if ( prob < sample_size ) { // 뽑힘 -> 기존 샘플과 교체
						
						if ( jc.llen(sample_key) != 0 ) { // prob < jc.llen(sample_key) 
							
							String nonSample = jc.lpop(sample_key);
							JSONObject temp;						
							temp = (JSONObject) parser.parse(nonSample);
							
							temp.put("sampleFlag", 0);
							temp.put("outputTime", System.currentTimeMillis());							
												
							producer.send(new ProducerRecord<String, String>(output_topic, temp.toString()));
						}
						jc.rpush(sample_key, sample);
					}
					else { // 안뽑힘
						messages.put("sampleFlag", 0);
						messages.put("outputTime", System.currentTimeMillis());
						producer.send(new ProducerRecord<String, String>(output_topic, messages.toString()));
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
