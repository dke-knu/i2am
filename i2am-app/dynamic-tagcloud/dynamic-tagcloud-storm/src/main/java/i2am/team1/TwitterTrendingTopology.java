package i2am.team1;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.testing.MemoryMapState;
import org.apache.storm.tuple.Fields;

public class TwitterTrendingTopology {
	public static void main(String[] args) throws Exception {
		final Config conf = new Config();
		
		final TridentTopology topology = new TridentTopology();
		//create TridentTopology
		topology.newStream("spout", new TwitterSpout())
			.name("TwitterSpout")
			.parallelismHint(8)
			.each(new Fields("batchId", "tweet", "containedKeyword"), new HashtagExtractor(), new Fields("batchId2", "hashtag", "keyword"))
			.name("HashtagExtractor")
			.parallelismHint(8)
			.groupBy(new Fields("batchId2", "hashtag", "keyword"))
			.name("Grouping")
			.persistentAggregate(new MemoryMapState.Factory(), new Count(), new Fields("count"))
			.parallelismHint(8)
			.newValuesStream()
			.name("newValuesStream")
			.aggregate(new Fields("hashtag", "count", "keyword"), new WordCountBinder(), new Fields("batchWordCount"))
			.name("WordCountBinder")
			.parallelismHint(8)
			.each(new Fields("batchWordCount"), new ResultTransport())
			.name("ResultTransport")
			.parallelismHint(8)
			;
		
		//If no args, assume we are testing locally
		if(args.length == 0) {
			//create LocalCluster and submit
			final LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("hashtag-count-topology", conf, topology.build());
		} else {
			//If on a real cluster, set the workers and submit
			conf.setNumWorkers(8);
			StormSubmitter.submitTopology(args[0], conf, topology.build());
		}
	}
}
