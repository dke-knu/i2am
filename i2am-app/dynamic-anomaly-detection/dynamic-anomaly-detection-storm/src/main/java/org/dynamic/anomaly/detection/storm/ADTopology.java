package org.dynamic.anomaly.detection.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class ADTopology {

	public static void main(String[] args) throws InterruptedException {
		
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("ADSpout", new ADSpout(), 1);

		builder.setBolt("ADGroupingBolt", new ADGroupingBolt())
			.fieldsGrouping("ADSpout", new Fields("cluster", "host", "key"));

		builder.setBolt("ADWindowedBolt", new ADWindowedBolt())
			.shuffleGrouping("ADGroupingBolt");
		
		builder.setBolt("AD3SigmaBolt", new AD3SigmaBolt())
			.shuffleGrouping("ADWindowedBolt");
		
		Config conf = new Config();
		conf.setDebug(true);
		// conf.setNumWorkers(1);
		//StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("ADTopology", conf, builder.createTopology());
		
		Thread.sleep(30 * 1000);
		cluster.shutdown();
	}

}
