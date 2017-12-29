package knu.cs.dke.topology_manager.topolgoies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import org.apache.storm.Config;
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

public class RemoteStormController {

	// Storm Conf.
	private TopologyBuilder builder;
	private NimbusClient nimbus;
	private Config conf;
	private Map storm_conf;	

	private String sampling_jarDirectory = "$STORM_HOME/Sampling.jar";
	private String sampling_class = "i2am.Common.SamplingTopology";

	private String filtering_jarDirectory = "$STORM_HOME/Filtering.jar";
	private String filtering_class = "i2am.Common.FilteringTopology";

	public RemoteStormController() throws TTransportException  {
		// Storm Conf.
		builder = new TopologyBuilder();
		conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "MN"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		storm_conf = Utils.readStormConfig();		
		storm_conf.put("nimbus.seeds", Arrays.asList("MN")); // nimbus.host > nimbus.seeds
		NimbusClient nimbus = new NimbusClient(storm_conf, "MN", 6627);	
	}

	public void runTopology(ASamplingFilteringTopology topology) throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {

		String topologyName = topology.getTopologyName();		
		String redisKey = topology.getRedisKey();
		String topologyType = topology.getTopologyType();		

		String jarDirectory = null;
		String algorithmType = null;

		if(topologyType.contains("SAMPLING")) {

			jarDirectory = sampling_jarDirectory;
			algorithmType = sampling_class;			
		}
		else if (topologyType.contains("FILTERING")) {

			jarDirectory = filtering_jarDirectory;
			algorithmType = filtering_class;
		}
		else {

			System.out.println("[Storm Remote Controller] Sampling? Filtering?");
		}

		String[] command = 
			{
					"/bin/sh",		
					"-c",
					"$STORM_HOME/bin/storm jar" + " "
							+ jarDirectory + " "
							+ algorithmType + " "
							+ topologyName + " "
							+ redisKey + " "
							+ topologyType
			};	

		System.out.println("[Topology] Run Topology : " + topologyName);

		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command);
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		while((line = br.readLine()) != null) {
			System.out.println(line);
		}

	}

	public void killTopology(String topologyName) throws NotAliveException, AuthorizationException, TException, InterruptedException {

		try {			
			KillOptions options = null;
			options.set_wait_secs(10);
			System.out.println("[Topology] Kill Topology : " + topologyName);
			nimbus.getClient().killTopologyWithOpts(topologyName, options);
			Thread.sleep(3000);

		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		} 
		nimbus.close();		
	}

	public void activateTopology(String topologyName) throws NotAliveException, AuthorizationException, TException, InterruptedException {
		try {			
			System.out.println("[Topology] Active Topology : " + topologyName);
			nimbus.getClient().activate(topologyName);
			Thread.sleep(3000);
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		} 
		nimbus.close();			
	}

	public void deactivateTopology(String topologyName) throws NotAliveException, AuthorizationException, TException, InterruptedException {
		try {			
			System.out.println("[Topology] Deactive Topology : " + topologyName);
			nimbus.getClient().deactivate(topologyName);			
			Thread.sleep(3000);
		} catch (AlreadyAliveException ae) {
			ae.printStackTrace();
		} 
		nimbus.close();
	}	
}
