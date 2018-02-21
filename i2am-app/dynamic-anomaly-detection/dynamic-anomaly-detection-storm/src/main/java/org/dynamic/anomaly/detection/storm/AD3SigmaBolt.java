package org.dynamic.anomaly.detection.storm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class AD3SigmaBolt implements IRichBolt {
	private OutputCollector collector;
	
	private Connection conn;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		String cluster = input.getStringByField("cluster");
		String host = input.getStringByField("host");
		String key = input.getStringByField("key");
		double value = input.getDoubleByField("value");
		double ma = input.getDoubleByField("mvAvg");
		double msd = input.getDoubleByField("mvStd");
		long time = input.getLongByField("time");
		// for performance.
		long startTime = input.getLongByField("startTime");
		
		double upper = ma+(3*msd);
		double lower = ma-(3*msd);
		boolean isAnomaly = false;
		if(value > upper || value < lower)
			isAnomaly = true;
		
		// for performance.
		collector.ack(input);
		long endTime = new Date().getTime();

		if (conn == null)	conn = getConnection();
		transmit(conn, cluster, host, key, value, upper, lower, isAnomaly, time);
		
		if (endTime>startTime)	collector.emit(new Values(startTime, endTime, time));
	}

	public void cleanup() {
		// TODO Auto-generated method stub

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		// for performance.
		declarer.declare(new Fields("startTime", "endTime", "loggingTime"));
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	private Connection getConnection() {
		final String driver = "org.mariadb.jdbc.Driver";
		final String url = "jdbc:mysql://114.70.235.40/anomalydetection"; // db ip
		final String uId = "anomalydetection"; // db id
		final String uPwd = "dke304"; // db passwd

		Connection conn = null;
		try{
			Class.forName(driver);
			conn = DriverManager.getConnection(url,uId,uPwd);
			
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return conn;
	}
	
	private void transmit(Connection conn,
			String clusterName, String hostName, String logKey, 
			double logValue, double upperBound, double lowerBound,
			boolean isAnomaly, long loggingTime) {
		Statement stmt = null;

		try{
			stmt = conn.createStatement();

			String sql = "INSERT INTO TBL_ANOMALY_DETECTION VALUES ("
					+ "'" + clusterName + "', '" + hostName + "', '" + logKey + "', "
					+ logValue + ", " + upperBound + ", " + lowerBound + ", "
					+ isAnomaly + ", FROM_UNIXTIME(" + loggingTime + "))";
			System.out.println(sql);
			int result = stmt.executeUpdate(sql);

			stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
