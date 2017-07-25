package com.terry.storm.twitstorm;

import java.util.HashMap;
import java.util.Map;

import twitter4j.*;
import twitter4j.conf.*;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class HashtagReaderBolt implements IRichBolt {
	
	private OutputCollector collector;

	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}	
	
	public void execute(Tuple tuple) {
		Status tweet = (Status) tuple.getValueByField("tweet");
		for(HashtagEntity hashtage : tweet.getHashtagEntities()) {
			System.out.println("Hashtag: " + hashtage.getText());
			this.collector.emit(new Values(hashtage.getText()));
		}
	}

	public void cleanup() {}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("hashtag"));
	}

	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}