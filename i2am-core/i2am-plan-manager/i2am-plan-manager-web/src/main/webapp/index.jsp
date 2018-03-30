<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<meta charset="utf-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<link rel="stylesheet" type="text/css" href="css/template.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/my.js"></script>

<script>
$(document).ready(function() {
        
	$("#home").click(function() {
        
    	$(".main").load("home.jsp");
		return false;
	});

	$("#plan").click(function() {
		
		$(".main").load("list_plan.jsp");
		return false;
	});		
	
	$("#source").click(function() {
		
		$(".main").load("list_source.jsp");
		return false;
	});
	
	$("#destination").click(function() {
		
		$(".main").load("list_destination.jsp");
		return false;
	});
	
});
</script>


<title>Plan Manager - DashBoard</title>

</head>

<body>

	<div class="header">
		<h1> Plan Manager </h1>
		<p> Create a plan </p>
	</div>


	<div id="navbar">
		<a href="#" id="home"> Home </a>
		<a href="#" id="plan"> Plan </a>
		<a href="#" id="source"> Source </a>
		<a href="#" id="destination"> Destination </a>
		<div id="dropdown">
			<button id="dropbtn">Resource
				<i class="fa fa-caret-down"></i>
			</button>
			<div id="dropdown-content">
				<a href="#"> Storm </a>
				<a href="#"> Kafka </a>
				<a href="#"> Database </a>
				<a href="#"> Redis </a>
			</div>			
		</div>
		<a href="#"> User </a>
		<a href="#"> Framework </a>
	</div>

	<div class="row">
				
		<div class="main">			
			
			<h1> Welcome! </h1>
			<hr>
			<br>				
			Select menu
					
		</div>
	
	</div>


	<div class="footer">
		<h3>I2AM</h3>
		Kangwon Univ.
	</div>

	
	<script>
		window.onscroll = function() {myFunction()};

		var navbar = document.getElementById("navbar");		
		var sticky = navbar.offsetTop;

		function myFunction() {
  			if (window.pageYOffset >= sticky) {
    			navbar.classList.add("sticky")
  			} else {
    			navbar.classList.remove("sticky");
  			}
		}
	</script>	
	
</body>

</html>