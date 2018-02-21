<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    request.setCharacterEncoding("UTF-8");
	String keyword = request.getParameter("search");
	String[] keywords = keyword.split(",");
	int length = keywords.length;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>D3js Tutorial</title>
<script type="text/javascript" src="./js/jquery-3.1.0.js"></script>
<link rel="stylesheet" type="text/css" href="./style/layout.css" />
<link rel="stylesheet" type="text/css" href="./style/blog.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script type="text/javascript" src="./js/d3.min.js"></script>
<script type="text/javascript" src="./js/d3.layout.cloud.js"></script>
<script type="text/javascript" src="./js/wordcloud.js"></script>
<script>
$('document').ready(function () {
	drawWordcloud();
	websocket_init();
	ui_init();
});

function websocket_init() {
	if('WebSocket' in window) {
		var host="ws://" + ":8080/dynamic-tagcloud/websocket"; // webserver ip
		var socket = new WebSocket(host);
		
		socket.oneror = function(event) {
			console.log("error" + event)
			//err
		};
		
		socket.onopen = function() {
			//open
			console.log("opened");
			
			socket.send("CLIENT::"+"<%=keyword%>");
		};
		
		socket.onmessage = function(event) {
			//message
			console.log("message received: "+event.data);
 			updateWordcloud(event.data);
		};
		
		socket.onclose = function() {
			//close
			console.log("closed");
		};
	} else {
		alert("websocket not supported in this browser!");
	}
}

function ui_init() { 
<%
	for(int i=0;i<length;++i) {
%>	
		$('#words').append("<span class='span_wc' id='<%=keywords[i] %>'><%=keywords[i] %> </span>");
<%
	}
%>
}

function updateWordcloud(input) {
	$('#word-cloud').removeAttr('style');
	dataChanged(input);
}
</script>
</head>
<body>
	<div class="blog-masthead">
		<div class="container">
			<nav class="blog-nav">
				<a class="blog-nav-item active" href="#">TagCloud</a>
			</nav>
		</div>
	</div>
	<div class="container">
		<div id="words">
		</div>
	 	<div id="word-cloud" style="background-image:url('./image/loading.gif'); background-repeat:no-repeat; background-position:center;">
	 	</div>
	</div>
	<footer class="blog-footer">
		<div class="container">
			 <p class="text-muted">Copyrightⓒ2016 by Data and Knowledge Engineering Lab</p>
		</div>
	</footer>
</body>
</html>