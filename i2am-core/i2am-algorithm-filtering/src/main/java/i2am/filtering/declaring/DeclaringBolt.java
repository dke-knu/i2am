package i2am.filtering.declaring;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import i2am.filtering.common.DbAdapter;

import java.sql.SQLException;
import java.util.Map;

public class DeclaringBolt extends BaseRichBolt{
    private int targetIndex;
    private String topologyName;
    private String algorithmName;
    private OutputCollector collector;

    public DeclaringBolt(String topologyName, String algorithmName){
        this.topologyName = topologyName;
        this.algorithmName = algorithmName;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        try {
            targetIndex = DbAdapter.getInstance().getTargetIndex(topologyName, algorithmName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);
        String target = data.split(",")[targetIndex];
        collector.emit(new Values(data, target, targetIndex));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data", "target", "targetIndex"));
    }
}