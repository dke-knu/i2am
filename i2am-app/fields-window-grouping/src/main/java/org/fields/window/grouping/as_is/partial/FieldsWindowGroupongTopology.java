package org.fields.window.grouping.as_is.partial;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt.Count;
import org.apache.storm.tuple.Fields;
import org.fields.window.grouping.as_is.RandomSpout;
import org.fields.window.grouping.as_is.WindowedBolt;

public class FieldsWindowGroupongTopology {
	
	public static void main(String[] args) throws InterruptedException {
	    TopologyBuilder builder = new TopologyBuilder();
	     builder.setSpout("spout", new RandomSpout(), 1);
	     builder.setBolt("windowed-bolt", 
	                     new WindowedBolt().withWindow(new Count(5), new Count(1)), 5)
	     	.partialKeyGrouping("spout", new Fields("sender"));
	    Config conf = new Config();
	    conf.setDebug(true);

	    LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("fields-window-groupong-topology", conf, builder.createTopology());
		
		Thread.sleep(30 * 1000);
		cluster.shutdown();
	} 
	
}
