package com.terry.storm.twitReservoir;

import java.util.Arrays;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class TwitterReservoirStorm {
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
            
      // Number of Sample : K
      // Number of Window
      int size_of_sample = Integer.parseInt(args[7]);
      int window_size = Integer.parseInt(args[8]);
      
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
      System.out.println(size_of_sample);      
            
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

      builder.setBolt("reservoir-sample-bolt", new ReservoirSampleBolt(size_of_sample, window_size, db_ip, port), 4)
         .fieldsGrouping("twitter-hashtag-reader-bolt", new Fields("hashtag"));
			      
      
      if( mode.equals("0") ) {
    	  LocalCluster cluster = new LocalCluster();
          cluster.submitTopology("TwitterHashtagStorm", config, builder.createTopology());
          Thread.sleep(10000);
          cluster.shutdown();
      } else {
    	  StormSubmitter.submitTopology("ReservoirSampleTopology", config, builder.createTopology());
      }
      
   }
}	