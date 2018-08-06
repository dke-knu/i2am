<%@ page import="i2am.plan.manager.web.bean.DatabaseInfo.DATABASE_TYPE"%>
<%@ page import="i2am.plan.manager.web.CommandSubmitter.DST_TYPE"%>
<%@ page language="java" contentType="text/html; charset=utf-8" 
	pageEncoding="EUC-KR" import="java.util.*" import="i2am.plan.manager.web.*" import="i2am.plan.manager.web.bean.*"%>
<%
	String user_id = (String) session.getAttribute("user_id");
	String dstName = request.getParameter("dst_name");
	String dstType = request.getParameter("dst_type");

	CommandSubmitter submitter = new CommandSubmitter();
	
	KafkaInfo kafka = null;
	DatabaseInfo database = null;
	if (DST_TYPE.valueOf(dstType) == DST_TYPE.KAFKA) {
		kafka = new KafkaInfo(request.getParameter("zookeeper_ip"), request.getParameter("zookeeper_port"), request.getParameter("kafka_topic"));
	} else if (DST_TYPE.valueOf(dstType) == DST_TYPE.DATABASE) {
		database = new DatabaseInfo(DATABASE_TYPE.DST,
				request.getParameter("database_ip"), request.getParameter("database_port"), 
				request.getParameter("database_id"), request.getParameter("database_pw"), 
				request.getParameter("database_db"), request.getParameter("database_table")
			);
	}
	submitter.createDst(user_id, dstName, DST_TYPE.valueOf(dstType), database, kafka); 
	
	submitter.submit();
	String command = submitter.printCommand();
%>
<%=command %>
