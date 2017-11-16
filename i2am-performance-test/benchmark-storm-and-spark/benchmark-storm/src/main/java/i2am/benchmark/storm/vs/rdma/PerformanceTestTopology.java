package i2am.benchmark.storm.vs.rdma;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.topology.TopologyBuilder;

import redis.clients.jedis.Protocol;

public class PerformanceTestTopology {
	public static void main(String[] args){
		
		if(args.length != 4 || args.length != 5){
			System.out.println("Usage: PerformanceTestTopology.exe [runtime] [interval] [algorithm] [redisKey]");
			System.exit(0);
		}
		
		int runtime = Integer.parseInt(args[0]);
		int interval = Integer.parseInt(args[1]);
		String algorithm = args[2];
		String redisKey = args[3];
		
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
		builder.setSpout("JSON-spout", new JSONSpout(interval), 8)
			.setNumTasks(8);
		
		if(algorithm.equals("reservoir")){
			builder.setBolt("reservoir-bolt", new JSONAggregateBolt(), 8)
				.shuffleGrouping(algorithm+"-bolt")
				.setNumTasks(8);
		}else if(algorithm.equals("systematic")){
			builder.setBolt("systematic-bolt", new SysSampling(redisKey, jedisClusterConfig), 8)
				.shuffleGrouping(algorithm+"-bolt")
				.setNumTasks(1);
		}else if(algorithm.equals("hash")){
			builder.setBolt("hash-bolt", new JSONAggregateBolt(), 8)
				.shuffleGrouping(algorithm+"-bolt")
				.setNumTasks(8);
		}else if(algorithm.equals("bloom")){
			builder.setBolt("bloom-bolt", new JSONAggregateBolt(), 8)
				.shuffleGrouping(algorithm+"-bolt")
				.setNumTasks(1);
		}else{
			System.out.println("Usage: Enter the right algorithm name [reservoir, systematic, bloom, hash]");
			System.exit(0);
		}
		
		builder.setBolt("JSON-aggregate-bolt", new JSONAggregateBolt(), 1)
			.shuffleGrouping(algorithm+"-bolt")
			.setNumTasks(1);
		
	}
}
