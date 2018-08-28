package knu.cs.dke.topology_manaver_test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.shade.org.json.simple.JSONValue;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

public class RemoteStormSubmitter {

	public static void main(String[] args) {
				
		// Parameter		
		String topologyFile = "D:\\i2am-algorithm-filtering.jar";
		String topologyName = "testTopology";
		String nimbusNode = "114.70.235.43";
		
		// Configuration
		TopologyBuilder builder = new TopologyBuilder();

		Config conf = new Config();
		conf.put(Config.NIMBUS_SEEDS, nimbusNode);
		conf.setDebug(true);
		
		Map stormConfig = Utils.readStormConfig();	
		stormConfig.put("nimbus.seeds", Arrays.asList(nimbusNode));
		
		// Connect
		NimbusClient nimbus;		
		
			try {
				
				System.out.println("연결 중...");
				nimbus = new NimbusClient(stormConfig, nimbusNode, 6627);				
				String submittedJar = StormSubmitter.submitJar(stormConfig, topologyFile);
												
				try {
					String jsonConfig = JSONValue.toJSONString(stormConfig);
					nimbus.getClient().submitTopology(topologyName, submittedJar, jsonConfig, builder.createTopology());
					
					
				} catch (AlreadyAliveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidTopologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthorizationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		

	}

}
