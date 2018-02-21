package knu.cs.dke.prog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import knu.cs.dke.vo.TrainingResult;

public class DBProcess {
	String url = "jdbc:mariadb://SERVER_IP:PORT/filtering_system";
	String id = "root";
	String pw = "cs2013";

	@SuppressWarnings("resource")
	public int saveLog(String lang, String keywords){
		Connection conn = null;
		Statement stmt = null;			
		ResultSet rs = null;
		int idx = 0;
		try{
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection(url,id,pw);

			stmt = conn.createStatement();
			//넣기 전 중복 확인
			String sql_duplication = "select * from bayesian_condition_log where lang='"+lang+"' and keywords='"+keywords+"'";
			rs = stmt.executeQuery(sql_duplication);

			if(rs.next()){
				idx = rs.getInt("idx");
				return idx;
			}else{
				//중복 아니면 추가
				String sql = "insert into bayesian_condition_log(lang,keywords,created_at) values ('"+lang+"','"+keywords+"',now())";
				stmt.executeQuery(sql);

				//그리고 그 인덱스 찾기
				rs = stmt.executeQuery(sql_duplication);
				while(rs.next()) {
					idx = rs.getInt("idx");
				}
				return idx;
			}
			//그 조건의 idx 찾기
		}catch(Exception e){
			e.printStackTrace();
			return idx;
		}finally{
			try{if (rs != null) rs.close();} catch(Exception e){}
			try{if (stmt != null) stmt.close();} catch(Exception e){}
			try{if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

	public String getLog(int idx, boolean get_only_log){
		Connection conn = null;
		Statement stmt = null;			
		ResultSet rs = null;
		String result = "";
		try{
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection(url,id,pw);

			stmt = conn.createStatement();
			String sql = "select * from bayesian_condition_log where idx="+idx;
			rs = stmt.executeQuery(sql);
			if(get_only_log){
				//key와 language 보냄
				if(rs.next()){
					result += rs.getString("lang")+"|"+rs.getString("keywords")+"||";
					System.out.println("!!!"+result);
				}
			} else{
				//spam수와 ham 수 
				if(rs.next()){
					result = rs.getInt("spam_num")+"|"+rs.getInt("ham_num");
				}
			}
			return result;
		}catch(Exception e){
			e.printStackTrace();
			return result;
		}finally{
			try{if (rs != null) rs.close();} catch(Exception e){}
			try{if (stmt != null) stmt.close();} catch(Exception e){}
			try{if (conn != null) conn.close();} catch(Exception e) {}
		}
	}

	public ArrayList<TrainingResult> getTrainedData(int idx){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<TrainingResult> savedTrain_set = new ArrayList<TrainingResult>();

		try{
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection(url,id,pw);

			stmt = conn.createStatement();

			String sql = "select * from bayesian_trained_data where log_idx="+idx;
			rs = stmt.executeQuery(sql);

			while(rs.next()){
				String t_word = rs.getString("word");
				int t_spam = rs.getInt("spam");
				int t_ham = rs.getInt("ham");
				double t_ws = rs.getDouble("ws");
				double t_wh = rs.getDouble("wh");

				savedTrain_set.add(new TrainingResult(t_word, t_spam, t_ham, t_ws, t_wh));
			}

			return savedTrain_set;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public boolean saveTrainData(ArrayList<TrainingResult> train_set, int log_idx, int spam, int ham){
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt = null;	
		try{
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection(url,id,pw);
			//update 수행하는데 delete -> insert 방식으로!
			stmt = conn.createStatement();
			//기존 것 delete
			String delete_sql = "delete from bayesian_trained_data where log_idx="+log_idx;
			stmt.executeUpdate(delete_sql);

			System.out.println("!!saveTrainData in");
			String sql = "insert into bayesian_trained_data values(?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			int count = 0;
			for(TrainingResult train : train_set){
				//DB에 저장
//				System.out.print(count+" ");
				pstmt.setString(1, train.getWord());
				pstmt.setInt(2, train.getSpamCount());
				pstmt.setInt(3, train.getHamCount());
				pstmt.setDouble(4, train.getWs());
				pstmt.setDouble(5, train.getWh());
				pstmt.setInt(6, log_idx);
				pstmt.executeUpdate();
				count++;
			}
			
			
			System.out.println("22222222spam: "+spam+", ham: "+ham);
			//여기에 사용된 spam과 ham의 문서 수 저장
			String update_sql = "update bayesian_condition_log set spam_num=?, ham_num=? where idx=?";
			pstmt = conn.prepareStatement(update_sql);
			pstmt.setInt(1, spam);
			pstmt.setInt(2, ham);
			pstmt.setInt(3, log_idx);
			pstmt.executeUpdate();

			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try{if (pstmt != null) pstmt.close();} catch(Exception e){}
			try{if (conn != null) conn.close();} catch(Exception e) {}
		}

	}

}
