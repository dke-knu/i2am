package i2am.benchmark.storm.bloom;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

import i2am.benchmark.storm.reservoir.DeclareFieldBolt;
import redis.clients.jedis.Protocol;

public class PerformanceTestTopology {
	
	// private static fina Logger LOG = LoggerFactory.getLoggerFactory.getLogger(PerformanceTestTopology.class); 
	
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, AuthorizationException{
		String[] zookeepers = args[0].split(","); //KAFAK ZOOKEEPER
		short zkPort = Short.parseShort(args[1]);
		
		/* Kafka -> Storm Config */
		StringBuilder sb = new StringBuilder();
		for(String zookeeper: zookeepers){
			sb.append(zookeeper + ":" + zkPort + ",");
		}
		
		String zkUrl = sb.substring(0, sb.length()-1);
		
		String input_topic = args[2];
		String output_topic = args[3];
		
		/* Redis Configurations */
		String redisKey = args[4];
		Set<InetSocketAddress> redisNodes = new HashSet<InetSocketAddress>();
		redisNodes.add(new InetSocketAddress("192.168.1.100", 17000));
		redisNodes.add(new InetSocketAddress("192.168.1.101", 17001));
		redisNodes.add(new InetSocketAddress("192.168.1.102", 17002));
		redisNodes.add(new InetSocketAddress("192.168.1.103", 17003));
		redisNodes.add(new InetSocketAddress("192.168.1.104", 17004));
		redisNodes.add(new InetSocketAddress("192.168.1.105", 17005));
		redisNodes.add(new InetSocketAddress("192.168.1.106", 17006));
		redisNodes.add(new InetSocketAddress("192.168.1.107", 17007));
		redisNodes.add(new InetSocketAddress("192.168.1.108", 17008));
		
		/* Jedis */
		JedisClusterConfig jedisClusterConfig = new JedisClusterConfig(redisNodes, Protocol.DEFAULT_TIMEOUT, 5); 
		
		/* filter */
		List<String> dataArray = new ArrayList<String>(); //필터링 할 데이터
		for(int i = 5; i < args.length; i++){
			dataArray.add(args[i]);
		}
		
		ZkHosts hosts = new ZkHosts(zkUrl);
		
		SpoutConfig kafkaSpoutConfig = new SpoutConfig(hosts, input_topic, "/" + input_topic, UUID.randomUUID().toString());
		kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		
		/* Storm -> Kafka Configs */
		sb = new StringBuilder();
		for(String zookeeper : zookeepers){
			sb.append(zookeeper + ":9092,");
		}
		
		String kafkaUrl = sb.substring(0, sb.length()-1);
		
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaUrl);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		props.put("acks", "1");
				
		/* Topology */
		KafkaBolt<String, Integer> kafkaBolt = new KafkaBolt<String, Integer>()
				.withProducerProperties(props)
				.withTopicSelector(new DefaultTopicSelector(output_topic))
				.withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("kafka-spout", new KafkaSpout(kafkaSpoutConfig), 1)
			.setNumTasks(1);
		builder.setBolt("declare-field-bolt", new DeclareFieldBolt(), 1)
			.shuffleGrouping("kafka-spout")
			.setNumTasks(1);
		builder.setBolt("bloom-filtering-bolt", new BloomFilteringBolt(dataArray, redisKey, jedisClusterConfig), 1)
			.shuffleGrouping("declare-field-bolt")
			.setNumTasks(1);
		builder.setBolt("kafka-bolt", kafkaBolt, 1)
			.shuffleGrouping("bloom-filtering-bolt")
			.setNumTasks(1);
		
		Config conf = new Config();
		conf.setDebug(true);

		conf.setNumWorkers(5);

		StormSubmitter.submitTopology("performance-filtering-topology", conf, builder.createTopology());
	}
}
