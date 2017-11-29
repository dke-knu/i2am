package i2am.Common;

import i2am.Sampling.SystematicSamplingBolt;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.*;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainerBuilder;
import org.apache.storm.redis.common.container.JedisCommandsInstanceContainer;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Protocol;

import java.net.InetSocketAddress;
import java.util.*;

public class SamplingTopology {
    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        /* Parameters */
        String topologyName = args[0];
        String redisKey = args[1];
        String algorithmName = args[2];

        /* Logger */
        Logger logger = LoggerFactory.getLogger(SamplingTopology.class);

        /* Redis Node Configurations */
        Set<InetSocketAddress> redisNodes = new HashSet<InetSocketAddress>();
        redisNodes.add(new InetSocketAddress("MN", 17000));
        redisNodes.add(new InetSocketAddress("SN01", 17001));
        redisNodes.add(new InetSocketAddress("SN02", 17002));
        redisNodes.add(new InetSocketAddress("SN03", 17003));
        redisNodes.add(new InetSocketAddress("SN04", 17004));
        redisNodes.add(new InetSocketAddress("SN05", 17005));
        redisNodes.add(new InetSocketAddress("SN06", 17006));
        redisNodes.add(new InetSocketAddress("SN07", 17007));
        redisNodes.add(new InetSocketAddress("SN08", 17008));

        /* Jedis */
        JedisClusterConfig jedisClusterConfig = null;
        JedisCommandsInstanceContainer jedisContainer = null;
        JedisCommands jedisCommands = null;

        /* Jedis Connection Configuration */
        jedisClusterConfig = new JedisClusterConfig(redisNodes, Protocol.DEFAULT_TIMEOUT, 5);

        /* Connect to Redis */
        if(jedisClusterConfig != null){
            jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else{
            throw new IllegalArgumentException("Jedis configuration not found");
        }

        /* Get all parameters from Redis */
        Map<String, String> allParameters = jedisCommands.hgetAll(redisKey);

        /* Kafka Spout Configuration */
        BrokerHosts brokerHosts = new ZkHosts("MN:22181");
        String inputTopic = allParameters.get("InputTopic");
        SpoutConfig kafkaSpoutConfig = new SpoutConfig(brokerHosts, inputTopic, "/"+inputTopic, UUID.randomUUID().toString());
        kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(kafkaSpoutConfig);

        /* Kafka Bolt Configuration */
        String outputTopic = allParameters.get("OutputTopic");
        Properties properties = new Properties();
        properties.put("metadata.broker.list", "MN:9092, SN01:9092, SN02:9092, SN03:9092, SN04:9092, SN05:9092, SN06:9092, SN07:9092, SN08:9092");
        properties.put("bootstrap.servers", "MN:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaBolt<String, String> kafkaBolt = new KafkaBolt<String, String>()
                .withProducerProperties(properties)
                .withTopicSelector(new DefaultTopicSelector(outputTopic))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<String, String>("", "data"));

        /* Topology Configuration */
        TopologyBuilder topologyBuilder = new TopologyBuilder();

        /* KafkaSpout */
        topologyBuilder.setSpout("KAFKA_SPOUT", kafkaSpout, 1)
                .setNumTasks(1);

        /* DeclaringBolt */
        if(algorithmName.equals("BINARY_BERNOULLI_SAMPLING")){
        }
        else{
            topologyBuilder.setBolt("DECLARING_BOLT", new DeclaringBolt(redisKey, jedisClusterConfig), 1)
                    .globalGrouping("KAFKA_SPOUT")
                    .setNumTasks(1);
        }

        /* SamplingBolt */
        if(algorithmName.equals("SYSTEMATIC_SAMPLING")){
            topologyBuilder.setBolt("SYSTEMATIC_SAMPLING", new SystematicSamplingBolt(redisKey, jedisClusterConfig), 4)
                    .shuffleGrouping("DECLARING_BOLT")
                    .setNumTasks(4);
        }

        /* PassingBolt and KafkaBolt */
        topologyBuilder.setBolt("PASSING_BOLT", new PassingBolt(), 1)
                .shuffleGrouping(algorithmName+"_BOLT")
                .setNumTasks(1);
        topologyBuilder.setBolt("KAFKA_BOLT", kafkaBolt, 1)
                .shuffleGrouping("PassingBolt")
                .setNumTasks(1);

        Config config = new Config();
        config.setDebug(true);

        if(algorithmName.equals("BINARY_BERNOULLI_SAMPLING")){
            config.setNumWorkers(8);
        }else{
            config.setNumWorkers(8);
        }

        /* Submit Topology */
        StormSubmitter.submitTopology(topologyName, config, topologyBuilder.createTopology());
    }
}
