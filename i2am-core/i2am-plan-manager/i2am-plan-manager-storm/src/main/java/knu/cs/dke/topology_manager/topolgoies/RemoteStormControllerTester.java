//package knu.cs.dke.topology_manager.topolgoies;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.Arrays;
//import java.util.Map;
//
//import org.apache.storm.Config;
//import org.apache.storm.generated.AlreadyAliveException;
//import org.apache.storm.generated.AuthorizationException;
//import org.apache.storm.generated.InvalidTopologyException;
//import org.apache.storm.generated.KillOptions;
//import org.apache.storm.generated.NotAliveException;
//import org.apache.storm.thrift.TException;
//import org.apache.storm.thrift.transport.TTransportException;
//import org.apache.storm.topology.TopologyBuilder;
//import org.apache.storm.utils.NimbusClient;
//import org.apache.storm.utils.Utils;
//
//public class RemoteStormControllerTester {
//
//	// Storm Conf.
//	private TopologyBuilder builder;
//	private NimbusClient nimbus;
//	private Config conf;
//	private Map storm_conf;	
//
//	private String sampling_jarDirectory = "$STORM_HOME/Topologies/TestTopology.jar";
//	private String sampling_class = "i2am.Common.DynamicTopology";	
//
//	public RemoteStormControllerTester() throws TTransportException  {
//		// Storm Conf.
//		builder = new TopologyBuilder();
//		conf = new Config();		
//		conf.put(Config.NIMBUS_SEEDS, "114.70.235.43"); // NIMBUS_HOSTS > NIMBUS_SEEDS
//		storm_conf = Utils.readStormConfig();		
//		storm_conf.put("nimbus.seeds", Arrays.asList("114.70.235.43")); // nimbus.host > nimbus.seeds
//		nimbus = new NimbusClient(storm_conf, "114.70.235.43", 6627);	
//	}
//
//	public boolean isSubmitted(String topologyName) throws AuthorizationException, TException {
//		
//		return (!nimbus.getClient().isTopologyNameAllowed(topologyName));
//	}
//	
//	public void runTopology(ASamplingFilteringTopology topology) throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {
//
//		String topologyName = topology.getTopologyName();		
//		// String redisKey = topology.getRedisKey();
//		// String topologyType = topology.getTopologyType();		
//
//		String jarDirectory = sampling_jarDirectory; 
//		String algorithmType = sampling_class;
//
//		String[] command = 
//			{
//					"/bin/sh",		
//					"-c",
//					"$STORM_HOME/bin/storm jar" + " "
//							+ jarDirectory + " "
//							+ algorithmType + " "
//							+ topologyName + " "
//							+ topology.getInputTopic() + " "
//							+ topology.getOutputTopic()							
//			};	
//
//		System.out.println("[Topology] Run Topology : " + topologyName);
//
//		Runtime runtime = Runtime.getRuntime();
//		Process process = runtime.exec(command);
//		InputStream is = process.getInputStream();
//		InputStreamReader isr = new InputStreamReader(is);
//		BufferedReader br = new BufferedReader(isr);
//		String line;
//		while((line = br.readLine()) != null) {
//			System.out.println(line);
//		}
//	}
//
//	public void killTopology(String topologyName) throws NotAliveException, AuthorizationException, TException, InterruptedException {
//
//		try {			
//			KillOptions options = new KillOptions();
//			options.set_wait_secs(10);
//			System.out.println("[Topology] Kill Topology : " + topologyName);
//			
//			if(!nimbus.getClient().isTopologyNameAllowed(topologyName)) { // 있으면 킬				
//				nimbus.getClient().killTopologyWithOpts(topologyName, options);
//				Thread.sleep(3000);
//			}
//			else {
//				System.out.println("[Storm Controller] 토폴로지를 찾을 수 없음");
//			}					
//
//		} catch (AlreadyAliveException ae) {
//			ae.printStackTrace();
//		} 		
//	}
//
//	public void activateTopology(String topologyName) throws NotAliveException, AuthorizationException, TException, InterruptedException {
//		try {	
//			
//			System.out.println("[Topology] Active Topology : " + topologyName);
//			
//			if(!nimbus.getClient().isTopologyNameAllowed(topologyName)) { // 없으면 켜야되는데
//				// Submit
//				nimbus.getClient().activate(topologyName);
//				Thread.sleep(3000);				
//			}
//			else {
//				System.out.println("[Storm Controller] 토폴로지를 찾을 수 없음");
//			}			
//			
//		} catch (AlreadyAliveException ae) {
//			ae.printStackTrace();
//		} 			
//	}
//
//	public void deactivateTopology(String topologyName) throws NotAliveException, AuthorizationException, TException, InterruptedException {
//		try {			
//			
//			System.out.println("[Topology] Deactive Topology : " + topologyName);
//			
//			if(!nimbus.getClient().isTopologyNameAllowed(topologyName)) { // 있으면 끔
//				nimbus.getClient().deactivate(topologyName);
//				Thread.sleep(3000);
//			}
//			else {
//				System.out.println("[Storm Controller] 토폴로지를 찾을 수 없음");
//			}
//			
//		} catch (AlreadyAliveException ae) {
//			ae.printStackTrace();
//		} 		
//	}	
//}
