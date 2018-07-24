<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.util.*" import="i2am.plan.manager.web.*"%>
	
<%
	String user_id = (String) session.getAttribute("user_id");
	String commandType = "DESTROY_" + request.getParameter("type").toUpperCase(); // SRC, DST, PLAN
	String name = request.getParameter("name");

	CommandSubmitter submitter = new CommandSubmitter();
	submitter.remove(user_id, CommandSubmitter.COMMAND_TYPE.valueOf(commandType), name);	
	
	submitter.submit();
	String command = submitter.printCommand();
%>
<%=command %>