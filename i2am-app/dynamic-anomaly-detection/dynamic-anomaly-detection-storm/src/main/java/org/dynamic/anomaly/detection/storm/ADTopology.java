package org.dynamic.anomaly.detection.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class ADTopology {

	public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("ADSpout", new ADSpout(), 1);

		builder.setBolt("ADGroupingBolt", new ADGroupingBolt(), 8)
		.setNumTasks(16)
		.fieldsGrouping("ADSpout", new Fields("cluster", "host", "key"));

		builder.setBolt("ADWindowedBolt", new ADMovAvgStdBolt(), 8)
		.setNumTasks(8)
		.shuffleGrouping("ADGroupingBolt");

		builder.setBolt("AD3SigmaBolt", new AD3SigmaBolt(), 8)
		.setNumTasks(8)
		.shuffleGrouping("ADWindowedBolt");

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
