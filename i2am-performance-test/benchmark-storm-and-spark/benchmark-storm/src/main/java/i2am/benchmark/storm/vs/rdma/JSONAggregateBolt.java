package i2am.benchmark.storm.vs.rdma;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONAggregateBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(JSONAggregateBolt.class);
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector = null;
	
	private long nOfMessages = 0l;
	private long turnAroundTime = 0l;
	
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.outputCollector = outputCollector;
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub

		// Get JSON From Tuple
		JSONParser parser = new JSONParser();
		JSONObject message = new JSONObject();
		
		try {
			message = (JSONObject) parser.parse(new String(tuple.getString(0)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long startTime = ((Number) message.get("startTime")).longValue();
		
		nOfMessages ++;
		turnAroundTime += (System.currentTimeMillis() - startTime);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public void cleanup() {
    	logger.error("### Performance Result ###");
    	logger.error("Number of messages = " + nOfMessages);
    	logger.error("Total Turnaround Time = " + turnAroundTime);
    	logger.error("Average Turnaround Time = " + (turnAroundTime/nOfMessages));
    }

}
