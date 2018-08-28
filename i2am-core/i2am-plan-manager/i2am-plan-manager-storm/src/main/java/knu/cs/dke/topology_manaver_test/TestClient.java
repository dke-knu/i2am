package knu.cs.dke.topology_manaver_test;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TestClient {

	private static String serverIp = "114.70.235.43";
	private static int serverPort = 11111;
		
	public static void main(String[] args) throws UnknownHostException, IOException {

		// JSON 생성하기
		// CreatePlanJson source = new CreatePlanJson();
		// source.setCommand();		
		// source.printJSON();
		
		// JSON 생성하기 2
		
		// Create Plan
		// String json = "{\"commandType\":\"CREATE_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"algorithms\":[{\"algorithmType\":\"QUERY_FILTERING\",\"algorithmIdx\":1,\"algorithmParams\":{\"keywords\":\"keyword1 keyword2 keyword3\"}}],\"srcName\":\"SRC1\",\"dstName\":\"DST1\",\"planName\":\"testPlan\",\"createdTime\":\"2017-11-24 14:48:36\"},\"commandId\":\"ae72cea5-1ac6-482e-8b47-d296863025c8\",\"commandTime\":\"2017-11-24 14:48:36\"}";
		
		// Active Plan
		String json = "{\"commandType\":\"CHANGE_STATUS_OF_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"modifiedTime\":\"2017-11-24 14:49:17\",\"planName\":\"testPlan\",\"after\":\"ACTIVE\"},\"commandId\":\"7354d7e6-2bf2-404b-b295-52f7ea95971f\",\"commandTime\":\"2017-11-24 14:49:17\"}";
			
		// Deactive Plan
		//String json = "{\"commandType\":\"CHANGE_STATUS_OF_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"modifiedTime\":\"2017-11-22 17:31:03\",\"planName\":\"Plan3\",\"after\":\"DEACTIVE\"},\"commandId\":\"27eea669-17ff-44e6-8c30-04c8275c6cad\",\"commandTime\":\"2017-11-22 17:31:03\"}";
		
		// Create Source
		//String json = "{\"commandType\":\"CREATE_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"algorithms\":[{\"algorithmType\":\"QUERY_FILTERING\",\"algorithmIdx\":1,\"algorithmParams\":{\"keywords\":\"dfgfg,sfsdf,sdfdsf\"}}],\"srcName\":\"SRC1\",\"dstName\":\"DST1\",\"planName\":\"sdfdsf\",\"createdTime\":\"2017-11-23 22:14:23\"},\"commandId\":\"2b0cacdb-3c03-4b11-af49-b479f7c8c226\",\"commandTime\":\"2017-11-23 22:14:23\"}";
		
		// Run Source
		// String json = "{\"commandType\":\"CHANGE_STATUS_OF_SRC\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"modifiedTime\":\"2017-11-22 21:38:37\",\"srcName\":\"SelfTest\",\"after\":\"ACTIVE\"},\"commandId\":\"08d1938d-00f3-456d-9ee9-15537b935900\",\"commandTime\":\"2017-11-22 21:38:37\"}";
		
		
		// Server에 접속하기
		Socket socket = new Socket(serverIp, serverPort);		
		DataOutputStream write;		
		write = new DataOutputStream(socket.getOutputStream());
		
		// Server에 String 타입으로 보내기
		write.writeUTF(json);
				
		write.close();
		socket.close();
		
		String s = "test";
		
	}	
}

