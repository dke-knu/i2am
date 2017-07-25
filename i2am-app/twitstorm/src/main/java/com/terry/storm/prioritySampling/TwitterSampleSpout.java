package com.terry.storm.prioritySampling;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import backtype.storm.utils.Utils;

public class TwitterSampleSpout extends BaseRichSpout {

	SpoutOutputCollector _collector;
	LinkedBlockingQueue<Status> queue = null;
	TwitterStream _twitterStream;

	String consumerKey;
	String consumerSecret;
	String accessToken;
	String accessTokenSecret;
	//String[] keyWords;
	
	/*
	public TwitterSampleSpout(String consumerKey, String consumerSecret,
			String accessToken, String accessTokenSecret, String[] keyWords) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
		this.keyWords = keyWords;
	}	
	*/
	
	public TwitterSampleSpout(String consumerKey, String consumerSecret,
			String accessToken, String accessTokenSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;		
	}	

	public TwitterSampleSpout() {
		// TODO Auto-generated constructor stub
	}
	
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

		queue = new LinkedBlockingQueue<Status>(1000);
		_collector = collector;

		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				queue.offer(status);
			}

			public void onDeletionNotice(StatusDeletionNotice sdn) {}


			public void onTrackLimitationNotice(int i) {}


			public void onScrubGeo(long l, long l1) {}


			public void onException(Exception ex) {}					

			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
			}
		};

		ConfigurationBuilder cb = new ConfigurationBuilder();

		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(consumerKey)
		.setOAuthConsumerSecret(consumerSecret)
		.setOAuthAccessToken(accessToken)
		.setOAuthAccessTokenSecret(accessTokenSecret);

		_twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		_twitterStream.addListener(listener);				
		
		//System.out.println(keyWords.length);
		_twitterStream.sample();
		
		/*
		if (keyWords.length == 0) {
			_twitterStream.sample();
		}else {
			FilterQuery query = new FilterQuery().track(keyWords);
			_twitterStream.filter(query);
		}				
		*/
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("tweet"));
	}

	public void nextTuple() {
		Status ret = queue.poll();

		if (ret == null) {
			Utils.sleep(50);
		} else {
			_collector.emit(new Values(ret));
		}
	}

	public void close() {
		_twitterStream.shutdown();
	}

	public Map<String, Object> getComponentConfiguration() {
		Config ret = new Config();
		ret.setMaxTaskParallelism(1);
		return ret;
	}

	@Override
	public void ack(Object id) {}

	@Override
	public void fail(Object id) {}
}
