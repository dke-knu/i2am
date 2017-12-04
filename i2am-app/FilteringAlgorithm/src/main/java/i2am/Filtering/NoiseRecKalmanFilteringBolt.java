package i2am.Filtering;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sbpark on 2017-11-30.
 */
public class NoiseRecKalmanFilteringBolt extends BaseRichBolt{
    private double x = 0, R = 0.5, P = 1000, Q;
    private int windowSize = 32;
    List<Double> inputData = new ArrayList<Double>();

    /* RedisKey */
    private String redisKey = null;
    private String QValueKey = "Q_val";

    /* Jedis */
    private transient JedisCommandsInstanceContainer jedisContainer;
    private JedisClusterConfig jedisClusterConfig;
    private JedisCommands jedisCommands = null;

    private OutputCollector collector;

    /* Logger */
    private final static Logger logger = LoggerFactory.getLogger(NoiseRecKalmanFilteringBolt.class);

    public NoiseRecKalmanFilteringBolt(String redisKey, JedisClusterConfig jedisClusterConfig){
        this.redisKey = redisKey;
        this.jedisClusterConfig = jedisClusterConfig;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        /* Get Q value from user(redis) */
        if (jedisClusterConfig != null) {
            this.jedisContainer = JedisCommandsContainerBuilder.build(jedisClusterConfig);
            jedisCommands = jedisContainer.getInstance();
        } else {
            throw new IllegalArgumentException("Jedis configuration not found");
        }

        Q = Double.parseDouble(jedisCommands.hget(redisKey, QValueKey));
    }

    @Override
    public void execute(Tuple input) {
        double x_present = Double.parseDouble(input.getString(0));
        double x_next, P_next, K, z, H = 1;

        if(inputData.size() < windowSize) {     // 다음 R 계산을 위해 차곡차곡 담는다
            inputData.add(x_present);
        }

        x_next = x;
        P_next = P+ Q; 	//Q: white noise --> by environment
        K = P_next*H / (H*H*P_next + R);	//kalman gain

        z = x_present;
        x = x_next + K*(z - H*x_next);		// filtered data
        P = (1 - K*H)*P_next;

        if(inputData.size() == windowSize) {    // calculate R
            // double list to double array
            double[] inputDataArray = new double[windowSize];
            for(int i=0; i< windowSize; i++) {
                inputDataArray[i] = inputData.get(i);
            }

            // calculate R
            RCalculator rCalculator = new RCalculator();
            R = rCalculator.calcR(inputDataArray, windowSize);

            // clear list
            inputData.clear();
        }

        collector.emit(new Values(Double.toString(x)));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("data"));
    }
}


class RCalculator{

    public double calcR(double[] DataSet, int winSize){	//sensor and original
        int f = winSize/4;
        double resultR;
        double sum = 0.0;
        double[] FWT, inverseFWT;
        // Estimation of the raw data (= w/o noise)
        FWT = Daubechies_forward_FWT_1d(winSize, DataSet);

        // extract raw data
        for(int i=0; i<winSize; i++){
            if(i<f){
                continue;
            }else{
                FWT[i] = 0;
            }
        }

        inverseFWT = Daubechies_inverse_FWT_1d(winSize, FWT);

        // Mean Square Error
        //DataSet - fwt
        for(int i=0; i< DataSet.length; i++){
            //abs(dist)
            double tmp = DataSet[i] - inverseFWT[i];
            if(tmp<0) tmp *= -1;

            sum += tmp;

            //estimate Q value
        }
        resultR = sum/(DataSet.length-1);
        resultR = Math.sqrt(resultR);

        return resultR;
    }

    public double[] Daubechies_forward_FWT_1d (int n, double[] DataSet)
    {
        double[] fastWaveletTransform = new double[n];
        int m;
        int k;

        k = logB(n, 2);

        for (m = 0 ;  m < n;  m++) fastWaveletTransform[m] = DataSet[m];

        for (m = k-1; m >= 0 ; m--)
            fastWaveletTransform = Daubechies_forward_pass_1d (m+1, fastWaveletTransform);

        return fastWaveletTransform;
    }

