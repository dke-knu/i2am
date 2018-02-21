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
import org.apache.storm.tuple.Tuple;

public class ADPerformanceBolt implements IRichBolt {
	private OutputCollector collector;
	
	private Connection conn;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		long startTime = input.getLongByField("startTime");
		long endTime = input.getLongByField("endTime");
		long loggingTime = input.getLongByField("loggingTime");
		
		if (conn == null)	conn = getConnection();
		transmit(conn, startTime, endTime, loggingTime);
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
	
	private void transmit(Connection conn, long startTime, long endTime, long loggingTime) {
		Statement stmt = null;

		try{
			stmt = conn.createStatement();

			String sql = "INSERT INTO TBL_PERFORMANCE VALUES "
					+ "(" + startTime + ", " + endTime + ", FROM_UNIXTIME(" + loggingTime + "))";
			int result = stmt.executeUpdate(sql);

			stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
