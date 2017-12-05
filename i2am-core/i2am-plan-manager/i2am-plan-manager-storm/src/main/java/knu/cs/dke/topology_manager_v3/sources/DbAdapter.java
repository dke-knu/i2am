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


public class DbAdapter {  

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

	public boolean sourceDbCheck(String sourceName) {
		
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT STATUS FROM TBL_SRC" + "WHERE NAME='" + sourceName + "'";
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			String status = ((String) rs.getObject(1)).toString();			
			
			if (status.equals("ACTIVE"))   return true;
			else if(status.equals("DEACTIVE")) return false;
			
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
		return true;
	}		
}




