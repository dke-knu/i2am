package i2am.Sampling;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import scala.util.Random;

import java.util.Map;

public class BBSSiteBolt extends BaseRichBolt{
    private OutputCollector collector;

    public BBSSiteBolt(){}

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getStringByField("data");
        int count = input.getIntegerByField("count");
        int round = input.getIntegerByField("round");
        int bitNumber = bitGenerate(round);

        if(bitNumber < 2) collector.emit(new Values(data, count, bitNumber)); // Emit
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "count", "bitNumber"));
    }

    public int bitGenerate(int round){
        int maxNumber = (int)Math.pow(2 ,round+1);

        return  (int)(Math.random()*maxNumber);
    }
}