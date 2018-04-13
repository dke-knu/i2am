<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="css/list.css">

<script>
$(document).on("click", ".newbutton", function(){

	window.open("./create-source.jsp", "_self");	
	
});
</script>

<title>Plan Manager - Source</title>
</head>

<body>
	<div class="titleline">
		<h1 class="title"> Source </h1>
		<button class="newbutton"> New Source</button>
		<div class="clear"></div>			
	</div>		
	<hr><br>	
	Your Sources

</body>
</html>