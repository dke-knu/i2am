<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import = "java.sql.*" %>
<!DOCTYPE html>
<html>
<head>
<title>Dynamic Anomaly Detection</title>
<script src="http://d3js.org/d3.v2.js"></script>
<script src="http://code.jquery.com/jquery-1.7.2.min.js"></script>
<script src="js/data.js"></script>
<script src="js/line-graph.js"></script>
<link rel="stylesheet" href="style/style.css" type="text/css">
<style>
body {
	font-family: "Helvetica Neue", Helvetica;
}

p {
	clear: both;
	top: 20px;
}

div.aGraph {
	margin-bottom: 30px;
}
</style>
</head>
<body>
	<div id="graph1" class="aGraph"
		style="position: relative; width: 100%; height: 400px"></div>

	<script>
		if (parent.document.getElementsByTagName("iframe")[0]) {
			parent.document.getElementsByTagName("iframe")[0].setAttribute(
					'style', 'height: 650px !important');
		}
		
		log_data["displayNames"] = [ "value", "upper", "lower", "is" ];
		log_data["colors"] = [ "green", "orange", "red", "darkred" ];
		log_data["scale"] = "linear";

		for (var i=0; i<4; i++) {
			log_data.values[i] = new Array(4320);
			for (var j=0; j<4320; j++)
				log_data.values[i][j] = 0;
		}

		<%
		long endTime = System.currentTimeMillis()/1000;
		long startTime = endTime - (60*60*6);
		%>
		
		log_data["start"] = <%=startTime*1000%>;
		log_data["end"] = <%=endTime*1000%>;
		temp_data["start"] = log_data["end"] + 5000;
		temp_data["end"] = temp_data["start"];
		
		<%
		Connection conn = null;
		
		try {
			String driver = "org.mariadb.jdbc.Driver";
			String url = "jdbc:mysql://114.70.235.40/anomalydetection";
			String id = "anomalydetection";
			String pw = "dke304";

			Class.forName(driver);
			conn = DriverManager.getConnection(url, id, pw);
			Statement stmt = conn.createStatement();
			
			String sql = "SELECT log_value, upper_bound, lower_bound, is_anomaly, UNIX_TIMESTAMP(logging_time) AS logging_time " + 
					"FROM TBL_ANOMALY_DETECTION WHERE cluster_name='cluster0' AND logging_time > from_unixtime('" + startTime + "');";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int idx = (int) Math.max(Math.min(Integer.MAX_VALUE, (rs.getLong("logging_time")-startTime)/5), Integer.MIN_VALUE);
				double value = rs.getDouble("log_value");
				double upper = rs.getDouble("upper_bound");
				double lower = rs.getDouble("lower_bound");
		%>
				log_data.values[0][<%=idx%>] = <%=value%>;
				log_data.values[1][<%=idx%>] = <%=upper%>;
				log_data.values[2][<%=idx%>] = <%=lower%>;
		<%
			}
		} catch(Exception e){ 
			e.printStackTrace();
		}
		%>
		
		var l1 = new LineGraph({
			containerId : 'graph1',
			data : log_data
		});

		setInterval(function() {
			$.ajax({
				type : 'post',
				url : './ajax/getNewADResults.jsp',
				data : ({
					param4 : temp_data["start"]
				}),
				success : function(data) {
					data = data.replace(/(^\s*)|(\s*$)/gi, "");
				}
			});
			
			
			var newData = [];
			log_data.values.forEach(function(dataSeries, index) {
				var v = dataSeries.shift();
				dataSeries.push(v);
				newData[index] = [ v ];
			})

			temp_data.values = newData;
			temp_data.start = temp_data.start + temp_data.step;
			temp_data.end = temp_data.end + temp_data.step;

			l1.slideData(temp_data);
		}, 5000);
	</script>
</body>
</html>
