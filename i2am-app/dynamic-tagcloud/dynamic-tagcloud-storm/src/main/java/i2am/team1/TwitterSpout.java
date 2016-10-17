package i2am.team1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.storm.Config;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterSpout implements IBatchSpout {
	private LinkedBlockingQueue<Status> queue;
	
	private final String consumerKey = "jIUbnuPiKl13bGsFRkgYjEE9R";
	private final String consumerSecret = "SLg4MnVrgnPvUmpecjc5ACpZflko9500MfLdyqUSwtQlgcbS1h";
	private final String accessToken = "732534959529857026-aKaxOeEc92ci97mFmHiKnI9EbNXtEgF";
	private final String accessTokenSecret = "lsuDedlkhL7J9MUcMQVFvmR0wKhr9cvFmLglYlczvhZuk";
	
	private String[] keywords = new String[]{};
	private TwitterStream twitterStream;
	private StatusListener listener;
	private Configuration config;
	private Connection conn;

	//open is ran when a spout instance is created
	public void open(Map conf, TopologyContext context) {
		//Create the queue
		this.queue = new LinkedBlockingQueue<Status>();

		//Create a listener for tweets (Status)
		this.listener = new StatusListener() {
			//If there's a tweet, add to the queue
			public void onStatus(Status status) {
				queue.offer(status);  
			}
			public void onDeletionNotice(StatusDeletionNotice sdn) {}
			public void onTrackLimitationNotice(int i) {}
			public void onScrubGeo(long l, long l1) {}
			public void onException(Exception e) {}
			public void onStallWarning(StallWarning warning) {}
		};

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret);
		config = cb.build();
	}
	public void filterTwitterStream(){
		if(twitterStream == null){
			twitterStream = new TwitterStreamFactory(config).getInstance();
			twitterStream.addListener(listener);
		}
		FilterQuery query = new FilterQuery().track(keywords);
		twitterStream.filter(query);
	}
	//Emit tweets from the queue
	public void emitBatch(long batchId, TridentCollector collector) {
		if (conn == null)
			conn = getConnection();

		String[] newKeywords = getKeywords(conn);
		if (newKeywords == null || newKeywords.length <= 0) {
			System.err.println("newKeywords is empty.");
			Utils.sleep(500);
			return;
		}

		if (!Arrays.equals(keywords, newKeywords)) {
			keywords = newKeywords;
			filterTwitterStream();
		}
		
		for (long start = System.currentTimeMillis(); System.currentTimeMillis()-start<5*1000;) {
			final Status status = queue.poll();
			if (status == null) {
				Utils.sleep(50);
			} else {
				List<String> containedKeyword = new ArrayList<String>();
				for (String kw : keywords) {
					if (status.getText().contains(kw))
						containedKeyword.add(kw);
				}
				collector.emit(new Values(batchId, status, containedKeyword));
			}
		}
	}
	public Connection getConnection() {
		final String driver = "com.mysql.jdbc.Driver";
		final String url = "jdbc:mysql://" + ":3306/tagcloud"; // db ip
		final String uId = ""; // db id
		final String uPwd = ""; // db passwd

		Connection conn = null;
		try{
			Class.forName(driver);
			conn = DriverManager.getConnection(url,uId,uPwd);
			
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return conn;
	}
	public String[] getKeywords(Connection conn) {
		Statement stmt = null;
		String[] newKeywords = null;;

		try{
			stmt = conn.createStatement();

			String sql = "SELECT keyword FROM keyword_tb WHERE `usage` > 0 ORDER BY keyword";
			ResultSet rs = stmt.executeQuery(sql);

			ArrayList<String> list = new ArrayList<String>();
			while(rs.next()){
				list.add(rs.getString("keyword"));
			}
			if (!list.isEmpty())
				newKeywords = list.toArray(new String[list.size()]);
			rs.close();
			stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return newKeywords;
	}

	//No handling of acks
	public void ack(long batchId) {
	}
	//Clean up the things opened in open()
	public void close() {
		twitterStream.shutdown();
	}
	//Get configuration
	public Map getComponentConfiguration() {
		Config conf = new Config();
		conf.setMaxTaskParallelism(1);
		return conf;
	}
	//Get the fields to be emitted
	public Fields getOutputFields() {
		return new Fields("batchId", "tweet", "containedKeyword");
	}
}