package com.terry.storm.twitbloomstorm;

import java.util.Arrays;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class TwitterHashtagBloomStorm {
   public static void main(String[] args) throws Exception{
      	   
	  // Launch Mode
	  String mode = args[0]; 
	  
	  // Ledis IP & Port
	  String db_ip = args[1];
	  int port = Integer.parseInt(args[2]);
	   
	  // Twitter Key
	  String consumerKey = args[3];
      String consumerSecret = args[4];      
      String accessToken = args[5];
      String accessTokenSecret = args[6];        
      
      // Number of Bucket
      int number_of_buckets = Integer.parseInt(args[7]);    // 버켓 사이즈
      
      // 필터링 키워드
      String[] arguments = args.clone();
      String[] keyWords = Arrays.copyOfRange(arguments, 8, arguments.length);      
      
      // Argument 확인
      System.out.println("Mode: " + mode);
      System.out.println("Ledis IP: " + db_ip + " : " + port);
      System.out.println(consumerKey);
      System.out.println(consumerSecret);
      System.out.println(accessToken);
      System.out.println(accessTokenSecret);
      System.out.println(number_of_buckets);      

      System.out.println("Filtered Keywords: ");
      for(int i=0; i<keyWords.length; i++) {
          System.out.println(keyWords[i]);
      }
      // Argument 확인
      
      Config config = new Config();
      config.setDebug(true);
		
      TopologyBuilder builder = new TopologyBuilder();
      builder.setSpout("twitter-spout", new TwitterSampleSpout(consumerKey,
         consumerSecret, accessToken, accessTokenSecret), 2);

      builder.setBolt("twitter-hashtag-reader-bolt", new HashtagReaderBolt(), 2)
         .shuffleGrouping("twitter-spout");

      builder.setBolt("bloom-filter-bolt", new BloomFilterBolt(number_of_buckets, keyWords, db_ip, port), 4)
         .fieldsGrouping("twitter-hashtag-reader-bolt", new Fields("hashtag"));      
			
      if ( mode.equals("0") ) {
    	  LocalCluster cluster = new LocalCluster();
          cluster.submitTopology("TwitterHashtagBloomStorm", config, builder.createTopology());
          Thread.sleep(10000);
          cluster.shutdown();    	  
      } else {
    	  StormSubmitter.submitTopology("BloomFilterTopology", config, builder.createTopology());
      }      
   }
}	