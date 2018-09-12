package i2am.sampling.common;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import i2am.sampling.declaring.HashDeclaringBolt;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
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

import i2am.sampling.BBSCoordinatorBolt;
import i2am.sampling.BBSSiteBolt;
import i2am.sampling.HashSamplingBolt;
import i2am.sampling.KSampleBolt;
import i2am.sampling.PrioritySamplingBolt;
import i2am.sampling.ReservoirSamplingBolt;
import i2am.sampling.SystematicSamplingBolt;
import i2am.sampling.UCKSampleBolt;
import i2am.sampling.declaring.BBSDeclaringBolt;
import i2am.sampling.declaring.DeclaringBolt;
import i2am.sampling.declaring.PriorityDeclaringBolt;
import i2am.sampling.passing.PassingBolt;
import i2am.sampling.passing.SystematicPassingBolt;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Protocol;

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
        redisNodes.add(new InetSocketAddress("SN01", 17000));
        redisNodes.add(new InetSocketAddress("SN02", 17000));
        redisNodes.add(new InetSocketAddress("SN03", 17000));
        redisNodes.add(new InetSocketAddress("SN04", 17000));
        redisNodes.add(new InetSocketAddress("SN05", 17000));
        redisNodes.add(new InetSocketAddress("SN06", 17000));
        redisNodes.add(new InetSocketAddress("SN07", 17000));
        redisNodes.add(new InetSocketAddress("SN08", 17000));

        /* Jedis */
        JedisClusterConfig jedisClusterConfig = null;
        JedisCommandsInstanceContainer jedisContainer = null;
        JedisCommands jedisCommands = null;

        /* Jedis Connection Configuration */
        jedisClusterConfig = new JedisClusterConfig(redisNodes, Protocol.DEFAULT_TIMEOUT, 5);
        jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
        jedisCommands = jedisContainer.getInstance();

        /* Get all parameters from Redis */
        Map<String, String> allParameters = jedisCommands.hgetAll(redisKey);

        /* Kafka Spout Configuration */
        BrokerHosts brokerHosts = new ZkHosts("MN:22181");
        String inputTopic = allParameters.get("InputTopic");
        SpoutConfig kafkaSpoutConfig = new SpoutConfig(brokerHosts, inputTopic, "/"+inputTopic, UUID.randomUUID().toString());
        kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        kafkaSpoutConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();
        kafkaSpoutConfig.ignoreZkOffsets = true;
        kafkaSpoutConfig.maxOffsetBehind = 0;
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
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<String, String>("key", "data"));

        /* Topology Configuration */
        TopologyBuilder topologyBuilder = new TopologyBuilder();

        /* KafkaSpout */
        topologyBuilder.setSpout("KAFKA_SPOUT", kafkaSpout, 1)
                .setNumTasks(1);

        /* DeclaringBolt */
        if(algorithmName.equals("RESERVOIR_SAMPLING")){
            topologyBuilder.setBolt("DECLARING_BOLT", new DeclaringBolt(topologyName, redisKey, jedisClusterConfig), 1)
                    .shuffleGrouping("KAFKA_SPOUT")
                    .setNumTasks(1);
        }
        else if(algorithmName.equals("HASH_SAMPLING")){
            topologyBuilder.setBolt("DECLARING_BOLT", new HashDeclaringBolt(topologyName, redisKey, jedisClusterConfig), 1)
                    .shuffleGrouping("KAFKA_SPOUT")
                    .setNumTasks(1);
        }
        else if(algorithmName.equals("PRIORITY_SAMPLING")){
            topologyBuilder.setBolt("DECLARING_BOLT", new PriorityDeclaringBolt(topologyName, redisKey, jedisClusterConfig), 1)
                    .shuffleGrouping("KAFKA_SPOUT")
                    .setNumTasks(1);
        }
        else if(algorithmName.equals("BINARY_BERNOULLI_SAMPLING")){
            topologyBuilder.setBolt("DECLARING_BOLT", new BBSDeclaringBolt(redisKey, jedisClusterConfig), 1)
                    .shuffleGrouping("KAFKA_SPOUT")
                    .setNumTasks(1);
        }

        /* SamplingBolt */
        switch (algorithmName) {
            case "SYSTEMATIC_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_BOLT", new SystematicSamplingBolt(redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("KAFKA_SPOUT")
                        .setNumTasks(4);
                break;
            case "K_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_BOLT", new KSampleBolt(redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("KAFKA_SPOUT")
                        .setNumTasks(4);
                break;
            case "UC_K_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_BOLT", new UCKSampleBolt(redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("KAFKA_SPOUT")
                        .setNumTasks(4);
                break;
            case "RESERVOIR_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_BOLT", new ReservoirSamplingBolt(redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("DECLARING_BOLT")
                        .setNumTasks(4);
                break;
            case "HASH_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_BOLT", new HashSamplingBolt(topologyName, redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("DECLARING_BOLT")
                        .setNumTasks(4);
                break;
            case "PRIORITY_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_BOLT", new PrioritySamplingBolt(redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("DECLARING_BOLT")
                        .setNumTasks(4);
                break;
            case "BINARY_BERNOULLI_SAMPLING":
                topologyBuilder.setBolt(algorithmName + "_SITE_BOLT", new BBSSiteBolt(redisKey, jedisClusterConfig), 4)
                        .shuffleGrouping("DECLARING_BOLT")
                        .setNumTasks(4);
                topologyBuilder.setBolt(algorithmName + "_BOLT", new BBSCoordinatorBolt(redisKey, jedisClusterConfig), 2)
                        .shuffleGrouping(algorithmName + "_SITE_BOLT")
                        .setNumTasks(2);
                break;
        }

        /* PassingBolt and KafkaBolt */
        if(algorithmName.equals("SYSTEMATIC_SAMPLING") || algorithmName.equals("K_SAMPLING") || algorithmName.equals("UC_K_SAMPLING")){
            topologyBuilder.setBolt("PASSING_BOLT", new SystematicPassingBolt(), 1)
                    .shuffleGrouping(algorithmName+"_BOLT")
                    .setNumTasks(1);
        }
        else{
            topologyBuilder.setBolt("PASSING_BOLT", new PassingBolt(), 1)
                    .shuffleGrouping(algorithmName+"_BOLT")
                    .setNumTasks(1);
        }

        topologyBuilder.setBolt("KAFKA_BOLT", kafkaBolt, 1)
                .shuffleGrouping("PASSING_BOLT")
                .setNumTasks(1);

        Config config = new Config();
        config.setDebug(true);

        if(algorithmName.equals("SYSTEMATIC_SAMPLING") || algorithmName.equals("K_SAMPLING") || algorithmName.equals("UC_K_SAMPLING")){
            config.setNumWorkers(7);
        }
        else if(algorithmName.equals("BINARY_BERNOULLI_SAMPLING")){
            config.setNumWorkers(10);
        }
        else{
            config.setNumWorkers(8);
        }

        /* Submit Topology */
        StormSubmitter.submitTopology(topologyName, config, topologyBuilder.createTopology());
    }
}