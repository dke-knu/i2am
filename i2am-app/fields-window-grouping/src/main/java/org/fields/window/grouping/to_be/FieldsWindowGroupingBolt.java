
package org.fields.window.grouping.to_be;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class FieldsWindowGroupingBolt implements IRichBolt {
	private OutputCollector collector;
	
	private Map<String, List<Tuple>> windows;
	private final int WINDOW_SIZE = 5;
	
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.windows = new HashMap<String, List<Tuple>>();
	}

	public void execute(Tuple input) {
		String sender = input.getStringByField("sender");
				
		if (!windows.containsKey(sender)) {
			List<Tuple> value = new ArrayList<Tuple>();
			value.add(input);
			windows.put(sender, value);
		} else {
			List<Tuple> values = windows.get(sender);
			values.add(input);
			if (values.size() > WINDOW_SIZE)
				values.remove(0);
			windows.put(sender, values);
		}
		
		collector.emit(new Values(sender, windows.get(sender)));
	}

	public void cleanup() {
		// TODO Auto-generated method stub
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("sender", "tuples"));
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}