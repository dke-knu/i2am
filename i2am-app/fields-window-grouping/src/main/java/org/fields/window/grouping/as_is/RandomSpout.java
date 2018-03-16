package org.fields.window.grouping.as_is;

import java.util.Map;
import java.util.Random;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

public class RandomSpout extends BaseRichSpout {
	private SpoutOutputCollector collector;
	private Random rand;
	
	private final String[] SENDER = {"Alice", "Bob", "Clark"};

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		System.out.println("spout open");
		this.collector = collector;
		this.rand = new Random();
	}
	
	public void nextTuple() {
		Utils.sleep(50);
		collector.emit(new Values(SENDER[rand.nextInt(SENDER.length)], rand.nextDouble()));
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("sender", "value"));
	}

}
