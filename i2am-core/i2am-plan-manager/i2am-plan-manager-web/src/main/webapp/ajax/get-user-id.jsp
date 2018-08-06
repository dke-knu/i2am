<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.util.*"%>
<%
	String result = (String) session.getAttribute("user_id");
%>
<%=result %>
