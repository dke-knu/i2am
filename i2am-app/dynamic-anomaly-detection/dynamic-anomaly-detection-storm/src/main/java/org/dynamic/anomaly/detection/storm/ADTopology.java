package org.dynamic.anomaly.detection.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt.Count;

public class ADTopology {
	
	public static void main(String[] args) throws InterruptedException {
	    TopologyBuilder builder = new TopologyBuilder();
	     builder.setSpout("spout", new ADSpout(), 1);
	     builder.setBolt("ADWindowedBolt", 
	                     new ADWindowedBolt().withWindow(new Count(60), new Count(1)))
	     	.shuffleGrouping("spout");
	     builder.setBolt("AD3sigmaBolt", new AD3sigmaBolt())
	     	.shuffleGrouping("ADWindowedBolt");
	    Config conf = new Config();
	    conf.setDebug(true);
	   // conf.setNumWorkers(1);
	    //StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());

	    LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("ADTopology", conf, builder.createTopology());
		//Thread.sleep(10 * 1000);
		//cluster.shutdown();
	}
	
}
