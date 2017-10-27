package i2am.benchmark.storm.reservoir;

import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassBolt extends BaseRichBolt{
	private final static Logger logger = LoggerFactory.getLogger(PassBolt.class);
	private OutputCollector outputCollector = null;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
		// TODO Auto-generated method stub
		this.outputCollector = outputCollector;
	}
	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		
		List<String> sampleList = (List<String>) tuple.getValue(0);
		long outputTime = System.currentTimeMillis();
		
		for(String data : sampleList){
			outputCollector.emit(new Values(new String(data + "," + outputTime)));
		}
	}
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("message"));
	}
}
