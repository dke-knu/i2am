package knu.cs.dke.topology_manager_v3.topolgoies;

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
	
	private String jarDirectory = "$STORM_HOME/WordCountTopology.jar";	
	private String topologyClass = null;
	
	public RemoteStormController(String topologyClass) throws TTransportException  {
		// Storm Conf.
		builder = new TopologyBuilder();
		conf = new Config();		
		conf.put(Config.NIMBUS_SEEDS, "MN"); // NIMBUS_HOSTS > NIMBUS_SEEDS
		storm_conf = Utils.readStormConfig();				
		storm_conf.put("nimbus.seeds", Arrays.asList("MN")); // nimbus.host > nimbus.seeds
		NimbusClient nimbus = new NimbusClient(storm_conf, "MN", 6627);
		
		this.topologyClass = topologyClass;
	}

	public void runTopology(String topologyName, String topologyClass) throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {
		
		String[] command = 
			{
					"/bin/sh",		
					"-c",
					"$STORM_HOME/bin/storm jar" + " "
					+ jarDirectory + " "					
					+ topologyClass
					// + topologyName
					// + params
					// + 일반화 시급
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
