package i2am.query.parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Stack;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import i2am.metadata.DbAdmin;

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

	protected void close(Connection con) throws SQLException {
		con.close();
	}
	
	public void addQuery (Node node, String id, String srcName, String topologyName) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		
		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			sql = "SELECT IDX FROM tbl_params_query_filtering "
					+ "WHERE F_TOPOLOGY = (SELECT IDX FROM tbl_topology WHERE "
						+ "TOPOLOGY_NAME = '" + topologyName + "')";	
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next())	
				addQuery(node, rs.getInt("IDX"), id, srcName);
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
	}
	
	private int addQuery (Node node, int queryFilteringIdx, String id, String srcName) {
		String insertQuery = null;
		String selectQuery = null;
		if ( node.getOperator().isLogicalOperator() ) {
			int leftIdx = addQuery(node.getLeft(), queryFilteringIdx, id, srcName);
			int rightIdx = addQuery(node.getRight(), queryFilteringIdx, id, srcName);
			insertQuery = "INSERT INTO tbl_query (F_LEFT_EXPR, OPERATOR, F_RIGHT_EXPR, F_QUERY_FILTERING) "
					+ "VALUES (" + leftIdx + ", '" + node.getOperator() + "', " + rightIdx + ", " + queryFilteringIdx + ")";
			selectQuery = "SELECT IDX FROM tbl_query "
					+ "WHERE F_LEFT_EXPR=" + leftIdx + " AND "
							+ "OPERATOR='" +node.getOperator() + "' AND "
							+ "F_RIGHT_EXPR=" + rightIdx + " AND "
							+ "F_QUERY_FILTERING=" + queryFilteringIdx;
		} else {
			insertQuery = "INSERT INTO tbl_query (F_LEFT_TARGET, OPERATOR, RIGHT_VALUE, F_QUERY_FILTERING) "
					+ "SELECT IDX, '" + node.getOperator() + "', '"
					+ node.getRight().getLeaf() + "', " + queryFilteringIdx  + " FROM tbl_src_csv_schema "
						+ "WHERE COLUMN_NAME = '" + node.getLeft().getLeaf() + "' AND "
							+ "F_SRC = (SELECT IDX FROM tbl_src "
								+ "WHERE NAME = '" + srcName + "' AND F_OWNER=(SELECT IDX FROM tbl_user "
									+ "WHERE ID = '" + id + "'))";
			selectQuery = "SELECT IDX FROM tbl_query "
					+ "WHERE F_LEFT_TARGET=(SELECT IDX FROM tbl_src_csv_schema "
						+ "WHERE COLUMN_NAME = '" + node.getLeft().getLeaf() + "' AND "
							+ "F_SRC = (SELECT IDX FROM tbl_src "
								+ "WHERE NAME = '" + srcName + "' AND F_OWNER=(SELECT IDX FROM tbl_user "
									+ "WHERE ID = '" + id + "'))) AND "
							+ "OPERATOR='" + node.getOperator() + "' AND "
							+ "RIGHT_VALUE='" + node.getRight().getLeaf() + "' AND "
							+ "F_QUERY_FILTERING=" + queryFilteringIdx;
		}
		
		Connection con = null;
		Statement stmt = null;
		
		try {
			con = ds.getConnection();
			stmt = con.createStatement();
			
			stmt.executeUpdate(insertQuery);

			ResultSet rs = stmt.executeQuery(selectQuery);
			if (rs.next())	return rs.getInt("IDX");
			
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					close(con);
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return -1;
	}
	
	public Node getQuery (String topologyName) {
		Connection con = null;
		Statement stmt = null;
		String sql = null;
		
		Node ret = null;
		
		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			sql = "SELECT q.IDX, q.F_LEFT_EXPR AS LEFT_TARGET, s.COLUMN_NAME, q.OPERATOR, q.F_RIGHT_EXPR, q.RIGHT_VALUE "
					+ "FROM tbl_query AS q LEFT JOIN tbl_src_csv_schema AS s "
						+ "ON q.F_LEFT_TARGET = s.IDX "
					+ "WHERE F_QUERY_FILTERING = (SELECT IDX "
						+ "FROM tbl_params_query_filtering WHERE F_TOPOLOGY = (SELECT IDX "
							+ "FROM tbl_topology WHERE TOPOLOGY_NAME = '" + topologyName + "'))";	

			ResultSet rs = stmt.executeQuery(sql);
			Stack<Node> stack = new Stack<Node>(); 
			while (rs.next()) {
				if (!Operator.isLogicalOperator(rs.getString("OPERATOR"))) {
					Node node = new Node(new Node(rs.getString("COLUMN_NAME")), 
									new Operator(rs.getString("OPERATOR")), 
									new Node(rs.getString("RIGHT_VALUE"))
							);
					stack.push(node);
				} else {
					Node right = stack.pop();
					Node left = stack.pop();
					Node node = new Node(left,
									new Operator(rs.getString("OPERATOR")),
									right);
					stack.push(node);
				}
			}
			
			ret = stack.pop();

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
		return ret;
	}
	
		Connection con = null;
		public String[] getSchema (String topologyName) {
		Statement stmt = null;
		String sql = null;
		
		String[] schema = null;
		
		try {
			con = ds.getConnection();
			stmt = con.createStatement();

			sql = "SELECT COLUMN_NAME FROM tbl_src_csv_schema "
					+ "WHERE F_SRC = (SELECT IDX FROM tbl_src "
					+ "WHERE IDX = (SELECT F_SRC FROM tbl_plan "
					+ "WHERE IDX = (SELECT F_PLAN FROM tbl_topology "
					+ "WHERE TOPOLOGY_NAME = '" + topologyName + "'))) ORDER BY COLUMN_INDEX DESC";
			ResultSet rs = stmt.executeQuery(sql);
			
			int rowcount = 0;
			if (rs.last()) {
			  rowcount = rs.getRow();
			  rs.beforeFirst();
			}
			schema = new String[rowcount];
			while(rs.next()) {
				schema[--rowcount] = rs.getString("COLUMN_NAME");
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
		return schema;
	}
}
