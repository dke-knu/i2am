package i2am.filtering.multivariate;

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

import i2am.query.parser.DbAdapter;
import i2am.query.parser.Node;

public class MultivariateQueryFilteringBolt extends BaseRichBolt{
	private final static Logger logger = LoggerFactory.getLogger(MultivariateQueryFilteringBolt.class);

	private String topologyName;

    private OutputCollector collector;
    private Node query;
    private String[] schema;

    public MultivariateQueryFilteringBolt(String topologyName){
        this.topologyName = topologyName;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.query = DbAdapter.getInstance().getQuery(topologyName);
        this.schema = DbAdapter.getInstance().getSchema(topologyName);
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);
        if ( !query.evaluate(arraysToMap(schema, data.split(","))) )
        	return;
        collector.emit(new Values(data));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
    
    private Map<String, String> arraysToMap (String[] key, String[] value) {
    	Map<String, String> map = new HashMap<String, String>();
    	for (int i=0; i<key.length; i++) { // value.length --> key.length로 바꿈
    		map.put(key[i], value[i]);
    	}
    	return map;
    }
}
