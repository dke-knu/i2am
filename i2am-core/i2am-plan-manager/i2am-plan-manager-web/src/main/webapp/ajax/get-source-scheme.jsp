<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.sql.*" import="java.util.*" import="i2am.plan.manager.web.*" import="org.json.simple.JSONArray"%>
<%
	String id = (String) session.getAttribute("user_id");
	
	JSONArray result = DbAdapter.getInstance().getSrcScheme(id);
%>
<%=result %>
