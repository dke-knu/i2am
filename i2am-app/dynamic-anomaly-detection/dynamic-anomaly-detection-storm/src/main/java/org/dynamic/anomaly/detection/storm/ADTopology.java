package org.dynamic.anomaly.detection.storm;

import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADTopology {
	public static final Logger LOG = LoggerFactory.getLogger(ADTopology.class);

	public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		final String zkIP = "MN.eth";
		final short zkPort = 22181; 

		SpoutConfig spoutConfig = new SpoutConfig(new ZkHosts(zkIP + ":" + zkPort), 
				"topic-test", "/brokers/ids", "dynamic-anomaly-detection");
		spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		//	error: Got fetch request with offset out of range
		spoutConfig.useStartOffsetTimeIfOffsetOutOfRange = true;
		spoutConfig.ignoreZkOffsets = true;
		
		KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("ADSpout", kafkaSpout, 1);
		
		builder.setBolt("ADParserBolt", new ADParserBolt(), 1)
//		.setNumTasks(8)
		.shuffleGrouping("ADSpout");

		builder.setBolt("ADGroupingBolt", new ADGroupingBolt(), 8)
		.setNumTasks(16)
		.fieldsGrouping("ADParserBolt", new Fields("cluster", "host", "key"));

		builder.setBolt("ADWindowedBolt", new ADMovAvgStdBolt(), 8)
		.setNumTasks(8)
		.shuffleGrouping("ADGroupingBolt");

		builder.setBolt("AD3SigmaBolt", new AD3SigmaBolt(), 8)
		.setNumTasks(8)
		.shuffleGrouping("ADWindowedBolt");

		builder.setBolt("ADPerformanceBolt", new ADPerformanceBolt(), 8)
		.setNumTasks(8)
		.shuffleGrouping("AD3SigmaBolt");

		Config conf = new Config();
		conf.setDebug(true);

		if (args == null || args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("ADTopology", conf, builder.createTopology());

			Thread.sleep(30 * 1000);
			cluster.shutdown();
		}
		else {
			conf.setNumWorkers(8);
			StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
		}
	}

}
