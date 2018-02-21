package knu.cs.dke.prog;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;

import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.DateParser;
import knu.cs.dke.vo.TwitterEvent;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class StreamTwitterInput {
	public static TwitterStream _twitterStream;
	public static boolean finish_flag = false;

	public void start(EPRuntime runtime){
		Twitter twitter = new TwitterFactory().getInstance();
		LinkedBlockingQueue<Status> queue = null;

		try{
			//twitter 연동
			AccessToken token = null;
			Paging page = new Paging();
			page.count(20);
			page.setPage(1);


			twitter.setOAuthConsumer(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET);

			token = new AccessToken(Constant.ACCESS_TOKEN, Constant.ACCESS_SECRET);
			twitter.setOAuthAccessToken(token);
			System.out.println("here1...!!!!");
			StatusListener listener = new StatusListener(){
				public void onStatus(Status status){
					ArrayList<String> hashTag_list = null;
					if(status.getHashtagEntities().length>0){
						hashTag_list = new ArrayList<String>();
						for(HashtagEntity hashTag : status.getHashtagEntities()){
							hashTag_list.add(hashTag.getText());
						}
					}
					String userId = status.getUser().getScreenName(); //..? 시운오빠가 보내준거랑 좀 다름...;;
					String userName = status.getUser().getName();
					String createdAt = status.getCreatedAt().toString();
					String text = status.getText();
					String lang = status.getLang();
					DateParser dp = new DateParser();
					long newCreatedAt = dp.parse(createdAt);
					runtime.sendEvent(new TwitterEvent(userName,userId,newCreatedAt,lang,text,hashTag_list));
					queue.offer(status);
				}

				public void onDeletionNotice(StatusDeletionNotice sdn) {}

				public void onTrackLimitationNotice(int i) {}

				public void onScrubGeo(long l, long l1) {}

				public void onException(Exception ex) {}

				public void onStallWarning(StallWarning arg0) {}
			};

			ConfigurationBuilder cb =new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(Constant.CONSUMER_KEY).setOAuthConsumerSecret(Constant.CONSUMER_SECRET)
			.setOAuthAccessToken(Constant.ACCESS_TOKEN).setOAuthAccessTokenSecret(Constant.ACCESS_SECRET);
			System.out.println("here2...!!!!");
			_twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
			System.out.println("here3...!!!!");
			_twitterStream.addListener(listener);
			System.out.println("here4...!!!!");
			_twitterStream.sample();

			while(!finish_flag){

			}		
			System.out.println("finish flag!!!!!!!");
		}catch (Exception e){
			e.printStackTrace();
		}
	}



}
