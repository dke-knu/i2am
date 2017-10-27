package i2am.benchmark.storm.reservoir;

import java.util.Properties;
import java.util.UUID;

import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

public class PerformanceTestTopology {
	
	// private static fina Logger LOG = LoggerFactory.getLoggerFactory.getLogger(PerformanceTestTopology.class); 
	
	public static void main(String[] args){
		String[] zookeepers = args[0].split(",");
		short zkPort = Short.parseShort(args[1]);
		
		/* Kafka -> Storm Config */
		StringBuilder sb = new StringBuilder();
		for(String zookeeper: zookeepers){
			sb.append(zookeeper + ":" + zkPort + ",");
		}
		
		String zkUrl = sb.substring(0, sb.length()-1);
		
		String input_topic = args[2];
		String output_topic = args[3];
		
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
		builder.setSpout("kafka-spout", new KafkaSpout(kafkaSpoutConfig), 1).setNumTasks(1);
		
	}
}
