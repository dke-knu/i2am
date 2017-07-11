package org.locality.aware.grouping;

import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.KillOptions;
import org.apache.storm.generated.Nimbus.Client;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalityGroupingTestTopology {
	private final static Logger logger = LoggerFactory.getLogger(LocalityGroupingTestTopology.class);
	static String topology_name = "wordcount-topology-for-locality";
	static final String RANDOM_SENTENCE_SPOUT = "random-sentence-spout";
	static final String PERFORMANCE_LOGGING_BOLT = "performance-logging-bolt";

	public static void main(String args[]) throws Exception {
		topology_name = args[0];
		final String GROUPING_TYPE = args[1];
		String zookeeper_connect_string = null;
		
		int tuple_size_kb = Integer.parseInt(args[2]);
		int spout_parallelism = Integer.parseInt(args[3]);
		int bolt_parallelism = Integer.parseInt(args[4]);
		int experiments_minutes = Integer.parseInt(args[5]);
		
		if ("locality-aware".equals(GROUPING_TYPE)) {
			zookeeper_connect_string = args[6];
		}
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(RANDOM_SENTENCE_SPOUT, new TupleGeneratorSpout(tuple_size_kb), spout_parallelism)
			.setNumTasks(spout_parallelism);
		BoltDeclarer bolt = builder.setBolt(PERFORMANCE_LOGGING_BOLT, new PerformanceLoggingBolt(), bolt_parallelism)
			.setNumTasks(bolt_parallelism);
		if ("locality-aware".equals(GROUPING_TYPE)) {
			bolt.customGrouping(RANDOM_SENTENCE_SPOUT, new LocalityAwareGrouping(zookeeper_connect_string));
		} else if ("shuffle".equals(GROUPING_TYPE)) {
			bolt.shuffleGrouping(RANDOM_SENTENCE_SPOUT);
		} else if ("my-shuffle".equals(GROUPING_TYPE)) {
			bolt.customGrouping(RANDOM_SENTENCE_SPOUT, new MyShuffleGrouping());
		} else if ("local-or-shuffle".equals(GROUPING_TYPE)) {
			bolt.localOrShuffleGrouping(RANDOM_SENTENCE_SPOUT);
		} else {
			logger.error("GROUPING TYPE ERROR: " + GROUPING_TYPE);
			return ;
		}

		Config config = new Config();
		config.setNumWorkers(8);
//		config.setDebug(true);

		// Submit topology to cluster
		logger.info("Submit Topology");
		try{
			StormSubmitter.submitTopology(topology_name, config, builder.createTopology());
			Thread.sleep(experiments_minutes * 60 * 1000);
			
			Map conf = Utils.readStormConfig();
			Client client = NimbusClient.getConfiguredClient(conf).getClient();
			KillOptions killOpts = new KillOptions();
			client.killTopologyWithOpts(topology_name, killOpts);
		}catch(AlreadyAliveException ae){
			logger.info(ae.get_msg());
		}catch(InvalidTopologyException ie){
			logger.info(ie.get_msg());
		}
	}
}