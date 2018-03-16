package submitter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.shade.org.json.simple.JSONValue;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

public class RemoteSubmitter {

	public static void main(String[] args) throws Exception{

		// Configuration.
		Config topologyConf = new Config();
		topologyConf.setDebug(true);
		topologyConf.setNumWorkers(2);

		Map defaultConf = Utils.readStormConfig();

		Map conf = Utils.readStormConfig();		
		
		List<String> seeds = new ArrayList<String>();
		seeds.add("192.168.56.100");
		conf.put(Config.NIMBUS_SEEDS, seeds);		
		//conf.put(Config.NIMBUS_HOST, "192.168.56.100");
		conf.put(Config.NIMBUS_THRIFT_PORT, 6627);
		conf.put(Config.STORM_THRIFT_TRANSPORT_PLUGIN, defaultConf.get(Config.STORM_THRIFT_TRANSPORT_PLUGIN));	     

		conf.putAll(topologyConf);

		// Code Upload.
		String inputJar = "D:\\topologies\\test3.jar";
		String remoteJar = StormSubmitter.submitJar(conf, inputJar);

		System.out.println(remoteJar);
		
		// Topology Submit.
		StormTopology topology = getTopology(String.format("file://" + "%s", remoteJar),
			"test.wordcount.WordCountTopology", "buildTopology");
				
		// Nimbus
		NimbusClient client = NimbusClient.getConfiguredClient(conf);
		
		
		try {
			client.getClient().submitTopology("my-topology", inputJar, JSONValue.toJSONString(conf), topology);			
			
		} catch ( AlreadyAliveException e ) {			
			e.printStackTrace();
		}
	}

	private static StormTopology getTopology(String path, String className, String methodName) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		System.out.println(path);
		
		ClassLoader loader = URLClassLoader.newInstance(new URL[] { new URL(path) });		
		
		Class<?> clazz = loader.loadClass(className);
		Method method = clazz.getMethod(methodName, new Class[] {});
		
		return (StormTopology) method.invoke(null, new Object[] {});
	}
}
