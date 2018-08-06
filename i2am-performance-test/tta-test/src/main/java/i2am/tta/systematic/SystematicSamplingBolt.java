package i2am.tta.systematic;

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

public class SystematicSamplingBolt extends BaseRichBolt {
	private final static Logger logger = LoggerFactory.getLogger(SystematicSamplingBolt.class);

	private int interval;
    private int randomNumber;
    private long count = 0;

    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        interval = 5;
        randomNumber = (int)(Math.random()*interval);
    }

    @Override
    public void execute(Tuple input) {
        if(count == Long.MAX_VALUE) count = 0; // Overflow Exception

        count++;
        String data = input.getStringByField("data");

        /* Systematic Sampling */
        if(count%interval == randomNumber) {
            collector.emit(new Values(data, 1, input.getLongByField("start-time")));
        } else {
        	collector.emit(new Values(data, 0, input.getLongByField("start-time")));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "sampleFlag", "start-time"));
    }
}