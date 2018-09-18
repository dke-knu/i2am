package i2am.plan.manager.kafka;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbAdapter {  
	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();
	//	private static final Log logger = LogFactory.getLog(klass);

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
		String url = "jdbc:mariadb://114.70.235.43:3306/i2am";
		String user = "plan-manager";
		String password = "dke214";

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

	public String getInputTopic(String id, String srcName) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;

		String topic = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT TRANS_TOPIC "
					+ "FROM tbl_src WHERE NAME = '" + srcName + "' AND F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + id + "' );";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())	return rs.getString("TRANS_TOPIC");
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
		return topic;
	}

	public String getSrcIdx(String id, String srcName) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;

		String topic = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT IDX "
					+ "FROM tbl_src WHERE NAME = '" + srcName + "' AND F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + id + "' );";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())	return rs.getString("TRANS_TOPIC");
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
		return topic;
	}



	public String getOutputTopic(String id, String dstName) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;

		String topic = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT TRANS_TOPIC "
					+ "FROM tbl_dst WHERE NAME = '" + dstName + "' AND F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + id + "' );";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())	return rs.getString("TRANS_TOPIC");
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
		return topic;
	}

	public boolean getSwtichValue(final String topic){
		Connection con = null;
		Statement stmt = null;
		String sql = null;

		boolean switchValue = false;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT IF (SWITCH_MESSAGING = 'Y', 'true', 'false') "
					+ "AS result from " + "tbl_src" + " WHERE TRANS_TOPIC = '" + topic + "'";
			ResultSet rs = stmt.executeQuery(sql);

			if(rs.next()){
				return Boolean.valueOf(rs.getString("result")).booleanValue();
			}


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
		return switchValue;
	}
}
