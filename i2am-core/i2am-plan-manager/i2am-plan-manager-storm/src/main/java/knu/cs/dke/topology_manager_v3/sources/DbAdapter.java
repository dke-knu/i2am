package knu.cs.dke.topology_manager_v3.sources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import knu.cs.dke.topology_manager_v3.Plan;
import knu.cs.dke.topology_manager_v3.destinations.Destination;
import knu.cs.dke.topology_manager_v3.destinations.KafkaDestination;
import knu.cs.dke.topology_manager_v3.sources.KafkaSource;
import knu.cs.dke.topology_manager_v3.sources.Source;
import knu.cs.dke.topology_manager_v3.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager_v3.topolgoies.BinaryBernoulliSamplingTopology;


public class DbAdapter implements Runnable {  

	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();
	//   private static final Log logger = LogFactory.getLog(klass);

	// singleton
	private volatile static DbAdapter instance;
	public static DbAdapter getInstance() {
		if(instance == null) {
			synchronized(DbAdapter.class) {
				if(instance == null) {
					instance = new DbAdapter();
				}
			} 
		} 
		return instance;
	}

	protected DbAdapter() {}

	private Connection cn;
	protected Connection getConnection() throws SQLException {
		String driverName = "org.mariadb.jdbc.Driver";
		String url = "jdbc:mariadb://" + "114.70.235.43" + ":" + "3306" + "/i2am";
		String user = "plan-manager"/* USER */;
		String password = "dke214"/* PASSWD */;

		try {
			Class.forName(driverName);
			this.cn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			System.out.println("Load error: " + e.getStackTrace());
		} catch (SQLException e) {
			System.out.println("Connection error: " + e.getStackTrace());
		}

		return cn;
	}

	protected void close(Connection con) throws SQLException {
		con.close();
	}

	/*
		DbAdapter.getInstance().login(id, pw);	
	 */
	public boolean login(String id, String pw) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT * FROM TBL_USER " + "WHERE ID='" + id + "' AND PASSWORD='" + pw +"'";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())   return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					close(con);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}	

	@Override
	public void run() {
		
		
	}
}




