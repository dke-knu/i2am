<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.util.*" import="i2am.plan.manager.web.*"%>
	
<%
	String user_id = (String) session.getAttribute("user_id");
	String commandType = "CHANGE_STATUS_OF_" + request.getParameter("type").toUpperCase(); // SRC, DST, PLAN
	String name = request.getParameter("name");
	String after = request.getParameter("after");

	CommandSubmitter submitter = new CommandSubmitter();
	submitter.changeStatus(user_id, CommandSubmitter.COMMAND_TYPE.valueOf(commandType), name, CommandSubmitter.STATUS.valueOf(after));
	
	submitter.submit();
	String command = submitter.printCommand();
%>
<%=command %>