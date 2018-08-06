package i2am.plan.manager.kafka.examples;

import java.io.IOException;

import i2am.plan.manager.kafka.I2AMProducer;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter { 
	private static final String consumerKey = "jIUbnuPiKl13bGsFRkgYjEE9R";
	private static final String consumerSecret = "SLg4MnVrgnPvUmpecjc5ACpZflko9500MfLdyqUSwtQlgcbS1h";
	private static final String accessToken = "732534959529857026-aKaxOeEc92ci97mFmHiKnI9EbNXtEgF";
	private static final String accessTokenSecret = "lsuDedlkhL7J9MUcMQVFvmR0wKhr9cvFmLglYlczvhZuk";
 
	public static void main(String[] args) throws TwitterException, IOException{
		StatusListener listener = new StatusListener(){
			I2AMProducer producer = new I2AMProducer("0KUK@naver.com", "TwitSRC");
			@Override
			public void onStatus(Status status) {
				producer.send(status.getUser().getName() + " : " + status.getText());
			}
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
			@Override
			public void onException(Exception ex) {
				ex.printStackTrace(); 
			}
			@Override
			public void onScrubGeo(long arg0, long arg1) {}
			@Override
			public void onStallWarning(StallWarning arg0) {}
		};
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret)
			.setJSONStoreEnabled(true);
		
		Configuration config = cb.build();
		TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
		twitterStream.addListener(listener);
		
		twitterStream.sample();
	}

}
