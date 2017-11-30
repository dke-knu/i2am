package i2am.Filtering;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class QueryFilteringBolt extends BaseRichBolt{
    private List<String> wordArray; // Filter List

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(QueryFilteringBolt.class);

    public QueryFilteringBolt(List<String> wordArray){
        this.wordArray =  wordArray;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);

        /* Query Filtering */
        for(String word : wordArray){
            if(data.contains(word)){
                collector.emit(new Values(data));
                break;
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}
