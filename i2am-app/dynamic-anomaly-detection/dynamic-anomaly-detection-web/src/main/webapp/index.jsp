<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Dynamic Anomaly Detection of Log Data</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<!-- Le styles -->
<script src="http://d3js.org/d3.v2.js"></script>
<script src="http://code.jquery.com/jquery-1.7.2.min.js"></script>
<script src="js/line-graph.js"></script>
<script src="js/bootstrap.js"></script>
<script src="js/moment.min.js"></script>
<link rel="stylesheet" href="style/style.css" type="text/css">
<link href="css/bootstrap.css" rel="stylesheet">

<style type="text/css">
body {
	padding-top: 60px;
	padding-bottom: 40px;
	font-family: "Helvetica Neue", Helvetica;
}
p {
	clear: both;
	top: 20px;
}

div.aGraph { 
	margin-bottom: 30px;
}

/* Main marketing message and sign up button */
.jumbotron {
  text-align: center;
}
.jumbotron .btn {
  margin: 20px;
  font-size: 21px;
  padding: 14px 100px;
}
</style>
<link href="css/bootstrap-responsive.css" rel="stylesheet">
</head>

<body>

	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<button type="button" class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="brand" href="#">Dynamic Anomaly Detection of Log Data</a>
				<div class="pull-right">
					<a class="brand" href="#">I2AM</a>
				</div>
			</div>
			<!--/.nav-collapse -->
		</div>
	</div>

	<%
	// sec
	int intervalTime = 5;
	// sec * min * hour
	int totalTime = 60 * 60 * 1;
	try {
		String driver = "org.mariadb.jdbc.Driver";
		String url = "jdbc:mysql://" + "/anomalydetection";
		String id = "";
		String pw = "";

		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, id, pw);
		Statement stmt = conn.createStatement();
		String sql;
		ResultSet rs;
	%>
	
	<div class="container">
		<!-- Example row of columns -->
		<div class="row">
			<div class="span12">
				<h2>Parameter</h2>

			</div>
			<div class="span4">
				<h4>Cluster</h4>
	<%
		sql = "SELECT DISTINCT(cluster_name) FROM TBL_ANOMALY_DETECTION ORDER BY cluster_name;";
		rs = stmt.executeQuery(sql);

		for (int i=0; rs.next(); i++) {
			String cluster = rs.getString("cluster_name");
	%>
				<label class="radio inline" id="clusters-container"> <input type="radio"
					name="clusters" id="cluster<%=i%>" value="<%=cluster %>" onclick="setHosts('<%=cluster %>');">
					<%=cluster %>
				</label><br/>
	<%
		}
	} catch(Exception e){ 
		e.printStackTrace();
	}
	%>
			</div>
			<div class="span4">
				<h4>Host</h4>
				<div id="hosts-container"></div>
			</div>
			<div class="span4">
				<h4>Log Key</h4>
				<div id="keys-container"></div>
			</div>
			<div class="jumbotron">
				<p>
					<a class="btn btn-primary btn-large" href="#" onclick="apply();">Apply &raquo;</a>
				</p>
			</div>
		</div>
		
		<!-- Main hero unit for a primary marketing message or call to action -->
		<div class="hero-unit">
			<div id="graph1" class="aGraph"
				style="position: relative; width: 100%; height: 400px"></div>
			<div id="log_area"></div>
		</div>

		<hr>
		
		<!-- Modal -->
		<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-header">
		    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		    	<h3 id="myModalLabel">Parameters are not filled.</h3>
		  	</div>
			<div class="modal-body">
		    	<p id="modalBody"></p>
		  	</div>
		  	<div class="modal-footer">
			    <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
		  </div>
		</div>

		<footer>
			<p>&copy; Kangwon National University</p>
		</footer>

	</div>
	<!-- /container -->

	<!-- Le javascript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script type="text/javascript">
	var log_data = {"start":0,"end":0,"step":5000,"names":["log_value","upper_bound","lower_bound","is_anomaly"],
			"values":[[],[],[],[]]};

	var temp_data = {"start":0,"end":0,"step":5000,"names":["log_value","upper_bound","lower_bound","is_anomaly"],
			"values":[[],[],[],[]]};
	
	log_data["displayNames"] = [ "value", "upper", "lower", "anomaly" ];
	log_data["colors"] = [ "black", "blue", "green", "red" ];
	log_data["opacity"] = [ 1.0, 1.0, 1.0, 0.0];
	log_data["lineTypes"] = [ "solid", "dashed", "dashed", "solid"];
	log_data["rounding"] = [ 3, 3, 3, 3 ];
	log_data["scale"] = "log";

	for (var i=0; i<4; i++) {
		log_data.values[i] = new Array(<%=totalTime/intervalTime%>);
		for (var j=0; j<<%=totalTime/intervalTime%>; j++)
			log_data.values[i][j] = 0;
	}

	
	function setHosts(cluster) {
		document.getElementById("hosts-container").innerHTML = '';
		document.getElementById("keys-container").innerHTML = '';
		$.ajax({
			type : 'post',
			url : './ajax/getHosts.jsp',
			data : ({
				param1 : cluster
			}),
			success : function(data) {
				data = data.replace(/(^\s*)|(\s*$)/gi, "");
				var hosts = data.split(",");
				document.getElementById("hosts-container").innerHTML = '';
				document.getElementById("keys-container").innerHTML = '';
				for (var i=0; i<hosts.length; i++) {
					createHostsRadioElement(i, hosts[i]);
				}
			},
		});
	}
	
	function createHostsRadioElement(idx, value) {
	    var radioHtml = '<label class="radio inline" id="hosts-container">' +
	    	'<input type="radio" name="hosts" id="host' + idx + '"' + 
	    	'value="' + value + '" onclick="setKeys(\'' + value + '\')"/>' + value + '</label><br/>';

	    var radioFragment = document.getElementById('hosts-container');
	    radioFragment.innerHTML += radioHtml;
	}
	
	function setKeys(host) {
		document.getElementById("keys-container").innerHTML = '';
		var cluster = $('input[name="clusters"]:checked').val();
		$.ajax({
			type : 'post',
			url : './ajax/getKeys.jsp',
			data : ({
				param1 : host,
				param2 : cluster
			}),
			success : function(data) {
				data = data.replace(/(^\s*)|(\s*$)/gi, "");
				var keys = data.split(",");
				document.getElementById("keys-container").innerHTML = '';
				for (var i=0; i<keys.length; i++) {
					createKeysRadioElement(i, keys[i]);
				}
			},
		});
	}
	
	function createKeysRadioElement(idx, value) {
	    var radioHtml = '<label class="radio inline" id="keys-container">' +
	    	'<input type="radio" name="keys" id="key' + idx + '"' + 
	    	'value="' + value + '"/>' + value + '</label><br/>';

	    var radioFragment = document.getElementById('keys-container');
	    radioFragment.innerHTML += radioHtml;
	}
	
	var refreshIntervalId = -1; 
	function apply(){
		var cluster = $('input[name="clusters"]:checked').val();
		var host = $('input[name="hosts"]:checked').val();
		var log_key = $('input[name="keys"]:checked').val();
		
		if (cluster == '' || cluster == null) {
			$('#modalBody').text("Cluster must be checked.");
			$('#myModal').modal('show');
			return ;
		} else if (host == '' || host == null) {
			$('#modalBody').text("Host must be checked.");
			$('#myModal').modal('show');
			return ;
		} else if (log_key == '' || log_key == null) {
			$('#modalBody').text("Key must be checked.");
			$('#myModal').modal('show');
			return ;
		}
		
		// minus delay time.
		var endTime = Math.round(new Date().getTime()/1000 - 5);
		var startTime = endTime - (<%=totalTime%>);
		
		$.ajax({ 
			type : 'post',
			url : './ajax/getADResultsToInit.jsp',
			data : ({
				param1 : startTime,
				param2 : cluster,
				param3 : host,
				param4 : log_key
			}),
			success : function(data) {
				data = data.replace(/(^\s*)|(\s*$)/gi, "");
				
				log_data["start"] = startTime*1000;
				log_data["end"] = endTime*1000;
				temp_data["start"] = log_data["end"] + 5000;
				temp_data["end"] = temp_data["start"];

				var jsonData = JSON.parse(data);
				var jsonValues = jsonData.values; 
				var jsonAnomalies = jsonData.anomalies;
				for (key in jsonValues) {
					log_data.values[0][key] = jsonValues[key];
					if (jsonAnomalies[key] == true)
						log_data.values[3][key] = jsonValues[key];
					else
						log_data.values[3][key] = null; 
				}
				var jsonUppers = jsonData.uppers;
				for (key in jsonUppers) {
					log_data.values[1][key] = jsonUppers[key];
				}
				var jsonLowers = jsonData.lowers;
				for (key in jsonLowers) {
					log_data.values[2][key] = jsonLowers[key];
				}
				
				document.getElementById("graph1").innerHTML = '';
				if (refreshIntervalId != -1)
					clearInterval(refreshIntervalId);

				var l1 = new LineGraph({
					containerId : 'graph1',
					data : log_data
				});
				
				var logArea = document.getElementById("log_area");
				logArea.innerHTML = '';
				for (var i=0; i<log_data.values[3].length; i++) {
					var sb = "";
					if (log_data.values[0][i] == 0) {
						sb += "<div class='alert'><button type='button' class='close' data-dismiss='alert'>&times;</button>";
						sb += "<strong>Missing Value is detected!</strong>";
						var date = moment.unix(startTime*1000 + i*5000);
						sb += " time=" + date.format("HH:mm:ss");
						sb += "<div/>";
					}
					else if (log_data.values[3][i] != null) {
						sb += "<div class='alert alert-error'><button type='button' class='close' data-dismiss='alert'>&times;</button>";
						sb += "<strong>Anomaly is detected!</strong>";
						sb += " value=" + log_data.values[3][i] + ", ";
						var date = moment.unix(startTime*1000 + i*5000);
						sb += "time=" + date.format("HH:mm:ss");
						sb += "<div/>";
					}
					logArea.innerHTML += sb;
				}
					
				
				refreshIntervalId = setInterval(function() {
					// minus delay time.
					var endTime = Math.round(new Date().getTime()/1000 - 5);
					var startTime = endTime - 5;
					
					$.ajax({
						type : 'post',
						url : './ajax/getADResultToUpdate.jsp',
						data : ({
							param1 : startTime,
							param2 : cluster,
							param3 : host,
							param4 : log_key
						}),
						success : function(data) {
							data = data.replace(/(^\s*)|(\s*$)/gi, "");
							
							var jsonData = JSON.parse(data);
							var newData = [];
							newData[0] = [jsonData.value];
							newData[1] = [jsonData.upper];
							newData[2] = [jsonData.lower];
							if (jsonData.anomaly == true) 
								newData[3] = [jsonData.value];
							else
								newData[3] = [null];
							
							log_data.values.forEach(function(dataSeries, index) {
								dataSeries.shift();
								dataSeries.push(newData[index]);
							})
							
							temp_data.values = newData;
							temp_data.start = temp_data.start + temp_data.step;
							temp_data.end = temp_data.end + temp_data.step;
							
							var sb = "";
							if (jsonData.value == 0) {
								sb += "<div class='alert'><button type='button' class='close' data-dismiss='alert'>&times;</button>";
								sb += "<strong>Missing Value is detected!</strong>";
								var date = moment.unix(startTime*1000);
								sb += " time=" + date.format("HH:mm:ss");
								sb += "<div/>";
							}
							else if (jsonData.anomaly == true) {
								sb += "<div class='alert alert-error'><button type='button' class='close' data-dismiss='alert'>&times;</button>";
								sb += "<strong>Anomaly is detected!</strong>";
								sb += " value=" + jsonData.value + ", ";
								var date = moment.unix(startTime*1000);
								sb += "time=" + date.format("HH:mm:ss");
								sb += "<div/>";
							}
							logArea.innerHTML += sb;
							
							l1.slideData(temp_data);
						}
					});
				}, 5000);
			},
		});
	}
	</script>
</body>
</html>
