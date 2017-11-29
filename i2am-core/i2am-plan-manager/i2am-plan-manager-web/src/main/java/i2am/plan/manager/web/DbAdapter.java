package i2am.plan.manager.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


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

	public boolean login(String id, String pw) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT * FROM tbl_user "
					+ "WHERE ID='" + id + "' AND PASSWORD='" + pw +"'";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())	return true;
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

	public boolean join(String id, String name, String pw) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT * FROM tbl_user "
					+ "WHERE ID='" + id + "'";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next())	return false;

			sql = "INSERT INTO tbl_user (ID, NAME, PASSWORD) "
					+ "VALUES ('" + id + "', '" + name + "', '" + pw + "')";

			return stmt.executeUpdate(sql) > 0;

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

	public boolean addTestData(String owner, String name, String path, long size, String type) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT IDX FROM tbl_user WHERE ID='" + owner + "'";
			ResultSet rs = stmt.executeQuery(sql);
			if ( !rs.next() )	return false;
			int ownerIdx = rs.getInt("IDX");

			sql = "INSERT INTO tbl_src_test_data (F_OWNER, NAME, CREATED_TIME, FILE_PATH, FILE_SIZE, FILE_TYPE) "
					+ "VALUES (" + ownerIdx + ", '" + name + "', now(), '" + path + "', " + size + ", '" + type + "')";
			return stmt.executeUpdate(sql) > 0;

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

	public JSONArray getTestData(String owner) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT NAME, CREATED_TIME, SUBSTRING_INDEX(FILE_PATH, '/', -1) AS FILE_NAME, FILE_SIZE, FILE_TYPE "
					+ "FROM tbl_src_test_data WHERE F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + owner + "' );";
			return getJSONArray(stmt.executeQuery(sql));

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
		return null;
	}

	public JSONArray getListSrc(String owner) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT NAME, CREATED_TIME, IS_RECOMMENDATION, USES_LOAD_SHEDDING, STATUS, RECOMMENDED_SAMPLING "
					+ "FROM tbl_src WHERE F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + owner + "' );";
			return getJSONArray(stmt.executeQuery(sql));

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
		return null;
	}

	public JSONArray getListDst(String owner) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT NAME, CREATED_TIME, STATUS "
					+ "FROM tbl_dst WHERE F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + owner + "' );";
			return getJSONArray(stmt.executeQuery(sql));

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
		return null;
	}

	public JSONArray getListPlan(String owner) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT p.NAME, p.CREATED_TIME, p.STATUS, s.TRANS_TOPIC as INPUT, d.TRANS_TOPIC as OUTPUT "
					+ "FROM tbl_plan p, tbl_src s, tbl_dst d "
					+ "WHERE p.F_SRC = s.IDX AND p.F_DST = d.IDX AND "
					+ "p.F_OWNER = ( SELECT IDX FROM tbl_user WHERE ID='" + owner + "' );";
			return getJSONArray(stmt.executeQuery(sql));

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
		return null;
	}

	private JSONArray getJSONArray(ResultSet rs) {
		JSONArray jarray = new JSONArray();

		try {
			while(rs.next()) {
				JSONObject obj = new JSONObject();
				ResultSetMetaData rmd = rs.getMetaData();

				for ( int i=1; i<=rmd.getColumnCount(); i++ ) {
					obj.put(rmd.getColumnLabel(i),rs.getString(rmd.getColumnLabel(i)));
				}

				jarray.add(obj); 
			}
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return jarray;
	}

	public boolean checkRedundancy(String type, String owner, String name) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = this.getConnection();
			stmt = con.createStatement();

			sql = "SELECT * FROM tbl_" + type.toLowerCase() + " "
					+ "WHERE NAME = '" + name + "' AND F_OWNER = "
					+ "( SELECT IDX FROM tbl_user WHERE ID='" + owner + "' );";
			
			if (!stmt.executeQuery(sql).next())
				return true;

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
}
