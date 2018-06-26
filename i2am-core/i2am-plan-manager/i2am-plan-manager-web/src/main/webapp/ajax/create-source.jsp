<%@ page import="i2am.plan.manager.web.bean.DatabaseInfo.DATABASE_TYPE"%>
<%@ page import="i2am.plan.manager.web.CommandSubmitter.SRC_TYPE"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="EUC-KR" import="java.util.*" import="i2am.plan.manager.web.*" import="i2am.plan.manager.web.bean.*"%>
<%
	// Source Info.
	String user_id = (String) session.getAttribute("user_id");
	String srcName = request.getParameter("source_name");
	
	// Source Type
	String srcType = request.getParameter("source_type");
	
	// Kafka
	String zookeeperIp = request.getParameter("zookeeperIp");
	String zookeeperPort = request.getParameter("zookeeperPort");
	String kafkaTopic = request.getParameter("kafkaTopic");
	
	// Database
	String dbIp = request.getParameter("dbIp");
	String dbPort = request.getParameter("dbPort");
	String dbId = request.getParameter("dbId");
	String dbPw = request.getParameter("dbPw");
	String dbName = request.getParameter("dbName");
	String dbQuery = request.getParameter("dbQuery");
		
	// Data Schema
	String[] dataScheme = request.getParameterValues("dataScheme");
	
	// Smart Engine
	Boolean usesCD = Boolean.parseBoolean(request.getParameter("useConceptDriftEngine"));
	Boolean usesLS = Boolean.parseBoolean(request.getParameter("useLoadShedding"));
	Boolean usesIntelli = Boolean.parseBoolean(request.getParameter("useIntelligentEngine"));		
	String testDataName = request.getParameter("testData");
	String target = request.getParameter("target");
	
	CommandSubmitter submitter = new CommandSubmitter();
	
	KafkaInfo kafka = null;
	DatabaseInfo database = null;
	
	if (SRC_TYPE.valueOf(srcType) == SRC_TYPE.KAFKA) {
		kafka = new KafkaInfo(zookeeperIp, zookeeperPort, kafkaTopic);
		
	} else if (SRC_TYPE.valueOf(srcType) == SRC_TYPE.DATABASE) {		
		database = new DatabaseInfo(DATABASE_TYPE.SRC, dbIp, dbPort, dbId, dbPw, dbName, dbQuery);				
	}
	
	submitter.createSrc(user_id, srcName, SRC_TYPE.valueOf(srcType), kafka, database, dataScheme, usesCD, usesLS, usesIntelli, testDataName, target);	
	submitter.submit();
	String command = submitter.printCommand();
	
%>
<%= command %>

