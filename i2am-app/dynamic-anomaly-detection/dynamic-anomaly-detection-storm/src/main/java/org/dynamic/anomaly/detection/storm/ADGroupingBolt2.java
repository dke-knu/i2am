package org.dynamic.anomaly.detection.storm;

import org.apache.storm.kafka.KafkaSpout2;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADGroupingBolt2 extends BaseBasicBolt{
    public static final Logger LOG = LoggerFactory.getLogger(KafkaSpout2.class);
    
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		LOG.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		String splitArray = input.getString(0);
		
		System.out.println(splitArray);

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("key","doctype"));
	}

}
