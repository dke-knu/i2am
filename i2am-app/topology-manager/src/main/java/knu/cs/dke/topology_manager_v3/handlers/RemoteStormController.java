package knu.cs.dke.topology_manager_v3.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.KillOptions;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.json.simple.JSONValue;

public class RemoteStormController {

	private TopologyBuilder builder;
	private NimbusClient nimbus;
	private Config conf;
	private Map storm_conf;
	
	private final static Map<String, String> JAR_DIRECTORY;
	static {	

		JAR_DIRECTORY = new HashMap<String, String>();

		String base_Directory = "D:/topologies/";
		// For linux;
		// String base_Directory = "$STORM_HOME/topologies/";
		// String command_base = "$STORM_HOME/bin/storm jar";
		
		JAR_DIRECTORY.put("HASH_SAMPLING", base_Directory + "hello-world-topology.jar");
		//JAR_DIRECTORY.put("BLOOM_SAMPLING", base_Directory + "bloom_Sampling.jar");
		// ...
	}

	public RemoteStormController() throws TTransportException  {
		
		builder = new TopologyBuilder();

		conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "MN"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		conf.setDebug(true);				

		storm_conf = Utils.readStormConfig();				
		storm_conf.put("nimbus.seeds", Arrays.asList("MN")); // nimbus.host > nimbus.seeds

		// Don't Used...
		// Client client = NimbusClient.getConfiguredClient(storm_conf).getClient();
		
		NimbusClient nimbus = new NimbusClient(storm_conf, "MN", 6627);		
	}
	
	public void runTopology(String topology, String topologyId) throws InvalidTopologyException, AuthorizationException, TException, InterruptedException {

		String inputJar = JAR_DIRECTORY.get(topology);
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
	
	public void killTopology(String topology) throws NotAliveException, AuthorizationException, TException, InterruptedException {				
		try {			
			KillOptions options = null;
			options.set_wait_secs(10);
			nimbus.getClient().killTopologyWithOpts(topology, options);
			Thread.sleep(3000);
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		} 
		nimbus.close();		
	}
	
	public void activateTopology(String topology) throws NotAliveException, AuthorizationException, TException, InterruptedException {
		try {			
			nimbus.getClient().activate(topology);
			Thread.sleep(3000);
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		} 
		nimbus.close();			
	}
	
	public void deactivateTopology(String topology) throws NotAliveException, AuthorizationException, TException, InterruptedException {
		try {			
			nimbus.getClient().deactivate(topology);
			Thread.sleep(3000);
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		} 
		nimbus.close();
	}
}
