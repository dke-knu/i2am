package i2am.team1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBConnection {
	
	final String driver = "com.mysql.jdbc.Driver";
	final String url = "jdbc:mysql://" + ":3306/tagcloud"; // db url
	final String uId = ""; // db user 
	final String uPwd = ""; // db passwd
	
	Connection conn;
	PreparedStatement pstmt;
	ResultSet rs;
	
	DBConnection() {
		try{
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(url,uId,uPwd);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insertKeywordsAndSession(String message, String session) {
		String keyword[];
		String s_id = "";
		int result;
		keyword = message.split(",");
		int length = keyword.length;
		
		//insert session
		String sql = "INSERT INTO session_tb (session) VALUES ('" + session + "')";
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//get session id
		sql = "SELECT id FROM session_tb WHERE session = '" + session + "'";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				s_id = rs.getString("id");
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			String sql2, sql3;
			String k_id = "";
			int	k_usage = 0;
			for(int i=0;i<length;++i) {
				//keyword exist or not
				sql = "SELECT id, `usage` FROM keyword_tb WHERE keyword = '" + keyword[i] + "'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				
				if (!rs.next()) {  //empty rs == new keyword
					//insert keyword
					sql2 = "INSERT INTO keyword_tb (keyword) VALUES ('" + keyword[i] + "')";
					pstmt = conn.prepareStatement(sql2);
					result = pstmt.executeUpdate();
					//select k_id
					sql2 = "SELECT id FROM keyword_tb WHERE keyword = '" + keyword[i] + "'";
					pstmt = conn.prepareStatement(sql2);
					rs = pstmt.executeQuery();
					while(rs.next()){
						k_id = rs.getString("id");
					}
				} else { // exist keyword
					do {
						k_id = rs.getString("id");
						k_usage = Integer.parseInt(rs.getString("usage"));
						++k_usage;
						sql2 = "UPDATE keyword_tb SET `usage` = " + k_usage + " WHERE id = " + k_id;
						pstmt = conn.prepareStatement(sql2);
						result = pstmt.executeUpdate();
					  } while (rs.next());
				}
				
				sql3 = "INSERT INTO session_keyword_tb (s_id, k_id) VALUES(" + s_id + ", " + k_id + ")";
				pstmt = conn.prepareStatement(sql3);
				result = pstmt.executeUpdate();
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public void deleteSession(String session) {
		try {
			String sql;
			Statement stmt = conn.createStatement();
			
			sql = "UPDATE keyword_tb SET `usage` = `usage`-1 WHERE id IN ("
					+ "SELECT k_id FROM session_keyword_tb WHERE s_id IN ("
					+ "SELECT id FROM session_tb WHERE session = '" + session + "'))";
			stmt.addBatch(sql);
			
			sql = "DELETE FROM keyword_tb WHERE `usage` = 0";
			stmt.addBatch(sql);
			
			sql = "DELETE FROM session_tb WHERE session = '" + session + "'";
			stmt.addBatch(sql);
			
			stmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	public String[] selectSession(String kw) {
		
		String sql;
		String[] sessions = null;
		ArrayList<String> sessions_arr = new ArrayList<String>();
		ArrayList<String> k_id_arr = new ArrayList<String>();
		ArrayList<String> s_id_arr = new ArrayList<String>();
		
		try {
			sql = "SELECT id FROM keyword_tb WHERE keyword = '" + kw + "'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				k_id_arr.add(rs.getString("id"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			for(int i=0, length = k_id_arr.size();i<length;++i) {
				sql = "SELECT s_id FROM session_keyword_tb WHERE k_id = " + k_id_arr.get(i);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next()){
					s_id_arr.add(rs.getString("s_id"));
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		try {
			for(int i=0, length = s_id_arr.size();i<length;++i) {
				sql = "SELECT session FROM session_tb WHERE id = " + s_id_arr.get(i);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next()){
					sessions_arr.add(rs.getString("session"));
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		sessions = new String[sessions_arr.size()];
		sessions = sessions_arr.toArray(sessions);
		
		return sessions;
	}
	
	public ArrayList<String> selectAllSessions() {
		ArrayList<String> sessions_arr = new ArrayList<String>();
		String sql;
		try {
			sql = "SELECT session FROM session_tb";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				sessions_arr.add(rs.getString("session"));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sessions_arr;
	}
}
