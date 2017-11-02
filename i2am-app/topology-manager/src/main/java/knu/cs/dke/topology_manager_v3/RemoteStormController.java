package knu.cs.dke.topology_manager_v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.thrift.TException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.json.simple.JSONValue;

public class RemoteStormController {

	private final static List<String> STORM_SEEDS = new ArrayList<String>() {{
		add("192.168.56.100");
	}};

	private final static String STORM_IP =  "192.168.56.100";
	private final static int STORM_PORT = 6627;

	private final static Map<String, String> JAR_DIRECTORY;
	static {	

		JAR_DIRECTORY = new HashMap<String, String>();

		String base_Directory = "D:/topologies/";

		JAR_DIRECTORY.put("HASH_SAMPLING", base_Directory + "hello-world-topology.jar");
		//JAR_DIRECTORY.put("BLOOM_SAMPLING", base_Directory + "bloom_Sampling.jar");
		// ...
	}	

	public void runTopology(String commandType, String topologyId) throws InvalidTopologyException, AuthorizationException, TException, InterruptedException {

		TopologyBuilder builder = new TopologyBuilder();

		Config conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, STORM_IP); // NIMBUS_HOSTS > NIMBUS_SEEDS
		conf.setDebug(true);				

		Map storm_conf = Utils.readStormConfig();				
		storm_conf.put("nimbus.seeds", STORM_SEEDS); // nimbus.host > nimbus.seeds

		// Don't Used...
		// Client client = NimbusClient.getConfiguredClient(storm_conf).getClient();

		String inputJar = JAR_DIRECTORY.get(commandType);
		NimbusClient nimbus = new NimbusClient(storm_conf, STORM_IP, STORM_PORT);

		// upload topology jar to Cluster using StormSubmitter
		String uploadedJarLocation = StormSubmitter.submitJar(storm_conf, inputJar);	

		try {
			String jsonConf = JSONValue.toJSONString(storm_conf);
			nimbus.getClient().submitTopology(topologyId, uploadedJarLocation, jsonConf, builder.createTopology());		

		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		}
		Thread.sleep(3000);
		nimbus.close();
	}
}
