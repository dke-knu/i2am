package i2am.tta.common;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class DeclaringBolt extends BaseRichBolt{
    private int targetIndex;
    private OutputCollector collector;

    public DeclaringBolt(int targetIndex){
        this.targetIndex = targetIndex;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);
        String target = data.split(",")[targetIndex];
        collector.emit(new Values(data, target, targetIndex, input.getLongByField("start-time")));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "target", "targetIndex", "start-time"));
    }
}
