<%@ page import="i2am.plan.manager.web.bean.DatabaseInfo.DATABASE_TYPE"%>
<%@ page import="i2am.plan.manager.web.CommandSubmitter.SRC_TYPE"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.util.*" import="i2am.plan.manager.web.*" import="i2am.plan.manager.web.bean.*"%>
<%
	String user_id = (String) session.getAttribute("user_id");
	String srcName = request.getParameter("src_name");
	String srcType = request.getParameter("src_type");
	Boolean usesIntelli = Boolean.parseBoolean(request.getParameter("uses_intelligent_engine"));
	String testDataName = request.getParameter("test_data_name");
	CommandSubmitter submitter = new CommandSubmitter();
	
	KafkaInfo kafka = null;
	DatabaseInfo database = null;
	if (SRC_TYPE.valueOf(srcType) == SRC_TYPE.KAFKA) {
		kafka = new KafkaInfo(request.getParameter("zookeeper_ip"), request.getParameter("zookeeper_port"), request.getParameter("kafka_topic"));
	} else if (SRC_TYPE.valueOf(srcType) == SRC_TYPE.DATABASE) {
		database = new DatabaseInfo(DATABASE_TYPE.SRC,
				request.getParameter("database_ip"), request.getParameter("database_port"), 
				request.getParameter("database_id"), request.getParameter("database_pw"),
				request.getParameter("database_name"), request.getParameter("database_query")
			);
	}
	//submitter.createSrc(user_id, srcName, SRC_TYPE.valueOf(srcType), database, kafka, usesIntelli, testDataName);
	
	submitter.submit();
	String command = submitter.printCommand();
%>
<%=command %>