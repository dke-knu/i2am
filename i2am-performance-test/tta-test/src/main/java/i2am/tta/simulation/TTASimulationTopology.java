package i2am.tta.simulation;

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

import i2am.tta.common.LocalityAwareGrouping;
import i2am.tta.common.LoggingBolt;
import i2am.tta.common.TupleGeneratorSpout;

public class TTASimulationTopology {
	private final static Logger logger = LoggerFactory.getLogger(TTASimulationTopology.class);
	private static final String RANDOM_TUPLE_SPOUT = "random-tuple-spout";
	private static final String PASSING_BOLT = "passing-bolt";
	private static final String PARALLEL_BOLT = "parallel-bolt";
	private static final String LOGGING_BOLT = "logging-bolt";

	public static void main(String args[]) throws Exception {
		final String TOPOLOGY_NAME = args[0];
		final String GROUPING_TYPE = args[1];
		
		int interval = Integer.parseInt(args[2]);
		int sleep_time = Integer.parseInt(args[3]);
		int bolt_parallelism = Integer.parseInt(args[4]);
		int experiments_minutes = Integer.parseInt(args[5]);
		
		TopologyBuilder builder = new TopologyBuilder();
		// spout
		builder.setSpout(RANDOM_TUPLE_SPOUT, new TupleGeneratorSpout(interval), 1)
			.setNumTasks(1);
		// bolt
		BoltDeclarer passingBolt1 = builder.setBolt(PASSING_BOLT+1, new PassingBolt(), 1)
			.setNumTasks(1);
		BoltDeclarer parallelBolt = builder.setBolt(PARALLEL_BOLT, new ParallelBolt(sleep_time), bolt_parallelism)
				.setNumTasks(bolt_parallelism);
		BoltDeclarer passingBolt2 = builder.setBolt(PASSING_BOLT+2, new PassingBolt(), 1)
				.setNumTasks(1);
		BoltDeclarer loggingBolt = builder.setBolt(LOGGING_BOLT, new LoggingBolt(), 1)
				.setNumTasks(1);

		if ("locality-aware".equals(GROUPING_TYPE)) {
			String zookeeper_connect_string = args[6];
			passingBolt1.customGrouping(RANDOM_TUPLE_SPOUT, new LocalityAwareGrouping(zookeeper_connect_string));
			parallelBolt.customGrouping(PASSING_BOLT+1, new LocalityAwareGrouping(zookeeper_connect_string));
			passingBolt2.customGrouping(PARALLEL_BOLT, new LocalityAwareGrouping(zookeeper_connect_string));
			loggingBolt.customGrouping(PASSING_BOLT+2, new LocalityAwareGrouping(zookeeper_connect_string));
		} else if ("shuffle".equals(GROUPING_TYPE)) {
			passingBolt1.shuffleGrouping(RANDOM_TUPLE_SPOUT);
			parallelBolt.shuffleGrouping(PASSING_BOLT+1);
			passingBolt2.shuffleGrouping(PARALLEL_BOLT);
			loggingBolt.shuffleGrouping(PASSING_BOLT+2);
		} else if ("local-or-shuffle".equals(GROUPING_TYPE)) {
			passingBolt1.localOrShuffleGrouping(RANDOM_TUPLE_SPOUT);
			parallelBolt.localOrShuffleGrouping(PASSING_BOLT+1);
			passingBolt2.localOrShuffleGrouping(PARALLEL_BOLT);
			loggingBolt.localOrShuffleGrouping(PASSING_BOLT+2);
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
			StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
			Thread.sleep(experiments_minutes * 60 * 1000);
			
			Map conf = Utils.readStormConfig();
			Client client = NimbusClient.getConfiguredClient(conf).getClient();
			KillOptions killOpts = new KillOptions();
			client.killTopologyWithOpts(TOPOLOGY_NAME, killOpts);
		}catch(AlreadyAliveException ae){
			logger.info(ae.get_msg());
		}catch(InvalidTopologyException ie){
			logger.info(ie.get_msg());
		}
	}
}