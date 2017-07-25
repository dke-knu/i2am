package com.terry.storm.prioritySampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;

public class PrioritySampleBolt implements IRichBolt {
	
	Map<String, Integer> counterMap; // 해쉬코드 카운터	
	private OutputCollector collector;
	
	int size_of_sample;	
	int window_size;
	
	ArrayList<String> sample;	
	ArrayList<String> priority;
	
	// Ledis: DataBase
	Jedis jedis;	
	String ip = "127.0.0.1";
	int port = 6379;
		
	public PrioritySampleBolt(int size_of_sample, int window_size, String ip, int port) {		
		this.size_of_sample = size_of_sample;
		this.window_size = window_size;
		this.ip = ip;
		this.port = port;
	}	
		
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.counterMap = new HashMap<String, Integer>();
		this.collector = collector;	
		jedis = new Jedis(ip, port);
		sample = new ArrayList<String>();
		priority = new ArrayList<String>();		
	}

	public void execute(Tuple tuple) {
		
		String key = tuple.getString(0);
				
		//Long sample_length = jedis.llen("reservoir:sample");
		// count를 초기화 해주어야 한당.
		int count = Integer.parseInt(jedis.get("count"));		
		
		// 들어온 데이터 수가 윈도우 사이즈를 넘어가면 샘플 초기화 or 새로운 샘플 수집
		// 기존의 샘플링 결과를 저장할 방법이 필요하다.
		if ( count%window_size == 0 ) {
			jedis.ltrim("priority:sample", -999999, -999999);
			jedis.ltrim("priority:priority", -999999, -999999);			
			jedis.set("count", "0");
		}
						
		// 가중치를 계산하기 위해 리스트를 읽어서 개수를 세야함니당
		sample = (ArrayList<String>) jedis.lrange("priority:sample", 0, -1);
		priority = (ArrayList<String>) jedis.lrange("priority:priority", 0, -1);		

		// Convert ArrayList<String> to ArrayList<Double>		
		ArrayList<Double> temp = new ArrayList<Double>();
				
		for( int i=0; i<priority.size(); i++ ) {
			temp.add( Double.parseDouble(priority.get(i)) );			
		}		
		//////////////////////////////////////////////////
		
		int w = Collections.frequency(sample, key) + 1; // sample에 들어있는 같은 key갯수 + 자기자신1 
		double random = Math.random(); // 랜덤숫자
		double p = Math.pow(random, 1.0/w); // 랜덤숫자의 가중치의역 제곱근
		
		// 샘플이 가득차지 않았다면 값을 넣는다.
		if( count < size_of_sample ) {			
			jedis.rpush("priority:sample", key);	
			jedis.rpush("priority:priority", Double.toString(p));
		}
		
		// 가득차면 확률을 계산한다. 
		else {
			// 계산된 확률이 최소값보다 크면 대체한다.
			double minP = 0;
			int minIndex = 0;			
			
			if( temp.size() != 0 ) {
				minP = Collections.min(temp);
				minIndex = temp.indexOf(minP);
			}
			
			if ( minP < p ) { 				
				jedis.lset("priority:sample", minIndex, key);
				jedis.lset("priority:priority", minIndex, Double.toString(p));				
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