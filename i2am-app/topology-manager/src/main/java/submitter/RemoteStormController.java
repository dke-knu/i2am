package submitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.SubmitOptions;
import org.apache.storm.generated.TopologyInitialStatus;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.json.simple.JSONValue;

public class RemoteStormController {
			
	public static void main(String[] args) throws Exception{
		
		TopologyBuilder builder = new TopologyBuilder();
				
		Config conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "192.168.56.100"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		conf.put(Config.NIMBUS_THRIFT_PORT, 6627);
		conf.put(Config.STORM_ZOOKEEPER_PORT, 2181);
		conf.put(Config.STORM_ZOOKEEPER_SERVERS, "MN");
		//conf.put(Config., value)
		
		conf.setDebug(true);
		conf.put("myParameter", "myParameter-Value");
				
		Map storm_conf = Utils.readStormConfig();
		List<String> seeds = new ArrayList<String>();
		seeds.add("192.168.56.100");		
		storm_conf.put("nimbus.seeds", seeds); // nimbus.host > nimbus.seeds
								
		// Don't Used...
		// Client client = NimbusClient.getConfiguredClient(storm_conf).getClient();
				
		String inputJar = "D:\\topologies\\word_topology.jar";
		NimbusClient nimbus = new NimbusClient(storm_conf, "MN", 6627);
		
		//StormCluster sc = new StormCluster();
		
		// upload topology jar to Cluster using StormSubmitter
		String uploadedJarLocation = StormSubmitter.submitJar(storm_conf, inputJar);
		
		
		try {
			String jsonConf = JSONValue.toJSONString(storm_conf);
			
			SubmitOptions options = new SubmitOptions();
			options.set_initial_status(TopologyInitialStatus.INACTIVE);			
			nimbus.getClient().send_submitTopologyWithOpts("word-count-topology", uploadedJarLocation, jsonConf, builder.createTopology(), options);
			//nimbus.getClient().killTopology("word-count-topology");
			//nimbus.getClient().activate("word-count-topology");
			
			
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		}
		Thread.sleep(3000);
		nimbus.close();
	}
}
