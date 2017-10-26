package i2am.benchmark.storm.wordcount;

import java.util.Properties;
import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
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
	
	//private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestTopology.class);

	public static void main(String[] args) throws Exception {
		String[] zookeepers = args[0].split(","); // e.g. 192.168.56.100,192.168.56.101,192.168.56.102
		short zkPort = Short.parseShort(args[1]);
		
		/* Kafka -> Storm Config */
		StringBuilder sb = new StringBuilder();
		for (String zookeeper: zookeepers) {
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
		for (String zookeeper: zookeepers) {
			sb.append(zookeeper + ":9092,");
		}
		String kafkaUrl = sb.substring(0, sb.length()-1);
		
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaUrl);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		props.put("acks", "1");

		KafkaBolt<String, Integer> kafkabolt = new KafkaBolt<String,Integer>()
				.withProducerProperties(props)
				.withTopicSelector(new DefaultTopicSelector(output_topic))
				.withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());


		TopologyBuilder builder = new TopologyBuilder();      

		builder.setSpout("kafka-spout", new KafkaSpout(kafkaSpoutConfig), 1)
			.setNumTasks(1);
		builder.setBolt("split-message-bolt", new SplitMessageBolt(), 1)
			.shuffleGrouping("kafka-spout")
			.setNumTasks(1);
		builder.setBolt("word-count-bolt", new WordCountBolt(), 1)
			.shuffleGrouping("split-message-bolt")
			.setNumTasks(1);
		builder.setBolt("sort-message-bolt", new SortMessageBolt(), 1)
			.shuffleGrouping("word-count-bolt")
			.setNumTasks(1);
		builder.setBolt("concatenate-message-bolt", new ConcatenateMessageBolt(), 1)
			.shuffleGrouping("sort-message-bolt")
			.setNumTasks(1);
		builder.setBolt("kafka-bolt", kafkabolt, 1)
			.shuffleGrouping("concatenate-message-bolt")
			.setNumTasks(1);
		
		Config conf = new Config();
		conf.setDebug(true);

		conf.setNumWorkers(6);

		StormSubmitter.submitTopology("performance-test-topology", conf, builder.createTopology());

	}
}