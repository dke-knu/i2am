<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="css/list.css">

<script>
$(document).on("click", ".newbutton", function(){

	window.open("./create-destination.jsp", "_self");	
	
});
</script>

<title>Plan Manager - Destination</title>
</head>

<body>

	<div class="titleline">
		<h1 class="title"> Destination </h1>
		<button class="newbutton" > New Destination</button>
		<div class="clear"></div>			
	</div>
	
	<hr><br>
	
	Your Destination

</body>
</html>