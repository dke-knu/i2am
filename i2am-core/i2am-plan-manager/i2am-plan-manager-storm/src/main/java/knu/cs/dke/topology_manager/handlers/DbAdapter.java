package knu.cs.dke.topology_manager.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.storm.thrift.transport.TTransportException;

import i2am.metadata.DbAdmin;
import i2am.query.parser.Node;
import knu.cs.dke.topology_manager.DestinationList;
import knu.cs.dke.topology_manager.Plan;
import knu.cs.dke.topology_manager.PlanList;
import knu.cs.dke.topology_manager.SourceList;
import knu.cs.dke.topology_manager.destinations.CustomDestination;
import knu.cs.dke.topology_manager.destinations.DBDestination;
import knu.cs.dke.topology_manager.destinations.Destination;
import knu.cs.dke.topology_manager.destinations.KafkaDestination;
import knu.cs.dke.topology_manager.sources.CustomSource;
import knu.cs.dke.topology_manager.sources.DBSource;
import knu.cs.dke.topology_manager.sources.KafkaSource;
import knu.cs.dke.topology_manager.sources.Source;
import knu.cs.dke.topology_manager.sources.SourceSchema;
import knu.cs.dke.topology_manager.sources.TestData;
import knu.cs.dke.topology_manager.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.BinaryBernoulliSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.BloomFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.HashSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.IKalmanFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.KSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.KalmanFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.NRKalmanFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.PrioritySamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.QueryFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.ReservoirSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.SystematicSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.UCKSamplingTopology;

public class DbAdapter {

	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();
	private static final Log logger = LogFactory.getLog(klass);

	// singleton
	private volatile static DbAdapter instance;

