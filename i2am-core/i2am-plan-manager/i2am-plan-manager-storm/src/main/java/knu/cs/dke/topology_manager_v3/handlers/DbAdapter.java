package knu.cs.dke.topology_manager_v3.handlers;

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
					+ "'" + source.getSwitchMessaging() + "',"
					+ "'" + source.getTransTopic() + "'"
					+ ")";			

			ResultSet insert = stmt.executeQuery(insertSource);			

			// Source에서 Index 가져오기 [이름으로]
			String last = "SELECT IDX FROM tbl_src WHERE NAME = " + "'" + source.getSourceName() +"'";
			ResultSet idx = stmt.executeQuery(last);
			idx.next();
			int sourceNumber = ((Number) idx.getObject(1)).intValue();

			switch(source.getSrcType()) {

			case "KAFKA":				
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

			case "DABABASE":
				break;

			default:
				System.out.println("[DBAdapter] Source Type Error.");
				break;			
			}

			System.out.println("[DBAdapter] Source Added.");
			
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

	public boolean addPlan(Plan plan) {

		Connection con = null;
		Statement stmt = null;

		try {
			con = this.getConnection();
			stmt = con.createStatement();

			// Get User.
			String owner = plan.getOwner();
			String ownerQuery = "SELECT IDX FROM tbl_user WHERE ID = '" + owner + "'";
			ResultSet ownerIdx = stmt.executeQuery(ownerQuery);
			ownerIdx.next();
			int ownerNumber = ((Number) ownerIdx.getObject(1)).intValue();

			// Get Source.
			String source = plan.getSource();
			String sourceQuery = "SELECT IDX FROM tbl_src WHERE NAME = '" + source + "'";
			ResultSet sourceIdx = stmt.executeQuery(sourceQuery);
			sourceIdx.next();
			int sourceNumber = ((Number) sourceIdx.getObject(1)).intValue();

			// Get Destination.
			String destination = plan.getDestination();
			String destinationQuery = "SELECT IDX FROM tbl_dst WHERE NAME = '" + destination + "'";
			ResultSet destinationIdx = stmt.executeQuery(destinationQuery);
			destinationIdx.next();
			int destinationNumber = ((Number) destinationIdx.getObject(1)).intValue();

			// Plan			
			String planQuery = "INSERT INTO tbl_plan "
					+ "VALUES ("
					+ "'0',"
					+ "'" + plan.getPlanName() + "',"
					+ "'" + plan.getCreatedTime() + "',"
					+ "'" + plan.getModifiedTime() + "',"
					+ "'" + plan.getStatus() + "',"
					+ "'" + ownerNumber + "',"
					+ "'" + sourceNumber + "',"
					+ "'" + destinationNumber + "'"
					+ ")";
			ResultSet insertPlan = stmt.executeQuery(planQuery);

			// Topology!
			List<ASamplingFilteringTopology> topologies = plan.getTopologies();			

			// Get Plan Idx.
			String planName = plan.getPlanName();
			String planNameQuery = "SELECT IDX FROM tbl_plan WHERE NAME = '" + planName + "'";
			ResultSet planIdx = stmt.executeQuery(planNameQuery);
			planIdx.next();
			int planNumber = ((Number) planIdx.getObject(1)).intValue();

			System.out.println("[DBAdapter] Topology to DB !");

			for( int i=0; i < topologies.size(); i++ ) 
			{				
				// 토폴로지 단 하나!
				ASamplingFilteringTopology topology = topologies.get(i);

				// Topology Insert!
				String topologyQuery = "INSERT INTO tbl_topology "
						+ "VALUES ("
						+ "'0',"
						+ "'" + topology.getCreatedTime() + "',"
						+ "'" + topology.getModifiedTime() + "',"
						+ "'" + topology.getStatus() + "',"
						+ "'" + topology.getIndex() + "',"
						+ "'" + topology.getTopologyType() + "',"
						+ "'" + planNumber + "',"
						+ "'" + topology.getInputTopic() + "',"
						+ "'" + topology.getOutputTopic() + "',"
						+ "'" + topology.getTopologyName() + "',"
						+ "'" + topology.getRedisKey() + "'"
						+ ")";

				ResultSet insertTopology = stmt.executeQuery(topologyQuery) ;

				// 이제 Type에 맞춰 Parameter Table에 값을 넣으세요!
				// 자신에 해당하는 Topology를 찾으려면
				// Plan & Topology Index 일치				
				String getTopologyQuery = "SELECT IDX FROM tbl_topology "
						+ "WHERE F_PLAN = '" + planNumber + "'"
						+ "AND TOPOLOGY_INDEX = '" + i +"'";
				ResultSet topologyIdx = stmt.executeQuery(getTopologyQuery);
				topologyIdx.next();
				int topologyNumber = ((Number) topologyIdx.getObject(1)).intValue();

				switch(topology.getTopologyType()) {

				case "BINARY_BERNOULLI_SAMPLING":

					BinaryBernoulliSamplingTopology bbs = (BinaryBernoulliSamplingTopology) topology;
					// Topology Insert!
					String paramsQuery = "INSERT INTO tbl_params_binary_bernoulli_sampling "
							+ "VALUES ("
							+ "'0',"
							+ "'" + topologyNumber + "',"
							+ "'" + bbs.getSampleSize() + "',"
							+ "'" + bbs.getWindowSize() + "',"
							+ "'" + bbs.getInputTopic() + "',"
							+ "'" + bbs.getOutputTopic() + "'"					
							+ ")";
					ResultSet paramsResult = stmt.executeQuery(paramsQuery);
					break;

				default:
					System.out.println("[DBAdapter] Topology Type Error.");
					break;
				}			

			}		

			System.out.println("[DBAdapter] Plan Added.");

			if (insertPlan.next())   return true;
			// Try End.
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

	public boolean addDestination(Destination destination) {

		Connection con = null;
		Statement stmt = null;

		try {
			con = this.getConnection();
			stmt = con.createStatement();

			// user 테이블에서 [이름]으로 [IDX]가져오기
			String owner = destination.getOwner();
			String ownerQuery = "SELECT IDX FROM tbl_user WHERE ID = '" + owner + "'";
			ResultSet ownerIdx = stmt.executeQuery(ownerQuery);
			ownerIdx.next();
			int ownerNumber = ((Number) ownerIdx.getObject(1)).intValue();

			// INSERT destination
			String insertDestination;
			insertDestination = "INSERT INTO tbl_dst " 
					+ "VALUES ("
					+ "'0',"
					+ "'" + destination.getDestinationName() + "',"
					+ "'" + destination.getCreatedTime() + "',"
					+ "'" + destination.getModifiedTime() + "',"
					+ "'" + destination.getStatus() + "',"
					+ "'" + ownerNumber + "',"
					+ "'" + destination.getDestinationType() + "',"
					+ "'" + destination.getTransTopic() + "'"					
					+ ")";			

			ResultSet insert = stmt.executeQuery(insertDestination);			

			// Source에서 Index 가져오기 [이름으로]
			String last = "SELECT IDX FROM tbl_dst WHERE NAME = " + "'" + destination.getDestinationName() +"'";
			ResultSet idx = stmt.executeQuery(last);
			idx.next();
			int destinationNumber = ((Number) idx.getObject(1)).intValue();

			switch(destination.getDestinationType()) {

			case "KAFKA":				
				KafkaDestination kd = (KafkaDestination) destination;
				String insertKafka = "INSERT INTO tbl_dst_kafka_info "
						+ "VALUES ("
						+ "'0',"
						+ "'" + kd.getZookeeperIp() + "',"
						+ "'" + kd.getZookeeperPort() + "',"
						+ "'" + kd.getTopic() + "',"
						+ "'" + destinationNumber + "'" // 마지막 idx 값을 외래키로
						+ ")";
				ResultSet kafka = stmt.executeQuery(insertKafka);
				break;

			case "database":
				break;

			default:
				System.out.println("[DBAdapter] Source Type Error.");
				break;			
			}

			System.out.println("[DBAdapter] Destination Added.");
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

	public boolean changePlanStatus(Plan plan) {
		
		Connection con = null;
		Statement stmt = null;		
		
		try {
			
			con = this.getConnection();
			stmt = con.createStatement();

			String status = plan.getStatus();
			
			String sql;			
			sql = "UPDATE tbl_plan SET STATUS ='" + status + "' WHERE NAME ='" + plan.getPlanName() + "'";
			
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
	
	public boolean changeSourceStatus(Source source) {
		
		Connection con = null;
		Statement stmt = null;		
		
		try {
			
			con = this.getConnection();
			stmt = con.createStatement();

			String status = source.getStatus();
			
			String sql;			
			sql = "UPDATE tbl_src SET STATUS ='" + status + "' WHERE NAME ='" + source.getSourceName() + "'";
			
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
}




