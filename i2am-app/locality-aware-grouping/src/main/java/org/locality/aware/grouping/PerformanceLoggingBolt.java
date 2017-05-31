package org.locality.aware.grouping;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceLoggingBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(PerformanceLoggingBolt.class);
	private OutputCollector outputCollector = null;
	private long rcv_number_of_tuples;
	private long lead_time;
//	private long start_time;
//	private long end_time;

	@Override
	public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
		logger.info("prepare");
		rcv_number_of_tuples = 0l;
		lead_time = 0l;
//		start_time = 0l;
//		end_time = 0;
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple)
	{
//		logger.info("execute");
		
//		String s = tuple.getStringByField("sentence");
//		Arrays.sort(s.toCharArray());
		
//		if (rcv_number_of_tuples % 1000 == 0) {
//			logger.info("\nGrouping Performance Test: received,"+Integer.toHexString(this.hashCode())+","+rcv_number_of_tuples+","+new Date().getTime());
//		}
		lead_time += System.currentTimeMillis() - tuple.getLongByField("start-time");
//		end_time = new Date().getTime();
//		if (start_time==0l)	start_time = end_time;
		rcv_number_of_tuples++;
//		outputCollector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}
	
	@Override
    public void cleanup() {
		logger.info("\nBolt cleanup,total_rcv_number_of_tuples," + rcv_number_of_tuples+",avg_time,"+((double)lead_time/(double)rcv_number_of_tuples));
//		logger.info("\nstart_time,"+start_time+",end_time,"+end_time);
//		long lead_time = end_time - start_time;
//		logger.info("\nBolt cleanup,total_rcv_number_of_tuples," + rcv_number_of_tuples+",avg_time,"+((double)lead_time/(double)(rcv_number_of_tuples-1)));
    }    
}