	public static DbAdapter getInstance() {
		if (instance == null) {
			synchronized (DbAdapter.class) {
				if (instance == null) {
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

	/*
	 * DbAdapter.getInstance().login(id, pw);
	 */
	public boolean login(String id, String pw) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			sql = "SELECT * FROM TBL_USER " + "WHERE ID='" + id + "' AND PASSWORD='" + pw + "'";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;
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
		return false;
	}

	public boolean addLog(String id, String type, String message) {

		Connection con = null;
		Statement stmt = null;
		String sql = null;
		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			sql = "INSERT INTO tbl_log (f_user, logging_type, logging_message) VALUES ("
					+ "(SELECT idx FROM tbl_user WHERE id ='" + id + "'), '" + type + "' , '" + message + "')";
			
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;
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

		return false;
	}

	public boolean addSource(Source source) {

		Connection con = null;
		Statement stmt = null;

		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			// user 테이블에서 [이름(메일)]으로 [IDX]가져오기
			String owner = source.getOwner();
			String ownerQuery = "SELECT IDX FROM tbl_user WHERE ID = '" + owner + "'";
			ResultSet ownerIdx = stmt.executeQuery(ownerQuery);
			ownerIdx.next();
			int ownerNumber = ((Number) ownerIdx.getObject(1)).intValue();

			// test data 테이블에서 [파일 이름]으로 [IDX]가져오기
			String testData = source.getTestData();

			int fileNumber = 0;

			if (testData != null) {
				String file = source.getTestData();
				String fileQuery = "SELECT IDX FROM tbl_src_test_data WHERE NAME = '" + file + "'";
				ResultSet fileIdx = stmt.executeQuery(fileQuery);
				fileIdx.next();
				fileNumber = ((Number) fileIdx.getObject(1)).intValue();
			}

			// INSERT source
			String insertSource;
			insertSource = "INSERT INTO tbl_src " + "VALUES (" + "'0'," + "'" + source.getSourceName() + "'," + "'"
					+ source.getCreatedTime() + "'," + "'" + source.getModifiedTime() + "'," + "'" + source.getStatus()
					+ "'," + "'" + ownerNumber + "'," + "'" + source.getUseIntelliEngine() + "'," + "'"
					+ source.getUseLoadShedding() + "',";

			if (testData != null) {
				insertSource = insertSource + "'" + fileNumber + "',";
			} else
				insertSource = insertSource + "null" + ",";

			insertSource = insertSource + "'" + source.getSrcType() + "'," + "'" + source.getSwitchMessaging() + "',"
					+ "'" + source.getTransTopic() + "'," + "'" + source.getUseConceptDrift() + "'" + ")";

			ResultSet insert = stmt.executeQuery(insertSource);

			// Source에서 Index 가져오기 [name] + [owner]로!
			String last = "SELECT IDX FROM tbl_src WHERE NAME = " + "'" + source.getSourceName() + "'" + "AND F_OWNER ="
					+ ownerNumber;
			ResultSet idx = stmt.executeQuery(last);
			idx.next();
			int sourceNumber = ((Number) idx.getObject(1)).intValue();

			switch (source.getSrcType()) {

			case "KAFKA":
				KafkaSource ks = (KafkaSource) source;
				String insertKafka = "INSERT INTO tbl_src_kafka_info " + "VALUES (" + "'0'," + "'" + ks.getZookeeperIp()
						+ "'," + "'" + ks.getZookeeperPort() + "'," + "'" + ks.getTopic() + "'," + "'" + sourceNumber
						+ "'" // 마지막 idx 값을 외래키로
						+ ")";
				ResultSet kafka = stmt.executeQuery(insertKafka);
				break;

			case "DATABASE":
				DBSource bs = (DBSource) source;
				String insertDB = "INSERT INTO tbl_src_database_info " + "VALUES (" + "'0'," + "'" + bs.getIp() + "',"
						+ "'" + bs.getPort() + "'," + "'" + bs.getUserId() + "'," + "'" + bs.getUserPassword() + "',"
						+ "'" + bs.getDbName() + "'," + "'" + bs.getQuery() + "'," + "'" + sourceNumber + "'"
						+ ")";

				ResultSet db = stmt.executeQuery(insertDB);
				break;

			case "CUSTOM":
				break;

			default:
				System.out.println("[DBAdapter] Source Type Error.");
				break;
			}

			// src_csv_schema 추가 + target 정보 가져오기
			ArrayList<SourceSchema> data = source.getData();

			for (int i = 0; i < data.size(); i++) {

				String insertScheme = "INSERT INTO tbl_src_csv_schema " + "VALUES (" + "'0'," + "'"
						+ data.get(i).getColumnIndex() + "'," + "'" + data.get(i).getColumnName() + "'," + "'"
						+ data.get(i).getColumnType() + "'," + "'" + sourceNumber + "'" + ")";

				ResultSet schema = stmt.executeQuery(insertScheme);
			}

			// tbl_intelligent_engine 추가
			// user 테이블에서 [이름]으로 [IDX]가져오기
			if (source.getUseIntelliEngine().equals("Y")) {

				System.out.println("?");

				String target = source.getTarget();
				String targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE COLUMN_NAME = '" + target + "' AND F_SRC='" + sourceNumber + "'";
				
				ResultSet targetIdx = stmt.executeQuery(targetQuery);
				targetIdx.next();
				int targetNumber = targetIdx.getInt("IDX");

				String intelli_params = "INSERT INTO tbl_intelligent_engine (F_SRC, F_TARGET)" + "VALUES ("						 
						+ "'" + sourceNumber + "',"
						+ "'" + targetNumber + "')";						

				ResultSet intelliResult = stmt.executeQuery(intelli_params);
			}

			System.out.println("[DBAdapter] Source Added.");

			if (insert.next())
				return true;

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
		return false;
	}

	public boolean addPlan(Plan plan) {

		Connection con = null;
		Statement stmt = null;

		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			// Get User.
			String owner = plan.getOwner();
			String ownerQuery = "SELECT IDX FROM tbl_user WHERE ID = '" + owner + "'";
			ResultSet ownerIdx = stmt.executeQuery(ownerQuery);
			ownerIdx.next();
			int ownerNumber = ((Number) ownerIdx.getObject(1)).intValue();

			// Get Source.
			String source = plan.getSource();
			String sourceQuery = "SELECT IDX FROM tbl_src WHERE NAME = '" + source + "'" + " AND F_OWNER ='"
					+ ownerNumber + "'";
			ResultSet sourceIdx = stmt.executeQuery(sourceQuery);
			sourceIdx.next();
			int sourceNumber = ((Number) sourceIdx.getObject(1)).intValue();

			// Get Destination.
			String destination = plan.getDestination();
			String destinationQuery = "SELECT IDX FROM tbl_dst WHERE NAME = '" + destination + "'" + " AND F_OWNER ='"
					+ ownerNumber + "'";
			;
			ResultSet destinationIdx = stmt.executeQuery(destinationQuery);
			destinationIdx.next();
			int destinationNumber = ((Number) destinationIdx.getObject(1)).intValue();

			// Plan
			String planQuery = "INSERT INTO tbl_plan " + "VALUES (" + "'0'," + "'" + plan.getPlanName() + "'," + "'"
					+ plan.getCreatedTime() + "'," + "'" + plan.getModifiedTime() + "'," + "'" + plan.getStatus() + "',"
					+ "'" + ownerNumber + "'," + "'" + sourceNumber + "'," + "'" + destinationNumber + "'" + ")";
			ResultSet insertPlan = stmt.executeQuery(planQuery);

			System.out.println("[DBAdapter] 플랜 입력 됨");

			// Topology!
			List<ASamplingFilteringTopology> topologies = plan.getTopologies();

			// Get Plan Idx.
			String planName = plan.getPlanName();
			String planNameQuery = "SELECT IDX FROM tbl_plan WHERE NAME = '" + planName + "'";
			ResultSet planIdx = stmt.executeQuery(planNameQuery);
			planIdx.next();
			int planNumber = ((Number) planIdx.getObject(1)).intValue();

			System.out.println("[DBAdapter] Topology to DB !");

			for (int i = 0; i < topologies.size(); i++) {
				
				// 토폴로지 단 하나!
				ASamplingFilteringTopology topology = topologies.get(i);

				// Topology Insert!
				String topologyQuery = "INSERT INTO tbl_topology " + "VALUES (" + "'0'," + "'"
						+ topology.getCreatedTime() + "'," + "'" + topology.getModifiedTime() + "'," + "'"
						+ topology.getStatus() + "'," + "'" + topology.getIndex() + "'," + "'"
						+ topology.getTopologyType() + "'," + "'" + planNumber + "'," + "'" + topology.getInputTopic()
						+ "'," + "'" + topology.getOutputTopic() + "'," + "'" + topology.getTopologyName() + "'," + "'"
						+ topology.getRedisKey() + "'" + ")";

				ResultSet insertTopology = stmt.executeQuery(topologyQuery);

				// 이제 Type에 맞춰 Parameter Table에 값을 넣으세요!
				// 자신에 해당하는 Topology를 찾으려면
				// Plan & Topology Index 일치
				String getTopologyQuery = "SELECT IDX FROM tbl_topology " + "WHERE F_PLAN = '" + planNumber + "'"
						+ "AND TOPOLOGY_INDEX = '" + i + "'";
				ResultSet topologyIdx = stmt.executeQuery(getTopologyQuery);
				topologyIdx.next();
				int topologyNumber = ((Number) topologyIdx.getObject(1)).intValue();

				// Target
				String targetQuery = "";
				ResultSet targetIdx;
				int targetNumber = -1;

				switch (topology.getTopologyType()) {

				case "BINARY_BERNOULLI_SAMPLING":

					BinaryBernoulliSamplingTopology bbs = (BinaryBernoulliSamplingTopology) topology;
					// Topology Insert!
					String bb_paramsQuery = "INSERT INTO tbl_params_binary_bernoulli_sampling " + "VALUES (" + "'0',"
							+ "'" + topologyNumber + "'," + "'" + bbs.getSampleSize() + "'," + "'" + bbs.getWindowSize()
							+ "'," + "'" + bbs.getPreSampleKey() + "'" + ")";
					ResultSet bb_paramsResult = stmt.executeQuery(bb_paramsQuery);
					break;

				case "HASH_SAMPLING":

					HashSamplingTopology hss = (HashSamplingTopology) topology;

					targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE F_SRC =" + sourceNumber
							+ " AND COLUMN_INDEX = " + hss.getTarget();
					targetIdx = stmt.executeQuery(targetQuery);
					targetIdx.next();
					targetNumber = ((Number) targetIdx.getObject(1)).intValue();

					String hash_Query = "INSERT INTO tbl_params_hash_sampling " + "VALUES (" 
							+ "'0'," + "'"
							+ topologyNumber + "'," + "'"
							+ targetNumber + "'," + "'" 
							+ hss.getSampleSize() + "'," + "'"
							+ hss.getWindowSize() + "'," + "'"
							+ hss.getHashFunction() + "')";
					
					ResultSet hash_paramsResult = stmt.executeQuery(hash_Query);
					break;

				case "I_KALMAN_FILTERING":

					IKalmanFilteringTopology ikft = (IKalmanFilteringTopology) topology;

					targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE F_SRC =" + sourceNumber
							+ " AND COLUMN_INDEX = " + ikft.getTarget();
					targetIdx = stmt.executeQuery(targetQuery);
					targetIdx.next();
					targetNumber = ((Number) targetIdx.getObject(1)).intValue();

					String i_kalman_query = "INSERT INTO tbl_params_intelligent_kalman_filtering "
							+ "VALUES (" + "'0'," 							
							+ "'" + topologyNumber + "',"
							+ "'" + targetNumber + "',"
							+ "'" + ikft.getA_val() + "',"
							+ "'" + ikft.getQ_val() + "',"
							+ "'" + ikft.getH_val() + "',"
							+ "'" + ikft.getX_val() + "',"
							+ "'" + ikft.getP_val() + "',"
							+ "'" + ikft.getR_val() + "')";
					ResultSet ikalman_paramsResult = stmt.executeQuery(i_kalman_query);
					break;

				case "KALMAN_FILTERING":

					KalmanFilteringTopology kft = (KalmanFilteringTopology) topology;

					targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE F_SRC =" + sourceNumber
							+ " AND COLUMN_INDEX = " + kft.getTarget();
					targetIdx = stmt.executeQuery(targetQuery);
					targetIdx.next();
					targetNumber = ((Number) targetIdx.getObject(1)).intValue();

					String kalman_query = "INSERT INTO tbl_params_kalman_filtering " + "VALUES (" + "'0'," + "'"
							+ topologyNumber + "'," + "'" + targetNumber + "'," + "'" + kft.getA_val() + "'," + "'"
							+ kft.getQ_val() + "'," + "'" + kft.getH_val() + "'," + "'" + kft.getX_val() + "'," + "'"
							+ kft.getP_val() + "'," + "'" + kft.getR_val() + "'" + ")";
					ResultSet kalman_paramsResult = stmt.executeQuery(kalman_query);
					break;

				case "K_SAMPLING":

					KSamplingTopology kst = (KSamplingTopology) topology;
					String k_query = "INSERT INTO tbl_params_k_sampling " + "VALUES (" + "'0'," + "'" + topologyNumber
							+ "'," + "'" + kst.getSamplingRate() + "'" + ")";
					ResultSet k_paramsResult = stmt.executeQuery(k_query);
					break;

				case "SYSTEMATIC_SAMPLING":

					SystematicSamplingTopology sft = (SystematicSamplingTopology) topology;

					String systematic_Query = "INSERT INTO tbl_params_systematic_sampling " + "VALUES (" + "'0'," + "'"
							+ topologyNumber + "'," + "'" + sft.getInterval() + "'" + ")";
					ResultSet systematic_paramsResult = stmt.executeQuery(systematic_Query);
					break;

				case "QUERY_FILTERING":

					QueryFilteringTopology qft = (QueryFilteringTopology) topology;

					String query_Query = "INSERT INTO tbl_params_query_filtering " + "VALUES (" + "'0'," + "'"
							+ topologyNumber + "'" + ")";
					ResultSet query_paramsResult = stmt.executeQuery(query_Query);

					// public void addQuery (Node node, String id, String srcName, String
					// topologyName)
					Node node = Node.parse(qft.getQuery());
					i2am.query.parser.DbAdapter.getInstance().addQuery(node, owner, source, qft.getTopologyName());

					break;

				case "PRIORITY_SAMPLING":

					PrioritySamplingTopology pst = (PrioritySamplingTopology) topology;

					targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE F_SRC =" + sourceNumber
							+ " AND COLUMN_INDEX = " + pst.getTarget();
					targetIdx = stmt.executeQuery(targetQuery);
					targetIdx.next();
					targetNumber = ((Number) targetIdx.getObject(1)).intValue();

					String priority_Query = "INSERT INTO tbl_params_priority_sampling " + "VALUES (" + "'0'," + "'"
							+ topologyNumber + "'," + "'" + targetNumber + "'," + "'" + pst.getSampleSize() + "'," + "'"
							+ pst.getWindowSize() + "'" + ")";
					ResultSet priority_paramsResult = stmt.executeQuery(priority_Query);
					break;

				case "RESERVOIR_SAMPLING":

					ReservoirSamplingTopology rvs = (ReservoirSamplingTopology) topology;

					String reservoir_Query = "INSERT INTO tbl_params_reservoir_sampling " + "VALUES (" + "'0'," + "'"
							+ topologyNumber + "'," + "'" + rvs.getSampleSize() + "'," + "'" + rvs.getWindowSize() + "'"
							+ ")";
					ResultSet reservoir_paramsResult = stmt.executeQuery(reservoir_Query);
					break;

				case "BLOOM_FILTERING":

					BloomFilteringTopology blf = (BloomFilteringTopology) topology;

					targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE F_SRC =" + sourceNumber
							+ " AND COLUMN_INDEX = " + blf.getTarget();
					targetIdx = stmt.executeQuery(targetQuery);
					targetIdx.next();
					targetNumber = ((Number) targetIdx.getObject(1)).intValue();

					String bloom_Query = "INSERT INTO tbl_params_bloom_filtering " + "VALUES (" 
							+ "'0'," 
							+ "'" + topologyNumber + "',"
							+ "'" + targetNumber + "'," 
							+ "'" + blf.getBucketSize() + "',"
							+ "'" + blf.getKeywords() + "',"
							+ "'" + blf.getHashFunction1() + "',"
							+ "'" + blf.getHashFunction2() + "',"
							+ "'" + blf.getHashFunction3() + "'"
							+ ")";
					
					ResultSet bloom_paramsResult = stmt.executeQuery(bloom_Query);
					break;

				case "NR_KALMAN_FILTERING":

					NRKalmanFilteringTopology nrkf = (NRKalmanFilteringTopology) topology;

					targetQuery = "SELECT IDX FROM tbl_src_csv_schema WHERE F_SRC =" + sourceNumber
							+ " AND COLUMN_INDEX = " + nrkf.getTarget();
					targetIdx = stmt.executeQuery(targetQuery);
					targetIdx.next();
					targetNumber = ((Number) targetIdx.getObject(1)).intValue();

					String nr_kalman_query = "INSERT INTO tbl_params_noise_recommend_kalman_filtering "
							+ "VALUES ("
							+ "'0',"
							+ "'" + topologyNumber + "'," 
							+ "'" + targetNumber + "',"
							+ "'" + nrkf.getA_val() + "',"
							+ "'" + nrkf.getQ_val() + "',"
							+ "'" + nrkf.getH_val() + "',"
							+ "'" + nrkf.getX_val()	+ "',"
							+ "'" + nrkf.getP_val() + "',"
							+ "'" + nrkf.getMeasure() + "'" + ")";
					ResultSet nr_kalman_paramsResult = stmt.executeQuery(nr_kalman_query);
					break;

				case "UC_K_SAMPLING":

					UCKSamplingTopology uckst = (UCKSamplingTopology) topology;

					String uc_k_query = "INSERT INTO tbl_params_uc_k_sampling " + "VALUES (" + "'0'," + "'"
							+ topologyNumber + "'," + "'" + uckst.getSamplingRate() + "'," + "'"
							+ uckst.getUcUnderBound() + "'" + ")";
					ResultSet uc_k_paramsResult = stmt.executeQuery(uc_k_query);
					break;

				default:
					System.out.println("[DBAdapter] Topology Type Error.");
					break;
				}

			}

			System.out.println("[DBAdapter] Plan Added.");

			if (insertPlan.next())
				return true;
			// Try End.
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
		return false;
	}

	public boolean addDestination(Destination destination) {

		Connection con = null;
		Statement stmt = null;

		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			// user 테이블에서 [이름]으로 [IDX]가져오기
			String owner = destination.getOwner();
			String ownerQuery = "SELECT IDX FROM tbl_user WHERE ID = '" + owner + "'";
			ResultSet ownerIdx = stmt.executeQuery(ownerQuery);
			ownerIdx.next();
			int ownerNumber = ((Number) ownerIdx.getObject(1)).intValue();

			// INSERT destination
			String insertDestination;
			insertDestination = "INSERT INTO tbl_dst " + "VALUES (" + "'0'," + "'" + destination.getDestinationName()
					+ "'," + "'" + destination.getCreatedTime() + "'," + "'" + destination.getModifiedTime() + "',"
					+ "'" + destination.getStatus() + "'," + "'" + ownerNumber + "'," + "'"
					+ destination.getDestinationType() + "'," + "'" + destination.getTransTopic() + "'" + ")";

			ResultSet insert = stmt.executeQuery(insertDestination);

			// Destination에서 Index 가져오기 [이름으로]
			String last = "SELECT IDX FROM tbl_dst WHERE NAME = " + "'" + destination.getDestinationName() + "'";
			ResultSet idx = stmt.executeQuery(last);
			idx.next();
			int destinationNumber = ((Number) idx.getObject(1)).intValue();

			switch (destination.getDestinationType()) {

			case "KAFKA":
				KafkaDestination kd = (KafkaDestination) destination;
				String insertKafka = "INSERT INTO tbl_dst_kafka_info " + "VALUES (" + "'0'," + "'" + kd.getZookeeperIp()
						+ "'," + "'" + kd.getZookeeperPort() + "'," + "'" + kd.getTopic() + "'," + "'"
						+ destinationNumber + "'" // 마지막 idx 값을 외래키로
						+ ")";
				ResultSet kafka = stmt.executeQuery(insertKafka);
				break;

			case "DATABASE":
				DBDestination dd = (DBDestination) destination;
				String insertDB = "INSERT INTO tbl_dst_database_info " + "VALUES (" + "'0'," + "'" + dd.getIp() + "',"
						+ "'" + dd.getPort() + "'," + "'" + dd.getUserId() + "'," + "'" + dd.getUserPassword() + "',"
						+ "'" + dd.getDbName() + "'," + "'" + dd.getTableName() + "'," + "'" + destinationNumber + "'" // 마지막
																														// idx
																														// 값을
																														// 외래키로
						+ ")";
				ResultSet db = stmt.executeQuery(insertDB);
				break;

			case "CUSTOM":
				break;

			default:
				System.out.println("[DBAdapter] Source Type Error.");
				break;
			}

			System.out.println("[DBAdapter] Destination Added.");
			if (insert.next())
				return true;

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
		return false;
	}

	public boolean changePlanStatus(Plan plan) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String status = plan.getStatus();

			// 플랜 상태
			String sql;
			sql = "UPDATE tbl_plan SET STATUS ='" + status + "' WHERE NAME ='" + plan.getPlanName() + "'";

			// 플랜에 포함된 토폴로지 상태
			String topology_status = "UPDATE tbl_topology SET STATUS = '" + status
					+ "' where F_PLAN = (select idx from tbl_plan where name ='" + plan.getPlanName() + "')";

			ResultSet rs = stmt.executeQuery(sql);
			ResultSet topology_rs = stmt.executeQuery(topology_status);

			if (rs.next() && topology_rs.next())
				return true;
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
		return false;
	}

	public boolean changeSourceStatus(Source source) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String status = source.getStatus();

			String sql;
			sql = "UPDATE tbl_src SET STATUS ='" + status + "' WHERE NAME ='" + source.getSourceName() + "'";

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;
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

		return false;
	}

	public boolean changeDestinationStatus(Destination destination) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String status = destination.getStatus();

			String sql;
			sql = "UPDATE tbl_dst SET STATUS ='" + status + "' WHERE NAME ='" + destination.getDestinationName() + "'";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;

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

		return false;
	}

