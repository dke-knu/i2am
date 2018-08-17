package i2am.tta.common;

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

import i2am.tta.bloom.BloomFilteringBolt;
import i2am.tta.ksample.KSampleBolt;
import i2am.tta.query.MultivariateQueryFilteringBolt;
import i2am.tta.simulation.TTASimulationTopology;
import i2am.tta.systematic.SystematicSamplingBolt;

public class TTATopology {
	private final static Logger logger = LoggerFactory.getLogger(TTASimulationTopology.class);
	
    public static void main(String[] args) throws Exception {
        /* Parameters */
    	String topologyName = args[0];
        String algorithmName = args[1];
		String groupingType = args[2];
		int interval = Integer.parseInt(args[3]);
		int experiments_minutes = Integer.parseInt(args[4]);

        /* Topology Configuration */
        TopologyBuilder builder = new TopologyBuilder();

        /* RandomTupleSpout */
        builder.setSpout("random-tuple-spout", new TupleGeneratorSpout(interval), 1)
			.setNumTasks(1);

        if (algorithmName.equals("k-sampling")){
        	/* SamplingBolt */
        	BoltDeclarer samplingBolt = builder.setBolt("sampling-or-filtering-bolt", new KSampleBolt(), 5)
        			.setNumTasks(5);
        	setGrouping(samplingBolt, groupingType, "random-tuple-spout");
        } else if (algorithmName.equals("systematic-sampling")){
        	/* SamplingBolt */
        	BoltDeclarer samplingBolt = builder.setBolt("sampling-or-filtering-bolt", new SystematicSamplingBolt(), 5)
        			.setNumTasks(5);
        	setGrouping(samplingBolt, groupingType, "random-tuple-spout");
        } else if(algorithmName.equals("query-filtering")){
        	/* FilteringBolt */
        	BoltDeclarer filteringBolt = builder.setBolt("sampling-or-filtering-bolt", new MultivariateQueryFilteringBolt(), 5)
                    .setNumTasks(5);
        	setGrouping(filteringBolt, groupingType, "random-tuple-spout");
        } else if(algorithmName.equals("bloom-filtering")){
            /* DeclaringBolt */
        	BoltDeclarer declaringBolt = builder.setBolt("declaring-bolt", new DeclaringBolt(2), 1)
                    .setNumTasks(1);
        	setGrouping(declaringBolt, groupingType, "random-tuple-spout");
        	/* FilteringBolt */
        	BoltDeclarer filteringBolt = builder.setBolt("sampling-or-filtering-bolt", new BloomFilteringBolt(), 4)
                    .setNumTasks(4);
        	setGrouping(filteringBolt, groupingType, "declaring-bolt");
        }

    	/* PassingBolt */
    	BoltDeclarer passingBolt = builder.setBolt("passing-bolt", new PassingBolt(), 1)
    			.setNumTasks(1);
    	setGrouping(passingBolt, groupingType, "sampling-or-filtering-bolt");

        /* LoggingBolt */
        BoltDeclarer loggingBolt =  builder.setBolt("logging-bolt", new LoggingBolt(), 1)
        		.setNumTasks(1);
    	setGrouping(loggingBolt, groupingType, "passing-bolt");

        Config config = new Config();
        config.setNumWorkers(8);

		/* Submit topology to cluster */
		try{
			StormSubmitter.submitTopology(topologyName + "-" + algorithmName + "-" + groupingType, config, builder.createTopology());
			Thread.sleep(experiments_minutes * 60 * 1000);
			
			Map<String, Object> conf = Utils.readStormConfig();
			Client client = NimbusClient.getConfiguredClient(conf).getClient();
			KillOptions killOpts = new KillOptions();
			client.killTopologyWithOpts(topologyName + "-" + algorithmName + "-" + groupingType, killOpts);
		}catch(AlreadyAliveException ae){
			logger.info(ae.get_msg());
		}catch(InvalidTopologyException ie){
			logger.info(ie.get_msg());
		}
    }
    
    private static void setGrouping(BoltDeclarer bolt, String groupingType, String senderName) {
		if ("locality-aware".equals(groupingType)) {
			String zkConnect = "MN:2181,SN01:2181,SN02:2181,SN03:2181,SN04:2181,SN05:2181,SN06:2181,SN07:2181,SN08:2181";
			bolt.customGrouping(senderName, new LocalityAwareGrouping(zkConnect));
		} else if ("shuffle".equals(groupingType)) {
			bolt.shuffleGrouping(senderName);
		} else if ("local-or-shuffle".equals(groupingType)) {
			bolt.localOrShuffleGrouping(senderName);
		} else {
			logger.error("GROUPING TYPE ERROR: " + groupingType);
		}
    }
}