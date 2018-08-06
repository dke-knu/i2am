package i2am.metadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbAdapter {  
	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();
	private static final Log logger = LogFactory.getLog(klass);
	 
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

	private final DbAdmin dbAdmin;
	private final DataSource ds;

	private DbAdapter() {
		dbAdmin = DbAdmin.getInstance();
		ds = dbAdmin.getDataSource();
	}
	
	public int getUserCounts () {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			sql = "SELECT COUNT(*) AS C FROM tbl_user";
			
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			return rs.getInt("C");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
}
