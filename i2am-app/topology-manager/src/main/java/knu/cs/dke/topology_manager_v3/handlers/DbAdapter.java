package knu.cs.dke.topology_manager_v3.handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import knu.cs.dke.topology_manager_v3.sources.KafkaSource;
import knu.cs.dke.topology_manager_v3.sources.Source;


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

			sql = "SELECT * FROM TBL_USER "
					+ "WHERE ID='" + id + "' AND PASSWORD='" + pw +"'";
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

	public boolean addSource(Source source) {

		Connection con = null;
		Statement stmt = null;

		try {
			con = this.getConnection();
			stmt = con.createStatement();

			// user 테이블에서 [이름]으로 [IDX]가져오기
			String owner = source.getOwner();
			String ownerQuery = "SELECT IDX FROM tbl_user WHERE ID = '" + owner + "'";
			ResultSet ownerIdx = stmt.executeQuery(ownerQuery);
			ownerIdx.next();
			int ownerNumber = ((Number) ownerIdx.getObject(1)).intValue();
			
			// test data 테이블에서 [파일 이름]으로 [IDX]가져오기
			String file = source.getTestData();
			String fileQuery = "SELECT IDX FROM tbl_src_test_data WHERE NAME = '" + file +"'";
			ResultSet fileIdx = stmt.executeQuery(fileQuery);
			fileIdx.next();
			int fileNumber = ((Number) fileIdx.getObject(1)).intValue();
			
			// INSERT source
			String insertSource;
			insertSource = "INSERT INTO tbl_src " 
							+ "VALUES ("
							+ "'0',"
							+ "'" + source.getSourceName() + "',"
							+ "'" + source.getCreatedTime() + "',"
							+ "'" + source.getModifiedTime() + "',"
							+ "'" + source.getStatus() + "',"
							+ "'" + ownerNumber + "',"
							+ "'" + source.getUseIntelliEngine() + "',"
							+ "'" + source.getUseLoadShedding() + "',"
							+ "'" + fileNumber + "',"
							+ "'" + source.getSrcType() + "',"
							+ "'" + source.getSwitchMessaging() + "'"
							+ ")";			

			ResultSet insert = stmt.executeQuery(insertSource);			

			// Source에서 Index 가져오기 [이름으로]
			String last = "SELECT IDX FROM tbl_src WHERE NAME = " + "'" + source.getSourceName() +"'";
			ResultSet idx = stmt.executeQuery(last);
			idx.next();
			int sourceNumber = ((Number) idx.getObject(1)).intValue();
						
			switch(source.getSrcType()) {

			case "kafka":				
				KafkaSource ks = (KafkaSource) source;
				String insertKafka = "INSERT INTO tbl_src_kafka_info "
										+ "VALUES ("
										+ "'0',"
										+ "'" + ks.getZookeeperIp() + "',"
										+ "'" + ks.getZookeeperPort() + "',"
										+ "'" + ks.getTopic() + "',"
										+ "'" + sourceNumber + "'" // 마지막 idx 값을 외래키로
										+ ")";
				ResultSet kafka = stmt.executeQuery(insertKafka);
				break;

			case "database":
				break;

			default:
				System.out.println("[DBAdapter] Source Type Error.");
				break;			
			}

			if (insert.next())   return true;

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
