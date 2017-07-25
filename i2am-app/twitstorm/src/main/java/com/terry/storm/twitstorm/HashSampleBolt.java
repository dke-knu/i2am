package com.terry.storm.twitstorm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;

public class HashSampleBolt implements IRichBolt {
	
	Map<String, Integer> counterMap; // 해쉬코드 카운터	
	private OutputCollector collector;
	
	ArrayList<String> bucket = new ArrayList<String>(); // 버켓 수
	int number_of_buckets;
	int picked_bucket;
	
	// Ledis: DataBase
	Jedis jedis;	
	String ip = "127.0.0.1";
	int port = 6379;
	
	
	public HashSampleBolt(int number_of_buckets, int picked_bucket, String ip, int port) {
		this.number_of_buckets = number_of_buckets;
		this.picked_bucket = picked_bucket;		
		this.ip = ip;
		this.port = port;
	}	
		
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.counterMap = new HashMap<String, Integer>();
		this.collector = collector;		
		
		jedis = new Jedis(ip, port);		
	}

	public void execute(Tuple tuple) {
		
		String key = tuple.getString(0);		
		int hash_key_number = Math.abs(key.hashCode()%number_of_buckets);
		String hash_key = Integer.toString(hash_key_number);
			
		if( Integer.toString(picked_bucket).equals(hash_key) ) {
			bucket.add(key);
			jedis.rpush("hashsample:list", key);
		}
				
		if(!counterMap.containsKey(hash_key)){
			counterMap.put(hash_key, 1);
		}else{
			Integer c = counterMap.get(hash_key) + 1;
			counterMap.put(hash_key, c);
		}	

		collector.ack(tuple);
	}

	public void cleanup() {		
				
		for(Map.Entry<String, Integer> entry:counterMap.entrySet()){
			System.out.println("Result: " + entry.getKey()+" : " + entry.getValue());
		}		
		
		System.out.println("Tags in Bucket : ");
		
		for( int i=0; i<bucket.size(); i++ ) {
			System.out.print(bucket.get(i)+" ");
			
		}		
		System.out.println("\n");
		
		jedis.close();
	}
	
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("hashtag"));
	}
	
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}