    /*--------------------------------------------------------------------------*/
    public double[] Daubechies_forward_pass_1d (int n, double[] fastWaveletTransform)
    {
        int i;
        int npts;
        double[] a = null;
        double[] c = null;

        final double[] h = new double[]{ 0.683013, 1.18301, 0.316987, -0.183013 };//const double

        npts = powerof2(n);

        a = new double[npts/2];
        c = new double[npts/2];

        for (i = 0;  i < npts/2;  i++)
        {
            a[i] = (h[0]*fastWaveletTransform[(2*i)%npts] + h[1]*fastWaveletTransform[(2*i+1)%npts] + h[2]*fastWaveletTransform[(2*i+2)%npts] + h[3]*fastWaveletTransform[(2*i+3)%npts]) / 2.0;
            c[i] = (h[3]*fastWaveletTransform[(2*i)%npts] - h[2]*fastWaveletTransform[(2*i+1)%npts] + h[1]*fastWaveletTransform[(2*i+2)%npts] - h[0]*fastWaveletTransform[(2*i+3)%npts]) / 2.0;
        }

        for (i = 0;  i < npts/2;  i++)
        {
            fastWaveletTransform[i] = a[i];
            fastWaveletTransform[i + npts/2] = c[i];
        }

        a = null;
        c = null;

        return fastWaveletTransform;
    }
	/*---------------------------------------------------------------------------*/
	/*
	  Calculate the Daubechies inverse fast wavelet transform in 1-dimension.
	*/
    //inverse

    public double[] Daubechies_inverse_FWT_1d (int n, double[] DataSet_dwt_daubechies)/*(int n, float * s)*/
    {
        double[] inverseFastWaveletTransform = new double[n];
        int m;
        int k;

        k = logB(n, 2);

        for (m = 0 ;  m < n;  m++)
            inverseFastWaveletTransform[m] = DataSet_dwt_daubechies[m];

        for (m = 1;  m <=k;  m++)
            inverseFastWaveletTransform = Daubechies_inverse_pass_1d (m, inverseFastWaveletTransform);

        return inverseFastWaveletTransform;
    }

	/*---------------------------------------------------------------------------*/

    public double[] Daubechies_inverse_pass_1d (int n, double[] inverseFastWaveletTransform)/*(int n, float * s)*/
    {
        int i;
        int nptsd2;
        int npts;
        double[] temp = null;
        double[] c = null;
        final double[] h = new double[]{ 0.683013, 1.18301, 0.316987, -0.183013 };//const double

        npts = powerof2 (n);
        nptsd2 = npts/2;
        c = new double[nptsd2];
        System.arraycopy(inverseFastWaveletTransform, nptsd2, c, 0, nptsd2);

        temp = new double[npts];

        for (i = 0;  i < nptsd2;  i++)
        {
            temp[2*i] = h[2]*inverseFastWaveletTransform[(i-1+nptsd2)%nptsd2] + h[1]*c[(i-1+nptsd2)%nptsd2] + h[0]*inverseFastWaveletTransform[i] + h[3]*c[i];
            temp[2*i+1] = h[3]*inverseFastWaveletTransform[(i-1+nptsd2)%nptsd2] - h[0]*c[(i-1+nptsd2)%nptsd2] + h[1]*inverseFastWaveletTransform[i] - h[2]*c[i];
        }

        for (i = 0;  i < npts;  i++)
            inverseFastWaveletTransform[i] = temp[i];

        temp = null;

        return inverseFastWaveletTransform;
    }


    /*******************************************************************************/
    int powerof2(int n)
    {
        int i, j;

        j = 1;

        if (n > 0)
            for (i = 0;  i < n;  i++)
                j *= 2;
        else if (n < 0)
            j = 0;

        return (j);
    }

    /*******************************************************************************/

    int logB(int x, int b)
    {
        double value, base, result;

        value = Math.log(x);
        base = Math.log(b);
        result = (value / base)+0.5; /*double/double 값이라 소수점 버림 발생하여 반올림 해줌.*/

        return (int)result;
    }
}

