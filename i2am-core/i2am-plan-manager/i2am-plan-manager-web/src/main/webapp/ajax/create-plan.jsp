<%@ page import="i2am.plan.manager.web.CommandSubmitter.ALGORITHM_TYPE"%>
<%@ page language="java" contentType="text/html; charset=utf-8" 
	pageEncoding="EUC-KR" import="java.util.*" import="i2am.plan.manager.web.*" import="i2am.plan.manager.web.bean.*"%>
<%	

	String user_id = (String) session.getAttribute("user_id");
	String newPlan = (String) request.getParameter("newPlan");
	
	CommandSubmitter submitter = new CommandSubmitter();		
	submitter.createPlan(user_id, newPlan);
	submitter.submit();
	String command = submitter.printCommand();
%>

<%= newPlan %>
