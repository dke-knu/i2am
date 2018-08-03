package i2am.sampling.passing;

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

public class SystematicPassingBolt extends BaseRichBolt {
    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(SystematicPassingBolt.class);

    public SystematicPassingBolt(){}

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);

        collector.emit(new Values("", data));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("", "data"));
    }
}