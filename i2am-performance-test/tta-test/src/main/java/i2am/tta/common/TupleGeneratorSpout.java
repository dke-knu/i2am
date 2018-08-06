package i2am.tta.common;

import java.util.Map;
import java.util.Random;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TupleGeneratorSpout extends BaseRichSpout {
	private final static Logger logger = LoggerFactory.getLogger(TupleGeneratorSpout.class);
	private SpoutOutputCollector _collector;
	private final String[] sentences = new String[]{ 
			"alice", "bob", "carol", "dave", "eve"
	};
	private char[] carray = new char[1000];
	private Random _rand;
	private long snd_number_of_tuples;
	
	private int interval;

	public TupleGeneratorSpout(int interval) {
		_rand = new Random();
		this.interval = interval;
	}
	
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		logger.info("open");
		snd_number_of_tuples = 0l;
		_collector = collector;
		_rand = new Random();
		for (int i=0; i<carray.length; i++)
			carray[i] = (char)(_rand.nextInt(26) + 'a');
	}

	@Override
	public void nextTuple() {
//		logger.info("nextTuple");
		String tuple = _rand.nextInt(100) + ","
				+ _rand.nextDouble() + ","
				+ sentences[_rand.nextInt(sentences.length)] + ","
				+ new String(carray);
				
		_collector.emit(new Values(tuple, System.currentTimeMillis()));
		snd_number_of_tuples++;
		try {Thread.sleep(interval);
		} catch (InterruptedException e) {}
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
		declarer.declare(new Fields("data", "start-time"));
	}
	
    @Override
    public void close() {
		logger.info("\nSpout close: total snd_number_of_tuples," + snd_number_of_tuples);
    }
}