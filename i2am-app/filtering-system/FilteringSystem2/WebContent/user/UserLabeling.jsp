<%@page import="knu.cs.dke.vo.TwitterEvent" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@page import="knu.cs.dke.prog.util.Constant" %>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>labeling</title>
</head>
<body>
<!-- 트위터 데이터 사용자의 이름, 생성일, 내용, 언어 -->
<form action='labeling' method='post'>
<jsp:useBean id="twits"
	scope = "request"
	class = "java.util.ArrayList"
	type = "java.util.ArrayList<knu.cs.dke.vo.TwitterEvent>"/>


<div align='center' stype="width:100%; height:200px; overflow:auto">
<table width="100%" border="1" cellspacing="0" celpadding="0">

<% int i=0; %>
<% for(TwitterEvent twit : twits) {%>
	<tr>
		<td align="center"><input type="checkbox" name="_selected_" value="<%= i %>"></td>
		<td align="left"><%= i %><br>
		사용자 이름: <%= twit.getUserName() %><br>
						사용자 Id: <%= twit.getUserId() %><br>
						언어: <%= twit.getLang() %><br>
						내용: <%= twit.getText() %><br>
						</td>
	</tr>
	<% i++; %>
<%} %>
</table>
</div>
<!-- output type을 파일로 정했으면, 사용자가 원할 때 파일 다운로드 가능하도록 함 -->
<br>
<p align='center'>
<!-- <input type='submit' name="ReturnOrRedo" value='file_return'> -->
<input TYPE='IMAGE' src="user/image/save.png" name='ReturnOrRedo' onclick="alert('file')" value='file_return' width='50' align='absmiddle'>
<!-- <input type='button' onclick="location.href='..\\setting?file=0'" value='처음 페이지로'>-->
<input TYPE='IMAGE' src="user/image/home.png" onclick="alert('home');location.href='http://SERVER_IP:PORT/FilteringSystem/return_file?file=0'" value='처음 페이지로' width='50' align='absmiddle'>

<!-- <input type='submit' name="ReturnOrRedo" value='시작'>-->
<input TYPE='IMAGE' src="user/image/feedback.png" name='ReturnOrRedo' value='Submit' height='45' align='absmiddle'>
</p>
</form>
</body>
</html>