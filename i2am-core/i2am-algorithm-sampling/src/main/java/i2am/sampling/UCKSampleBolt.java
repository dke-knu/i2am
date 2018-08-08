package i2am.sampling;

import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainerBuilder;
import org.apache.storm.redis.common.container.JedisCommandsInstanceContainer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCommands;

import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;


public class UCKSampleBolt extends BaseRichBolt{
    private long count;
    private int windowSize;
    private int  samplingRate;
    private double ucUnderBound;
    PriorityQueue<SampleElement> sample = new PriorityQueue<SampleElement>();

    /* RedisKey */
    private String redisKey = null;
    private String srKey = "SamplingRate";
    private String ucKey = "UCUnderBound";

    /* Jedis */
    private JedisCommandsInstanceContainer jedisContainer = null;
    private JedisClusterConfig jedisClusterConfig = null;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(UCKSampleBolt.class);

    public UCKSampleBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        this.count = 0;
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;

        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

        logger.info("############# UCKSAMPLEBOLT");
        logger.info(jedisCommands.hget(redisKey, srKey));

		/* Get parameters */
        samplingRate = Integer.parseInt(jedisCommands.hget(redisKey, srKey));
        ucUnderBound = Double.parseDouble(jedisCommands.hget(redisKey, ucKey));
        windowSize = window_calculator();

        logger.info("##########HJKIM windoeSize: " + windowSize);

    }

    @Override
    public void execute(Tuple input) {
        double rand;
        int wLength;
        int sLength;
        String element = input.getString(0);
        count++;

        if((count%samplingRate)==0) sLength=samplingRate;
        else sLength=(int)count%samplingRate;
        wLength=(int)count%windowSize;

        rand = Math.random();

        if(sLength==1){
            //sample increase
            sample.add(new SampleElement(element, rand));  //current data insert
        }
        else{
            //current slot sampling
            SampleElement tmp = sample.peek();
            if (rand > tmp.getRand() ) {
                sample.poll();
                sample.add(new SampleElement(element,rand));
            }
        }

        if (wLength==0){

            for (SampleElement aSample : sample) {

                collector.emit(new Values(aSample.getElement())); //emit

            }
            sample.clear();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }

    public double combi(int n, int r){
        double result=1;
        int i;
        int min_r, max_r;

        if(r>n-r){
            max_r=r;
            min_r=n-r;
        }else{
            max_r=n-r;
            min_r=r;
        }

        for(i=0; i<min_r; i++){
            result = result * (n-i)/(i+1);
        }
        return result;
    }

    public double uc_calculator(int k){
        int x, r, m, i;
        double result1=0;
        double result2=0;
        double p = 1/samplingRate;

        r=(int)Math.floor(k*p);
        m=samplingRate;

        if(0>(r+1)-m) x=0;
        else x=r+1-m;

        for(i=x; i<=r; i++){
            result1 = result1 + (combi(k,i)*combi(m,r+1-i));
        }
        result2 = result1/combi(k+m,r+1);

        return result2;
    }

    public int window_calculator(){
        int widowSize=0;
        int k=samplingRate;
        double uc=100;
        double b_uc=0;

        uc = uc_calculator(k);
        b_uc = uc;
        windowSize=k;

        while(true){
            if(uc<ucUnderBound){
                break;
            }
            if(Double.isNaN(uc)) break;     // Nan Exception
            if(Double.isFinite(uc)) break;  // Infinite Exception

            b_uc=uc;
            windowSize=k;

            k+=samplingRate;
            uc = uc_calculator(k);

        }

        return windowSize;
    }
}

class SampleElement implements Comparable<SampleElement>{
    private String data;
    private double rand;

    SampleElement(String data, double rand){
        this.data=data;
        this.rand=rand;
    }

    String getElement() { return data; }

    double getRand() { return rand; }

    @Override
    public int compareTo(SampleElement o) {
        if (rand<o.rand) return -1;
        else if (rand==o.rand) return 0;
        else return 1;
    }
}