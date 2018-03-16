package org.dynamic.anomaly.detection.storm;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.storm.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** deprecated. */
public class ADSpout extends BaseRichSpout {
	private SpoutOutputCollector collector;
	private Random rand;

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		this.rand = new Random();
	}

	public void nextTuple() {
		for (int i=0; i<2; i++) {
			for (int j=0; j<4; j++) {
				for (int k=0; k<4; k++) {
					String cluster = "cluster" + i;
					String host = "host" + j;
					String key = "key" + k;
					if (rand.nextInt(100)==0)
						collector.emit(new Values(cluster, host, key, rand.nextGaussian()*2+1000, (new Date()).getTime()/1000));
					else
						collector.emit(new Values(cluster, host, key, rand.nextGaussian()*2+10, (new Date()).getTime()/1000));
				}
			}
		}
		Utils.sleep(5000);
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("cluster", "host", "key", "value", "time"));
	}

}
