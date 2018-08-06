<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.sql.*" import="java.util.*" import="i2am.plan.manager.web.*"%>
<%
	String id = (String) session.getAttribute("user_id");
	String name = request.getParameter("name");
	String type = request.getParameter("type");
	
	boolean result = DbAdapter.getInstance().checkRedundancy(type, id, name);
%>
<%=result %>
