<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.sql.*" import="java.util.*" import="org.json.simple.*" %>
<%
	String param1 = request.getParameter("param1");
	long startTime = Long.parseLong(param1);
	String cluster = request.getParameter("param2");
	String host = request.getParameter("param3");
	String key = request.getParameter("param4");
	JSONObject data = new JSONObject();

	try {
		String driver = "org.mariadb.jdbc.Driver";
		String url = "jdbc:mysql://" + "/anomalydetection";
		String id = "";
		String pw = "";

		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, id, pw);
		Statement stmt = conn.createStatement();

		String sql = "SELECT log_value, upper_bound, lower_bound, is_anomaly, UNIX_TIMESTAMP(logging_time) AS logging_time "
				+ "FROM TBL_ANOMALY_DETECTION "
				+ "WHERE cluster_name='" + cluster + "' AND host_name='" + host + "' AND log_key='" + key + "' "
				+ "AND logging_time > from_unixtime('"
				+ startTime + "');";
		ResultSet rs = stmt.executeQuery(sql);

		JSONObject values = new JSONObject();
		JSONObject uppers = new JSONObject();
		JSONObject lowers = new JSONObject();
		JSONObject anomalies = new JSONObject();
		while (rs.next()) {
			int idx = (int) Math.max(Math.min(Integer.MAX_VALUE, (rs.getLong("logging_time") - startTime) / 5),
					Integer.MIN_VALUE);
			values.put(idx, rs.getDouble("log_value"));
			uppers.put(idx, rs.getDouble("upper_bound"));
			lowers.put(idx, rs.getDouble("lower_bound"));
			anomalies.put(idx, rs.getBoolean("is_anomaly"));
		}
		data.put("values", values);
		data.put("uppers", uppers);
		data.put("lowers", lowers);
		data.put("anomalies", anomalies);
		if (stmt != null)
			stmt.close();
		if (conn != null)
			conn.close();
	} catch (SQLException e) {
		e.printStackTrace();
		out.println("연결 에러");
	}
%>
<%=data.toJSONString()%>