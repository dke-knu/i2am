package org.i2am.algorithm.filtering;

import java.util.Map;
import java.util.Random;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import i2am.Filtering.Multivariate.MultivariateQueryFilteringBolt;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class QueryFilteringLocalTest 
extends TestCase
{
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public QueryFilteringLocalTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( QueryFilteringLocalTest.class );
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp()
	{
//		String query = "( ( ( ( C1 NLT -1 ) OR ( C1 NGT 1 ) ) AND ( C2 TIN hello ) ) AND ( C3 FROM 1521126000 ) )";
//    	Node node = Node.parse(query);
//    	DbAdapter.getInstance().addQuery(node, "test@a.b", "test01", "FilteringBoltTest");
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("Spout", new Spout(), 2);
		builder.setBolt("FilteringBolt", new MultivariateQueryFilteringBolt("FilteringBoltTest"), 4).shuffleGrouping("Spout");
		builder.setBolt("PrintBolt", new PrintBolt(), 1).globalGrouping("FilteringBolt");

		Config conf = new Config();
		LocalCluster cluster = new LocalCluster();

		cluster.submitTopology("QueryFilteringLocalTest", conf, builder.createTopology());
		Utils.sleep(5000);

		cluster.killTopology("QueryFilteringLocalTest");
		cluster.shutdown();  
	}
}

class Spout extends BaseRichSpout {
	private SpoutOutputCollector collector;
	String[] sentences = new String[]{ 
			"1,hello world,1521126000",
			"1,hello world,1521126001",
			"2,hello world,1521126002",
			"2,hello world,1521126000",
			"2,hello world,1521126001"
	};

	public void open(Map conf, TopologyContext context,SpoutOutputCollector collector) {
		this.collector = collector; 
	}

	public void nextTuple() {
		this.collector.emit(new Values(sentences[new Random().nextInt(5)]));
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("data"));
	}
}

class PrintBolt extends BaseBasicBolt{
		public void execute(Tuple tuple, BasicOutputCollector collector) {
               System.out.println(tuple.getStringByField("data"));
        }

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {}
}
