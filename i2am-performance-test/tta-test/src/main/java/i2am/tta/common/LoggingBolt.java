package i2am.tta.common;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(LoggingBolt.class);
	private OutputCollector outputCollector = null;
	private long rcv_number_of_tuples;
	private long lead_time;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		logger.info("prepare");
		rcv_number_of_tuples = 0l;
		lead_time = 0l;
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
//		logger.info("execute");
		lead_time += System.currentTimeMillis() - tuple.getLongByField("start-time");
//		logger.info("##" + tuple.getIntegerByField("sampleFlag") + "," + tuple.getStringByField("data"));
		rcv_number_of_tuples++;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}
	
	@Override
    public void cleanup() {
		logger.info("\nBolt cleanup,total_rcv_number_of_tuples," + rcv_number_of_tuples+",avg_time,"+((double)lead_time/(double)rcv_number_of_tuples));
    }    
}