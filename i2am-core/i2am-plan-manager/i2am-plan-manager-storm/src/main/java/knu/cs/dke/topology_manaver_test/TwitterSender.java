package knu.cs.dke.topology_manaver_test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TwitterSender {

	public static void main(String[] args) throws TwitterException {
		
		AccessToken accesstoken = new AccessToken("725180753106063360-aCKLTqLFwyp7S1T0bzj8No7weYb1DiR", "iMLeyvhJ2KyxErLJJm6a1uzAPp9JGwbgJeM6e1sXRKh5J");
		Twitter twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer("2aRE4DIrbYk8uyxxh0HIxz9e3", "DyMZPokVZCG5MpQjHZC7ZjBYamNzVBFfBL2Ig3Nui3LPkwyS02");
		twitter.setOAuthAccessToken(accesstoken);
		//User user = twitter.verifyCredentials();

		TwitterStream twitterStream = new TwitterStreamFactory().getInstance().addListener(new StatusListener() {
						
			@Override
			public void onStatus(Status status) {
								
				String message = status.getUser().getScreenName().replace(",", "") + ","
									+ status.getUser().getLang().replace(",", "") + ","
									+ status.getUser().getLocation() + ","									
									+ status.getUser().getCreatedAt() + ","
									+ status.getRetweetCount() + ","
									+ status.getText().replace(",", "");
								
				System.out.println(message);
				
				// Send to Kafka...!
				String output_topic = "835eaa3b-2dc4-44ee-b39c-12d37d112ca0";		
				String groupId = UUID.randomUUID().toString(); 

				String server = "114.70.235.43:19092";

				String servers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
						+ "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";	

				// Producer Props
				Properties produce_props = new Properties();
				produce_props.put("bootstrap.servers", servers);
				produce_props.put("acks", "all");
				produce_props.put("retries", 0);
				produce_props.put("batch.size", 16384);
				produce_props.put("linger.ms", 1);
				produce_props.put("buffer.memory", 33554432);
				produce_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
				produce_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

				Producer<String, String> producer = new KafkaProducer<>(produce_props);			
						
				producer.send(new ProducerRecord<String, String>(output_topic, message));	
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				//System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				//System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				//System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				//System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		});		
		
		twitterStream.setOAuthConsumer("2aRE4DIrbYk8uyxxh0HIxz9e3", "DyMZPokVZCG5MpQjHZC7ZjBYamNzVBFfBL2Ig3Nui3LPkwyS02");
		twitterStream.setOAuthAccessToken(accesstoken);
		twitterStream.sample();
	}

}

