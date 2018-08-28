package knu.cs.dke.topology_manaver_test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainerBuilder;
import org.apache.storm.redis.common.container.JedisCommandsInstanceContainer;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

//import i2am.Declaring.DeclaringBolt;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Protocol;

public class Stormsubmitter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		// Storm Conf.		
		NimbusClient nimbus = null;
		Config conf;
		Map storm_conf;			

		conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "114.70.235.43"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		
		storm_conf = Utils.readStormConfig();
		storm_conf.put("nimbus.seeds", Arrays.asList(new String[] {"114.70.235.43"})); // nimbus.host > nimbus.seeds
		
		
		
				
		// Topology 
		TopologyBuilder builder = new TopologyBuilder();
		
		try { // 스톰 클러스터에 접속하기
			System.out.println("연결 중...");
			nimbus = new NimbusClient(storm_conf, "114.70.235.43", 6627);			


		} catch (TTransportException e1) {
			System.out.println("연결 실패!");
			e1.printStackTrace();
		}
		System.out.println("연결됨");


		// 명령어 보내기
		try {			
						
			//System.out.println(nimbus.getClient().isTopologyNameAllowed("topologyName"));
			
			BrokerHosts brokerHosts = new ZkHosts("MN:22181");
	        String inputTopic = "InputTopic";
	        SpoutConfig kafkaSpoutConfig = new SpoutConfig(brokerHosts, inputTopic, "/"+inputTopic, UUID.randomUUID().toString());
	        kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
	        KafkaSpout kafkaSpout = new KafkaSpout(kafkaSpoutConfig);
			
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
	        
			
			builder.setSpout("KAFKA_SPOUT", kafkaSpout, 1).setNumTasks(1);
	        
//	            builder.setBolt("DECLARING_BOLT", new DeclaringBolt("topologyName"), 1)
//	                    .shuffleGrouping("KAFKA_SPOUT")
//	                    .setNumTasks(1);
//	        			
			
			//String jsonConfig = JSONValue.toJSONString(storm_conf);			
			//nimbus.getClient().send_submitTopology("test-name", submittedJar, jsonConfig, builder.createTopology());
				        
	            
	        StormSubmitter.submitTopology("Remote-Submit", storm_conf, builder.createTopology());
			

		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
