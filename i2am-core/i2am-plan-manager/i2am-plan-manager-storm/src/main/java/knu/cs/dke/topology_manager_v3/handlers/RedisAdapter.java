package knu.cs.dke.topology_manager_v3.handlers;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import knu.cs.dke.topology_manager_v3.Plan;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.BinaryBernoulliSamplingTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.BloomFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.HashSamplingTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.KSamplingTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.PrioritySamplingTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.QueryFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.ReservoirSamplingTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.SystematicSamplingTopology;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisAdapter {

	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();

	// singleton
	private volatile static RedisAdapter instance;
	public static RedisAdapter getInstance() {
		if(instance == null) {
			synchronized(RedisAdapter.class) {
				if(instance == null) {
					instance = new RedisAdapter();
				}
			}
		}
		return instance;
	}

	protected RedisAdapter() {}


	private JedisCluster jedis;

	protected JedisCluster getConnection() {

		Set<HostAndPort> redisNodes = new HashSet<HostAndPort>();
		redisNodes.add(new HostAndPort("MN", 17000));
		redisNodes.add(new HostAndPort("SN01", 17001));
		redisNodes.add(new HostAndPort("SN02", 17002));
		redisNodes.add(new HostAndPort("SN03", 17003));
		redisNodes.add(new HostAndPort("SN04", 17004));
		redisNodes.add(new HostAndPort("SN05", 17005));
		redisNodes.add(new HostAndPort("SN06", 17006));
		redisNodes.add(new HostAndPort("SN07", 17007));
		redisNodes.add(new HostAndPort("SN08", 17008));		

		this.jedis = new JedisCluster(redisNodes);	

		return jedis;
	}

	public void set(String key, String value) {
		jedis.set(key, value);
	}

	public void addPlanParams(Plan plan) {

		JedisCluster jedisCn = this.getConnection();


		List<ASamplingFilteringTopology> topologies = plan.getTopologies();

		for( ASamplingFilteringTopology topology : topologies ) {

			String type = topology.getTopologyType();

			switch(type) {

			case "SYSTEMATIC_SAMPLING":

				SystematicSamplingTopology sys_topology = (SystematicSamplingTopology) topology;

				String sys_redisKey = sys_topology.getRedisKey();

				String sys_sampleKey = sys_topology.getTopologyName();
				String sys_interval = String.valueOf(sys_topology.getInterval());												
				String sys_inputTopic = sys_topology.getInputTopic();
				String sys_outputTopic = sys_topology.getOutputTopic();

				jedisCn.hset(sys_redisKey, "SampleKey", sys_sampleKey);
				jedisCn.hset(sys_redisKey, "Interval", sys_interval);				
				jedisCn.hset(sys_redisKey, "InputTopic", sys_inputTopic);
				jedisCn.hset(sys_redisKey, "OutputTopic", sys_outputTopic);				

				break;

			case "QUERY_FILTERING":

				QueryFilteringTopology query_topology = (QueryFilteringTopology) topology;

				String query_redisKey = query_topology.getRedisKey();

				String query_kewords = query_topology.getKeywords();
				String query_inputTopic = query_topology.getInputTopic();
				String query_outputTOpic = query_topology.getOutputTopic();

				jedisCn.hset(query_redisKey, "Keywords", query_kewords);
				jedisCn.hset(query_redisKey, "InputTopic", query_inputTopic);
				jedisCn.hset(query_redisKey, "OutputTopic", query_outputTOpic);

				break;

			case "BINARY_BERNOULLI_SAMPLING":

				BinaryBernoulliSamplingTopology bbs_topology = (BinaryBernoulliSamplingTopology) topology;

				String bbs_redisKey = bbs_topology.getRedisKey();

				String bbs_sampleKey = bbs_topology.getTopologyName();
				String bbs_preSampleKey = bbs_topology.getPreSampleKey();
				String bbs_sampleSize = String.valueOf(bbs_topology.getSampleSize());
				String bbs_windowSize = String.valueOf(bbs_topology.getWindowSize());
				String bbs_inputTopic = bbs_topology.getInputTopic();
				String bbs_outputTopic = bbs_topology.getOutputTopic();				

				jedisCn.hset(bbs_redisKey, "SampleKey", bbs_sampleKey);
				jedisCn.hset(bbs_redisKey, "PreSampleKey", bbs_preSampleKey);
				jedisCn.hset(bbs_redisKey, "SampleSize", bbs_sampleSize);
				jedisCn.hset(bbs_redisKey, "WindowSize", bbs_windowSize);
				jedisCn.hset(bbs_redisKey, "InputTopic", bbs_inputTopic);
				jedisCn.hset(bbs_redisKey, "OutputTopic", bbs_outputTopic);

				break;

			case "BLOOM_FILTERING":

				BloomFilteringTopology bloom_topology = (BloomFilteringTopology) topology;

				String bloom_redisKey = bloom_topology.getRedisKey();

				String bloom_kewords = bloom_topology.getKeywords();
				String bloom_bucketSize = String.valueOf(bloom_topology.getBucketSize());		
				String bloom_inputTopic = bloom_topology.getInputTopic();
				String bloom_outputTOpic = bloom_topology.getOutputTopic();

				jedisCn.hset(bloom_redisKey, "Keywords", bloom_kewords);
				jedisCn.hset(bloom_redisKey, "BucketSize", bloom_bucketSize);
				jedisCn.hset(bloom_redisKey, "InputTopic", bloom_inputTopic);
				jedisCn.hset(bloom_redisKey, "OutputTopic", bloom_outputTOpic);

				break;

			case "HASH_SAMPLING":

				HashSamplingTopology hash_topology = (HashSamplingTopology) topology;

				String hash_redisKey = hash_topology.getRedisKey();

				String hash_sampleKey = hash_topology.getTopologyName();				
				String hash_sampleSize = String.valueOf(hash_topology.getSampleSize());
				String hash_windowSize = String.valueOf(hash_topology.getWindowSize());
				String hash_function = hash_topology.getHashFunction();
				String hash_bucketSize = String.valueOf(hash_topology.getBucketSize());

				jedisCn.hset(hash_redisKey, "SampleKey", hash_sampleKey);
				jedisCn.hset(hash_redisKey, "SampleSize", hash_sampleSize);
				jedisCn.hset(hash_redisKey,  "WindowSize", hash_windowSize);
				jedisCn.hset(hash_redisKey,  "HashFunction", hash_function);
				jedisCn.hset(hash_redisKey, "BucketSize", hash_bucketSize);

				break;

			case "PRIORITY_SAMPLING":

				PrioritySamplingTopology priority_topology = (PrioritySamplingTopology) topology;

				String priority_redisKey = priority_topology.getRedisKey();

				String priority_sampleKey = priority_topology.getTopologyName();
				String priority_sampleSize = String.valueOf(priority_topology.getSampleSize());
				String priority_windowSize = String.valueOf(priority_topology.getWindowSize());								
				String priority_inputTopic = priority_topology.getInputTopic();
				String priority_outputTopic = priority_topology.getOutputTopic();

				jedisCn.hset(priority_redisKey, "SampleKey", priority_sampleKey);
				jedisCn.hset(priority_redisKey, "SampleSize", priority_sampleSize);
				jedisCn.hset(priority_redisKey, "WindowSize", priority_windowSize);
				jedisCn.hset(priority_redisKey, "InputTopic", priority_inputTopic);
				jedisCn.hset(priority_redisKey, "OutputTopic", priority_outputTopic);				

				break;

			case "RESERVOIR_SAMPLING":

				ReservoirSamplingTopology reservoir_topology = (ReservoirSamplingTopology) topology;

				String reservoir_redisKey = reservoir_topology.getRedisKey();

				String reservoir_sampleKey = reservoir_topology.getTopologyName();
				String reservoir_sampleSize = String.valueOf(reservoir_topology.getSampleSize());
				String reservoir_windowSize = String.valueOf(reservoir_topology.getWindowSize());								
				String reservoir_inputTopic = reservoir_topology.getInputTopic();
				String reservoir_outputTopic = reservoir_topology.getOutputTopic();

				jedisCn.hset(reservoir_redisKey, "SampleKey", reservoir_sampleKey);
				jedisCn.hset(reservoir_redisKey, "SampleSize", reservoir_sampleSize);
				jedisCn.hset(reservoir_redisKey, "WindowSize", reservoir_windowSize);
				jedisCn.hset(reservoir_redisKey, "InputTopic", reservoir_inputTopic);
				jedisCn.hset(reservoir_redisKey, "OutputTopic", reservoir_outputTopic);				

				break;

			case "K_SAMPLING":
				
				KSamplingTopology k_topology = (KSamplingTopology) topology;

				String k_redisKey = k_topology.getRedisKey();
				
				String k_sampleRate = String.valueOf(k_topology.getSamplingRate());												
				String k_inputTopic = k_topology.getInputTopic();
				String k_outputTopic = k_topology.getOutputTopic();
				
				jedisCn.hset(k_redisKey, "SamplingRateKey", k_sampleRate);				
				jedisCn.hset(k_redisKey, "InputTopic", k_inputTopic);
				jedisCn.hset(k_redisKey, "OutputTopic", k_outputTopic);				

				break;				
				
			case "KALMAN_FILTERING":
				break;

			default :

				System.out.println("[Redis Adapter] Algoritm Type Error.");

				break;


			}			
		}	
	}
}