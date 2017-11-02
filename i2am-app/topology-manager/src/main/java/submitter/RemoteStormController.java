package submitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.Nimbus.Client;
import org.apache.storm.generated.SubmitOptions;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.json.simple.JSONValue;

public class RemoteStormController {
			
	public static void main(String[] args) throws Exception{
		
		TopologyBuilder builder = new TopologyBuilder();
		
		Config conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "192.168.56.100"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		conf.setDebug(true);				
		
		Map storm_conf = Utils.readStormConfig();
		List<String> seeds = new ArrayList<String>();
		seeds.add("192.168.56.100");		
		storm_conf.put("nimbus.seeds", seeds); // nimbus.host > nimbus.seeds
								
		// Don't Used...
		// Client client = NimbusClient.getConfiguredClient(storm_conf).getClient();
				
		String inputJar = "D:\\topologies\\hello-world-topology.jar";
		NimbusClient nimbus = new NimbusClient(storm_conf, "192.168.56.100", 6627);
		
		// upload topology jar to Cluster using StormSubmitter
		String uploadedJarLocation = StormSubmitter.submitJar(storm_conf, inputJar);
		
		
		try {
			String jsonConf = JSONValue.toJSONString(storm_conf);
			nimbus.getClient().submitTopology("kafka_topology3", uploadedJarLocation, jsonConf, builder.createTopology());		
			
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		}
		Thread.sleep(60000);
		nimbus.close();
	}
}
