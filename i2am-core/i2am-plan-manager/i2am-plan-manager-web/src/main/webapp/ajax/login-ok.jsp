<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.sql.*" import="java.util.*" import="i2am.plan.manager.web.*"%>
<%
	String id = request.getParameter("user_id");
	String pw = request.getParameter("user_pw");
	
	boolean result = DbAdapter.getInstance().login(id, pw);
	session.setAttribute("user_id", id);	
%>
<%=result %>
