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

	public static void main(String[] args) throws AuthorizationException, TException {
		// TODO Auto-generated method stub
	
		// Storm Conf.		
		NimbusClient nimbus = null;
		Config conf;
		Map storm_conf;			

		conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "114.70.235.43"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		
		storm_conf = Utils.readStormConfig();
		storm_conf.put("nimbus.seeds", Arrays.asList("114.70.235.43")); // nimbus.host > nimbus.seeds
		
		
		
				
		// Topology 
		TopologyBuilder builder = new TopologyBuilder();
		
		try { // 스톰 클러스터에 접속하기
			System.out.println("연결 중...");
			nimbus = new NimbusClient(storm_conf, "114.70.235.43", 6627);			
			
			System.out.println(nimbus.getClient().isTopologyNameAllowed("gg"));

		} catch (TTransportException e1) {
			System.out.println("연결 실패!");
			e1.printStackTrace();
		}
		System.out.println("연결됨");

		


	}

}
