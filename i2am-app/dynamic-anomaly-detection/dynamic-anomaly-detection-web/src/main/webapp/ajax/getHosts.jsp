<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.sql.*" import="java.util.*"%>
<%
	String cluster = request.getParameter("param1");
	String result = new String();
	
	try {
		String driver = "org.mariadb.jdbc.Driver";
		String url = "jdbc:mysql://" + "/anomalydetection";
		String id = "";
		String pw = "";

		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, id, pw);
		Statement stmt = conn.createStatement();

		String sql = "SELECT DISTINCT(host_name) FROM TBL_ANOMALY_DETECTION " +
				"WHERE cluster_name='" + cluster + "' ORDER BY host_name;";
		ResultSet rs = stmt.executeQuery(sql);
		
		StringBuilder sb = new StringBuilder();
		while (rs.next())
			sb.append(rs.getString(1) + ",");
		result = sb.substring(0, sb.length()-1);
		
		if (stmt != null)
			stmt.close();
		if (conn != null)
			conn.close();
	} catch (SQLException e) {
		e.printStackTrace();
		out.println("Connection error.");
	}
%>
<%=result%>