package knu.cs.dke.topology_manaver_test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.json.simple.JSONValue;

public class Stormsubmitter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		
		
		// Storm Conf.
		TopologyBuilder builder;
		NimbusClient nimbus = null;
		Config conf;
		Map storm_conf;			
		
		builder = new TopologyBuilder();
		conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "114.70.235.43"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		storm_conf = Utils.readStormConfig();		
		storm_conf.put("nimbus.seeds", Arrays.asList("114.70.235.43")); // nimbus.host > nimbus.seeds
		
		
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

			System.out.println(nimbus.getClient().isTopologyNameAllowed("topologyName"));


		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
