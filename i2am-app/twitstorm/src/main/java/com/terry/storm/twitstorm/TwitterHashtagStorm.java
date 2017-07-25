package com.terry.storm.twitstorm;

import java.util.Arrays;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class TwitterHashtagStorm {
   public static void main(String[] args) throws Exception{
      	   
	  // 실행환경 - 로컬모드:0, 클러스터:1 	   
	  String mode = args[0];	  
	  
	  // Ledis IP
	  String db_ip = args[1];
	  int port = Integer.parseInt(args[2]);	  
	  
	  // Twitter Keys
	  String consumerKey = args[3]; 
      String consumerSecret = args[4];      
      String accessToken = args[5];
      String accessTokenSecret = args[6];
            
      // Buckets
      int number_of_buckets = Integer.parseInt(args[7]);  // 버켓의 수           
      int picked_bucket = Integer.parseInt(args[8]);      // 선택된 버켓 
      
      // Keywords: Don't need.
      String[] arguments = args.clone();
      String[] keyWords = Arrays.copyOfRange(arguments, 9, arguments.length);
      
      
      // Argument Check
      System.out.println("Mode: " + mode);
      System.out.println("Ledis IP: " + db_ip + " : " + port);
      System.out.println(consumerKey);
      System.out.println(consumerSecret);
      System.out.println(accessToken);
      System.out.println(accessTokenSecret);
      System.out.println(number_of_buckets);
      System.out.println(picked_bucket);
            
      for(int i=0; i<keyWords.length; i++) {
          System.out.println(keyWords[i]);
      }      
            
      Config config = new Config();
      config.setDebug(true);
      config.setNumWorkers(2);
		
      TopologyBuilder builder = new TopologyBuilder();
      builder.setSpout("twitter-spout", new TwitterSampleSpout(consumerKey,
         consumerSecret, accessToken, accessTokenSecret, keyWords), 2);

      builder.setBolt("twitter-hashtag-reader-bolt", new HashtagReaderBolt(), 2)
         .shuffleGrouping("twitter-spout");

      builder.setBolt("hash-sample-bolt", new HashSampleBolt(number_of_buckets, picked_bucket, db_ip, port), 4)
         .fieldsGrouping("twitter-hashtag-reader-bolt", new Fields("hashtag"));
			      
      
      if( mode.equals("0") ) {
    	  LocalCluster cluster = new LocalCluster();
          cluster.submitTopology("TwitterHashtagStorm", config, builder.createTopology());
          Thread.sleep(10000);
          cluster.shutdown();
      } else {
    	  StormSubmitter.submitTopology("HashSampleTopology", config, builder.createTopology());
      }
      
   }
}	