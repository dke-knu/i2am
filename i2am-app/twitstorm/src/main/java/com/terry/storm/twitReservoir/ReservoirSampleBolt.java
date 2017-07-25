package com.terry.storm.twitReservoir;

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

public class ReservoirSampleBolt implements IRichBolt {
	
	Map<String, Integer> counterMap; // 해쉬코드 카운터	
	private OutputCollector collector;
	
	int size_of_sample;
	int window_size;
	
	// Ledis: DataBase
	Jedis jedis;	
	String ip = "127.0.0.1";
	int port = 6379;
		
	public ReservoirSampleBolt(int size_of_sample, int window_size, String ip, int port) {		
		this.size_of_sample = size_of_sample;
		this.window_size = window_size;
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
				
		//Long sample_length = jedis.llen("reservoir:sample");				
		int count = Integer.parseInt(jedis.get("count"));		
		
		// 들어온 데이터 수가 윈도우 사이즈를 넘어가면 샘플 초기화 or 새로운 샘플 수집
		// 기존의 샘플링 결과를 저장할 방법이 필요하다.
		if ( count%window_size == 0 ) {
			jedis.ltrim("reservoir:sample", -999999, -999999);
			jedis.set("count", "0");
		}
						
		if( count < size_of_sample ) {			
			jedis.rpush("reservoir:sample", key);			
		}
		else {			
			int prob = (int) (Math.random() * count);
			
			if ( prob < size_of_sample ) {				
				jedis.lset("reservoir:sample", prob, key);				
			}							
		}
		
		//count += 1;
		jedis.incr("count");
		
		collector.ack(tuple);
	}

	public void cleanup() {		
		jedis.close();
	}
	
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("hashtag"));
	}
	
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}