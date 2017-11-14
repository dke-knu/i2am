package i2am.benchmark.storm.reservoir;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeclareFieldBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(DeclareFieldBolt.class);
	private OutputCollector outputCollector = null;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
		// TODO Auto-generated method stub
		this.outputCollector = outputCollector;
	}
	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		
		// Input Time
		long inputTime = System.currentTimeMillis();
		
		// Get JSON From Tuple 
		JSONParser parser = new JSONParser();
		JSONObject message = null;
		
		try {
			message = (JSONObject) parser.parse(new String(tuple.getString(0)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Put Input Time
		message.put("inputTime", inputTime);
		
		//Emit
		outputCollector.emit(new Values(message.toString()));
		outputCollector.ack(tuple);
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("message"));
	}
	
	@Override
    public void cleanup() {
    }    
}


