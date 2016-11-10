package org.dynamic.anomaly.detection.storm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

public class AD3SigmaBolt implements IRichBolt {
	private OutputCollector collector;
	
	private Connection conn;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		String cluster = input.getStringByField("cluster");
		String host = input.getStringByField("host");
		String key = input.getStringByField("key2");
		double value = input.getDoubleByField("value");
		double ma = input.getDoubleByField("mvAvg");
		double sd = input.getDoubleByField("mvStd");
		long time = input.getLongByField("time");
		
		double upper = ma+(3*sd);
		double lower = ma-(3*sd);
		boolean isAnomaly = false;
		if(value > upper && value < lower)
			isAnomaly = true;
		System.out.println(isAnomaly);
		
		if (conn == null)	conn = getConnection();
		transmit(conn, cluster, host, key, value, upper, lower, isAnomaly, time);
	}

	public void cleanup() {
		// TODO Auto-generated method stub

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	private Connection getConnection() {
		final String driver = "org.mariadb.jdbc.Driver";
		final String url = "jdbc:mysql://" + "/anomalydetection"; // db ip
		final String uId = ""; // db id
		final String uPwd = ""; // db passwd

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
			int result = stmt.executeUpdate(sql);

			stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
