package org.locality.aware.grouping;

import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TupleGeneratorSpout extends BaseRichSpout {
	private final static Logger logger = LoggerFactory.getLogger(TupleGeneratorSpout.class);
	SpoutOutputCollector _collector;
//	String[] sentences = new String[]{ 
//			"the cow jumped over the moon", 
//			"an apple a day keeps the doctor away",
//			"four score and seven years ago", 
//			"snow white and the seven dwarfs", 
//			"i am at two with nature" 
//	};
//	Random _rand;
	private long snd_number_of_tuples;
	byte[] tuple;

	public TupleGeneratorSpout(int tuple_size_kb) {
		tuple = new byte[tuple_size_kb*1024];
	}
	
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		logger.info("open");
		snd_number_of_tuples = 0l;
		_collector = collector;
//		_rand = new Random();
	}

	@Override
	public void nextTuple() {
//		logger.info("nextTuple");
		Utils.sleep(100);
		
//		String sentence = sentences[_rand.nextInt(sentences.length)];
		_collector.emit(new Values(tuple, System.currentTimeMillis()));
		snd_number_of_tuples++;
		
//		if (snd_number_of_tuples % 1000 == 0) {
//			logger.info("\nGrouping Performance Test: sended,"+Integer.toHexString(this.hashCode())+","+snd_number_of_tuples+","+new Date().getTime());
//		}
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		logger.info("declare");
		declarer.declare(new Fields("sentence", "start-time"));
	}
	
    @Override
    public void close() {
		logger.info("\nSpout close: total snd_number_of_tuples," + snd_number_of_tuples);
    }
}