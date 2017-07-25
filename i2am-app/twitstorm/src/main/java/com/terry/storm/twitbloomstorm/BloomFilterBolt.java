package com.terry.storm.twitbloomstorm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import filter.BloomFilter;
import redis.clients.jedis.Jedis;

public class BloomFilterBolt implements IRichBolt {
		
	Map<String, Integer> counterMap; // 해쉬코드 카운터	
	private OutputCollector collector;	
	
	String[] filter_keywords; // 필터링 '할' 단어	
	ArrayList<String> filtered_tags; // 필터링 '된' 단어, 확인용	
			
	int number_of_buckets; // 블룸 필터 크기
	BloomFilter bloom; // 블룸 필터	
	
	// Ledis: DataBase
	Jedis jedis;
	String ip = "127.0.0.1";
	int port = 6379;
		
	public BloomFilterBolt(int number_of_buckets, String[] keywords, String ip, int port) {		
		this.number_of_buckets = number_of_buckets; // 블룸 필터 크기
		this.filter_keywords = keywords.clone(); // 리스트: 필터링 할 단어들
		this.filtered_tags = new ArrayList<String>(); // 결과 저장용(레디스 사용 시 필요없음)
		this.ip = ip;
		this.port = port;
	}	
		
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.counterMap = new HashMap<String, Integer>(); 
		this.collector = collector;		
		jedis = new Jedis(ip, port);
		
		// 필터 생성
		this.bloom = new BloomFilter(number_of_buckets);			
		// 필터링 단어 등록 ... 
		for(int i=0; i<filter_keywords.length; i++) {			
			bloom.add(filter_keywords[i]);
		}
	}

	public void execute(Tuple tuple) {		
		
		String key = tuple.getString(0); // 태그 추출		
		
		if( bloom.isExist(key) ) { // 단어가 필터에 걸리면 태그 저장 (리스트&레디스)
			filtered_tags.add(key);
			jedis.rpush("bloomFilter:list", key);			
		}		
		
		// 해쉬맵에 저장하는 부분. 필요없음.
		if(!counterMap.containsKey(key)){ 
			counterMap.put(key, 1);
		}else{
			Integer c = counterMap.get(key) + 1;
			counterMap.put(key, c);
		}

		collector.ack(tuple);
	}

	public void cleanup() {	
				
		for(Map.Entry<String, Integer> entry:counterMap.entrySet()){
			System.out.println("Result: " + entry.getKey()+" : " + entry.getValue());
		}		
		
		System.out.println("Filtered tags : ");
		for( int i=0; i<filtered_tags.size(); i++ ) {
			System.out.print(filtered_tags.get(i)+" ");
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