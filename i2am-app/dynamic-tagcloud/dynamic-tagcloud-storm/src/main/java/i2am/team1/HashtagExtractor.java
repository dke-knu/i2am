package i2am.team1;

import java.util.List;

import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;

import twitter4j.HashtagEntity;
import twitter4j.Status;

public class HashtagExtractor extends BaseFunction {

	public void execute(TridentTuple tuple, TridentCollector collector) {
		//Get the batch ID
		final long batchId = tuple.getLong(0);
		//Get the tweet
		final Status status = (Status) tuple.get(1);
		//Get the contained keywords.
		final List<String> containedKeyword = (List<String>) tuple.get(2);
		//Loop through the hashtags
		for (HashtagEntity hashtag : status.getHashtagEntities()) {
			for (String keyword : containedKeyword) {
				//Emit each hashtag
				collector.emit(new Values(batchId, hashtag.getText(), keyword));
			}
		}
	}
}
