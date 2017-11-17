package i2am.benchmark.storm.vs.rdma;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.Nimbus.Client;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.thrift.TException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

import i2am.benchmark.storm.bloom.BloomFilteringBolt;
import i2am.benchmark.storm.query.QueryFilteringBolt;
import i2am.benchmark.storm.reservoir.ReservoirSamplingBolt;
import i2am.benchmark.storm.systematic.SystematicSamplingBolt;
import redis.clients.jedis.Protocol;

public class PerformanceTestTopology {
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, AuthorizationException{
		
		int runtime = Integer.parseInt(args[0]);
		int interval = Integer.parseInt(args[1]);
		String algorithm = args[2];
		String redisKey = args[3];
		
		/* WordList to Filter */
		List<String> dataArray = new ArrayList<String>(); 
		for(int i = 4; i < args.length; i++){
			dataArray.add(args[i]);
		}
		
		/* Redis Configurations */
		Set<InetSocketAddress> redisNodes = new HashSet<InetSocketAddress>();
		redisNodes.add(new InetSocketAddress("MN", 17000));
		redisNodes.add(new InetSocketAddress("SN01", 17001));
		redisNodes.add(new InetSocketAddress("SN02", 17002));
		redisNodes.add(new InetSocketAddress("SN03", 17003));
		redisNodes.add(new InetSocketAddress("SN04", 17004));
		redisNodes.add(new InetSocketAddress("SN05", 17005));
		redisNodes.add(new InetSocketAddress("SN06", 17006));
		redisNodes.add(new InetSocketAddress("SN07", 17007));
		redisNodes.add(new InetSocketAddress("SN08", 17008));
		
		/* Jedis */
		JedisClusterConfig jedisClusterConfig = new JedisClusterConfig(redisNodes, Protocol.DEFAULT_TIMEOUT, 5); 
		
		TopologyBuilder builder = new TopologyBuilder();
			builder.setSpout("JSON-spout", new JSONSpout(interval), 1)
				.setNumTasks(1);
		
		if(algorithm.equals("reservoir")){
			builder.setBolt("reservoir-bolt", new ReservoirSamplingBolt(redisKey, jedisClusterConfig), 4)
				.shuffleGrouping("JSON-spout")
				.setNumTasks(4);
		}else if(algorithm.equals("systematic")){
			builder.setBolt("systematic-bolt", new SystematicSamplingBolt(redisKey, jedisClusterConfig), 4)
				.shuffleGrouping("JSON-spout")
				.setNumTasks(4);
		}else if(algorithm.equals("query")){
			builder.setBolt("query-bolt", new QueryFilteringBolt(dataArray), 4)
				.shuffleGrouping("JSON-spout")
				.setNumTasks(4);
		}else if(algorithm.equals("bloom")){
			builder.setBolt("bloom-bolt", new BloomFilteringBolt(dataArray, redisKey, jedisClusterConfig), 4)
				.shuffleGrouping("JSON-spout")
				.setNumTasks(4);
		}else{
			System.out.println("Usage: Enter the right algorithm name [reservoir, systematic, bloom, hash]");
			System.exit(0);
		}
		
		builder.setBolt("JSON-aggregate-bolt", new JSONAggregateBolt(), 1)
			.globalGrouping(algorithm+"-bolt")
			.setNumTasks(1);
		
		Config conf = new Config();
		conf.setDebug(true);  
		conf.setNumWorkers(6);
		
		StormSubmitter.submitTopology("performance-"+algorithm+"-topology", conf, builder.createTopology());
		
		try{
			Map kill_conf = Utils.readStormConfig();
			NimbusClient nimbusClient = NimbusClient.getConfiguredClient(kill_conf);
			Client client = nimbusClient.getClient();
			
			//killOpts.set_wait_secs(waitSeconds); // time to wait before killing
			Thread.sleep(runtime*1000);
			
			client.killTopology("performance-"+algorithm+"-topology"); //provide topology name
		}catch(AlreadyAliveException ae){
			System.out.println(ae);
		}catch(InvalidTopologyException ie){
			System.out.println(ie);
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotAliveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