	// To-Do
	public boolean removeSource(Source source) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String sql;
			sql = "DELETE FROM tbl_src WHERE name = '" + source.getSourceName() + "' AND F_OWNER = "
					+ "(SELECT idx FROM tbl_user WHERE id = '" + source.getOwner() + "')";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;

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

		return false;
	}

	public boolean removePlan(Plan plan) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String sql;
			sql = "DELETE FROM tbl_plan WHERE name = '" + plan.getPlanName() + "' AND F_OWNER = "
					+ "(SELECT idx FROM tbl_user WHERE id = '" + plan.getOwner() + "')";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;

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

		return false;
	}

	public boolean removeDestination(Destination destination) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String sql;
			sql = "DELETE FROM tbl_dst WHERE name = '" + destination.getDestinationName() + "' AND F_OWNER = "
					+ "(SELECT idx FROM tbl_user WHERE id = '" + destination.getOwner() + "')";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())
				return true;

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

		return false;
	}

	// Edit Source

	// Edit Plan

	// Edit Destination

	// Update Recommendation
	public boolean updateRecommendationn(Source source) {
		return true;
	}

	public boolean loadSources(SourceList sources) {

		Connection con = null;
		Statement stmt = null;

		// Sources
		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			String query = "select * from tbl_src s, tbl_user u where s.F_OWNER = u.IDX";
			ResultSet rs = stmt.executeQuery(query);

			Source source = null;

			while (rs.next()) { // 소스를 가지고 진행!

				String type = "";
				ResultSet type_rs = null;

				// 1. 타입에 따라 파라미터를 가져온당.
				switch (rs.getString("SRC_TYPE")) {

				case "DATABASE":
					type = "select * from tbl_src s, tbl_src_database_info d, tbl_user u where s.IDX = d.F_SRC AND s.F_OWNER = u.IDX AND d.F_SRC = '"
							+ rs.getInt("s.IDX") + "'";
					type_rs = stmt.executeQuery(type);
					type_rs.next();

					source = new DBSource(rs.getString("s.NAME"), rs.getString("CREATED_TIME"), rs.getString("u.ID"),
							rs.getString("SRC_TYPE"), null, rs.getString("CONCEPT_DRIFT_STATUS"),
							rs.getString("USES_LOAD_SHEDDING"), rs.getString("IS_RECOMMENDATION"), "testData", "target",
							type_rs.getString("IP"), type_rs.getString("PORT"), type_rs.getString("ID"),
							type_rs.getString("PASSWORD"), type_rs.getString("DB_NAME"), type_rs.getString("QUERY"));

					break;

				case "KAFKA":
					type = "select * from tbl_src s, tbl_src_kafka_info d, tbl_user u where s.IDX = d.F_SRC AND s.F_OWNER = u.IDX AND d.F_SRC = '"
							+ rs.getInt("s.IDX") + "'";
					type_rs = stmt.executeQuery(type);
					type_rs.next();

					source = new KafkaSource(rs.getString("s.NAME"), rs.getString("CREATED_TIME"), rs.getString("u.ID"),
							rs.getString("SRC_TYPE"), null, rs.getString("CONCEPT_DRIFT_STATUS"),
							rs.getString("USES_LOAD_SHEDDING"), rs.getString("IS_RECOMMENDATION"), "testData", "target",
							type_rs.getString("ZOOKEEPER_IP"), type_rs.getString("PORT"), type_rs.getString("TOPIC"));

					break;

				case "CUSTOM":
					source = new CustomSource(rs.getString("s.NAME"), rs.getString("CREATED_TIME"),
							rs.getString("u.ID"), rs.getString("SRC_TYPE"), null, rs.getString("CONCEPT_DRIFT_STATUS"),
							rs.getString("USES_LOAD_SHEDDING"), rs.getString("IS_RECOMMENDATION"), "testData",
							"target");
					break;

				default:
					System.out.println("[DBAdapter] 소스를 가져오는 중 타입 에러 발생");
					return false;

				}

				source.setModifiedTime(rs.getString("MODIFIED_TIME"));
				source.setModifiedTime(rs.getString("STATUS"));
				source.setTransTopic(rs.getString("TRANS_TOPIC"));

				ArrayList<SourceSchema> schema = new ArrayList<SourceSchema>();
				String schema_query = "select * from tbl_src s, tbl_src_csv_schema c, tbl_user u where s.NAME ='"
						+ rs.getString("s.NAME") + "' AND s.IDX = c.F_SRC AND u.ID = '" + rs.getString("u.ID") + "'";
				ResultSet schema_rs = stmt.executeQuery(schema_query);

				while (schema_rs.next()) {

					SourceSchema column = new SourceSchema(schema_rs.getInt("COLUMN_INDEX"),
							schema_rs.getString("COLUMN_NAME"), schema_rs.getString("COLUMN_TYPE"));
					schema.add(column);
				}
				source.setData(schema);
				sources.add(source);
			}

			if (rs.next())
				return true;

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

		return false;
	}

	public boolean loadDestinations(DestinationList destinations) {

		Connection con = null;
		Statement stmt = null;

		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			// Database
			String query = "select * from tbl_dst d, tbl_dst_database_info i, tbl_user u where d.IDX = i.F_DST AND d.F_OWNER = u.IDX";
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {

				DBDestination temp = new DBDestination(rs.getString("d.NAME"), rs.getString("d.CREATED_TIME"),
						rs.getString("u.ID"), rs.getString("d.DST_TYPE"), rs.getString("i.IP"), rs.getString("i.PORT"),
						rs.getString("i.ID"), rs.getString("i.PASSWORD"), rs.getString("i.DB_NAME"),
						rs.getString("i.TABLE_NAME"));

				temp.setModifiedTime(rs.getString("d.MODIFIED_TIME"));
				temp.setModifiedTime(rs.getString("d.STATUS"));
				temp.setTransTopic(rs.getString("d.TRANS_TOPIC"));
				
				destinations.add(temp);
			}

			// Kafka
			query = "select * from tbl_dst d, tbl_dst_kafka_info i, tbl_user u where d.IDX = i.F_DST AND d.F_OWNER = u.IDX";
			rs = stmt.executeQuery(query);

			while (rs.next()) {

				KafkaDestination temp = new KafkaDestination(rs.getString("d.NAME"), rs.getString("d.CREATED_TIME"),
						rs.getString("u.ID"), rs.getString("d.DST_TYPE"), rs.getString("i.ZOOKEEPER_IP"),
						rs.getString("i.PORT"), rs.getString("i.TOPIC"));

				temp.setModifiedTime(rs.getString("d.MODIFIED_TIME"));
				temp.setModifiedTime(rs.getString("d.STATUS"));
				temp.setTransTopic(rs.getString("d.TRANS_TOPIC"));

				destinations.add(temp);
			}

			// Custom
			query = "select * from tbl_dst d, tbl_user u where d.DST_TYPE = 'CUSTOM' AND d.F_OWNER = u.IDX";
			rs = stmt.executeQuery(query);

			while (rs.next()) {

				CustomDestination temp = new CustomDestination(rs.getString("d.NAME"), rs.getString("d.CREATED_TIME"),
						rs.getString("u.ID"), rs.getString("d.DST_TYPE"));

				temp.setModifiedTime(rs.getString("d.MODIFIED_TIME"));
				temp.setModifiedTime(rs.getString("d.STATUS"));
				temp.setTransTopic(rs.getString("d.TRANS_TOPIC"));

				destinations.add(temp);
			}

			if (rs.next())
				return true;

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

		return false;
	}

	public boolean loadPlans(PlanList plans) throws TTransportException {

		Connection con = null;
		Statement stmt = null;

		// Sources
		try {

			con = ds.getConnection();
			stmt = con.createStatement();

			// Plan
			String plan_query = "select * from tbl_plan p, tbl_src s, tbl_dst d, tbl_user u"
					+ " where p.F_SRC = s.IDX and p.F_DST = d.IDX and p.F_OWNER = u.IDX";

			ResultSet rs = stmt.executeQuery(plan_query);

			while (rs.next()) {

				String owner = rs.getString("u.ID");
				String planName = rs.getString("p.NAME");
				String created_time = rs.getString("CREATED_TIME");
				String modified_time = rs.getString("MODIFIED_TIME");
				String status = rs.getString("STATUS");
				String srcName = rs.getString("s.NAME");
				String dstName = rs.getString("d.NAME");

				Plan temp = new Plan(planName, created_time, status, owner, srcName, dstName);
				temp.setModifiedTime(modified_time);

				String topology_query = "select * from tbl_topology where F_PLAN = '" + rs.getInt("p.IDX")
						+ "' ORDER BY TOPOLOGY_INDEX";
				ResultSet topology_rs = stmt.executeQuery(topology_query);

				List<ASamplingFilteringTopology> topologies = new ArrayList<ASamplingFilteringTopology>();
				ASamplingFilteringTopology temp_topology = null;
				String params_query = "";
				ResultSet params_rs;

				while (topology_rs.next()) {

					switch (topology_rs.getString("TOPOLOGY_TYPE")) {

					case "BINARY_BERNOULLI_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_binary_bernoulli_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new BinaryBernoulliSamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"),
								"BINARY_BERNOULLI_SAMPLING", params_rs.getInt("SAMPLE_SIZE"), params_rs.getInt("WINDOW_SIZE"));
						break;

					case "BLOOM_FILTERING":

						params_query = "select * from tbl_topology t, tbl_params_bloom_filtering p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new BloomFilteringTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "BLOOM_FILTERING",
								params_rs.getInt("BUCKET_SIZE"), params_rs.getString("KEYWORDS"),
								params_rs.getInt("F_TARGET"), params_rs.getString("HASH_FUNCTION1"), params_rs.getString("HASH_FUNCTION2"),
								params_rs.getString("HASH_FUNCTION1"));
						break;

					case "HASH_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_hash_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new HashSamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "HASH_SAMPLING",
								params_rs.getInt("SAMPLE_SIZE"), params_rs.getInt("WINDOW_SIZE"), params_rs.getString("HASH_FUNCTION"),
								params_rs.getInt("F_TARGET"));

						break;

					case "I_KALMAN_FILTERING":

						params_query = "select * from tbl_topology t, tbl_params_intelligent_kalman_filtering p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new IKalmanFilteringTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "I_KALMAN_FILTERING",
								params_rs.getDouble("A_VAL"), params_rs.getDouble("Q_VAL"),
								params_rs.getDouble("H_VAL"), params_rs.getDouble("X_VAL"),
								params_rs.getDouble("P_VAL"), params_rs.getInt("F_TARGET"));

						break;

					case "KALMAN_FILTERING":

						params_query = "select * from tbl_topology t, tbl_params_kalman_filtering p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new KalmanFilteringTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "KALMAN_FILTERING",
								params_rs.getDouble("A_VAL"), params_rs.getDouble("Q_VAL"),
								params_rs.getDouble("H_VAL"), params_rs.getDouble("X_VAL"),
								params_rs.getDouble("P_VAL"), params_rs.getDouble("R_VAL"),
								params_rs.getInt("F_TARGET"));

						break;

					case "K_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_k_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new KSamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "K_SAMPLING",
								params_rs.getInt("SAMPLING_RATE"));

						break;

					case "NR_KALMAN_FILTERING":

						params_query = "select * from tbl_topology t, tbl_params_noise_recommend_kalman_filtering p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new NRKalmanFilteringTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "NR_KALMAN_FILTERING",
								params_rs.getDouble("A_VAL"), params_rs.getDouble("Q_VAL"),
								params_rs.getDouble("H_VAL"), params_rs.getDouble("X_VAL"),
								params_rs.getDouble("P_VAL"), params_rs.getString("RECOMMENDED_MEASURE"),
								params_rs.getInt("F_TARGET"));

						break;

					case "PRIORITY_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_priority_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new PrioritySamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "PRIORITY_SAMPLING",
								params_rs.getInt("SAMPLE_SIZE"), params_rs.getInt("WINDOW_SIZE"),
								params_rs.getInt("F_TARGET"));

						break;

					case "QUERY_FILTERING":

						params_query = "select * from tbl_topology t, tbl_params_query_filtering p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX")
								+ "' and TOPOLOGY_TYPE ='QUERY_FILTERING'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						Node node = i2am.query.parser.DbAdapter.getInstance()
								.getQuery(params_rs.getString("TOPOLOGY_NAME"));
						System.out.println(node.toString());

						temp_topology = new QueryFilteringTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "QUERY_FILTERING",
								node.toString());

						break;

					case "RESERVOIR_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_reservoir_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new ReservoirSamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "RESERVOIR_SAMPLING",
								params_rs.getInt("SAMPLE_SIZE"), params_rs.getInt("WINDOW_SIZE"));

						break;

					case "SYSTEMATIC_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_systematic_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new SystematicSamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "SYSTEMATIC_SAMPLING",
								params_rs.getInt("INTERVAL"));

						break;

					case "UC_K_SAMPLING":

						params_query = "select * from tbl_topology t, tbl_params_uc_k_sampling p "
								+ "where t.IDX = p.F_TOPOLOGY and t.F_PLAN = '" + rs.getInt("p.IDX") + "'";

						params_rs = stmt.executeQuery(params_query);
						params_rs.next();

						temp_topology = new UCKSamplingTopology(topology_rs.getString("CREATED_TIME"),
								rs.getString("p.NAME"), topology_rs.getInt("TOPOLOGY_INDEX"), "RESERVOIR_SAMPLING",
								params_rs.getInt("SAMPLING_RATE"), params_rs.getDouble("UC_UNDER_BOUND"));

						break;

					default:
						System.out.println("[DbAdapter] 토폴로지 타입을 읽는데 실패하였음");
						break;
					}

					// 기타 정보 추가
					temp_topology.setInputTopic(topology_rs.getString("INPUT_TOPIC"));
					temp_topology.setOutputTopic(topology_rs.getString("OUTPUT_TOPIC"));
					temp_topology.setTopologyName(topology_rs.getString("TOPOLOGY_NAME"));
					temp_topology.setRedisKey(topology_rs.getString("REDIS_KEY"));

					// 리스트에 넣기
					topologies.add(temp_topology.getIndex(), temp_topology);
				}

				// 플랜에 토폴로지 셋 하기
				temp.setTopologies(topologies);

				// 플랜 입력
				plans.add(temp);
			}

			if (rs.next())
				return true;

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

		return false;
	}

}
