package i2am.tta.query;

import java.util.HashMap;
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

import i2am.query.parser.Node;

public class MultivariateQueryFilteringBolt extends BaseRichBolt{
	private final static Logger logger = LoggerFactory.getLogger(MultivariateQueryFilteringBolt.class);

	private OutputCollector collector;
	private Node query;
	private String[] schema;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.query = Node.parse("( ( int NGT 50 ) AND ( double NGT 0.5 ) )");
		this.schema = new String[]{"int", "double", "text", "carray"};
	}

	@Override
	public void execute(Tuple input) {
		String data = input.getString(0);
		if ( !query.evaluate(arraysToMap(schema, data.split(","))) )
	        collector.emit(new Values(data, 0, input.getLongByField("start-time")));
		else 
			collector.emit(new Values(data, 1, input.getLongByField("start-time")));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("data", "sampleFlag", "start-time"));
	}

	private Map<String, String> arraysToMap (String[] key, String[] value) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i=0; i<value.length; i++) {
			map.put(key[i], value[i]);
		}
		return map;
	}
}
