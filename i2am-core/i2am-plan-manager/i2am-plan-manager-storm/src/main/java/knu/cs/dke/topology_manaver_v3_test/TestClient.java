package knu.cs.dke.topology_manaver_v3_test;

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

	private static String serverIp = "localhost";
	private static int serverPort = 11111;
		
	public static void main(String[] args) throws UnknownHostException, IOException {

		// JSON 생성하기
		// CreatePlanJson source = new CreatePlanJson();
		// source.setCommand();		
		// source.printJSON();
		
		// JSON 생성하기 2
		
		// Create Plan
		//String json = "{\"commandType\":\"CREATE_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"algorithms\":[{\"algorithmType\":\"BINARY_BERNOULLI_SAMPLING\",\"algorithmIdx\":1,\"algorithmParams\":{\"windowSize\":100,\"sampleSize\":20}}],\"srcName\":\"SRC1\",\"dstName\":\"DST1\",\"planName\":\"Plan3\",\"createdTime\":\"2017-11-22 17:08:37\"},\"commandId\":\"69464a38-8984-4f45-a2ea-97fd8fdc8934\",\"commandTime\":\"2017-11-22 17:08:37\"}";
		
		// Active Plan
		//String json = "{\"commandType\":\"CHANGE_STATUS_OF_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"modifiedTime\":\"2017-11-22 17:19:09\",\"planName\":\"Plan3\",\"after\":\"ACTIVE\"},\"commandId\":\"18d2cd85-a7ac-4d5a-9b48-17558ee51f8b\",\"commandTime\":\"2017-11-22 17:19:09\"}";
			
		// Deactive Plan
		//String json = "{\"commandType\":\"CHANGE_STATUS_OF_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"modifiedTime\":\"2017-11-22 17:31:03\",\"planName\":\"Plan3\",\"after\":\"DEACTIVE\"},\"commandId\":\"27eea669-17ff-44e6-8c30-04c8275c6cad\",\"commandTime\":\"2017-11-22 17:31:03\"}";
		
		// Create Source
		//String json = "{\"commandType\":\"CREATE_SRC\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"srcName\":\"SelfTest\",\"kafkaParams\":{\"zookeeperIp\":\"MN\",\"zookeeperPort\":\"9092\",\"topic\":\"topic-in\"},\"usesIntelligentEngine\":\"N\",\"createdTime\":\"2017-11-22 21:38:10\",\"srcType\":\"KAFKA\"},\"commandId\":\"451b7cad-293b-476a-882f-6fef060a3260\",\"commandTime\":\"2017-11-22 21:38:10\"}";
		
		// Run Source
		String json = "{\"commandType\":\"CHANGE_STATUS_OF_SRC\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"modifiedTime\":\"2017-11-22 21:38:37\",\"srcName\":\"SelfTest\",\"after\":\"ACTIVE\"},\"commandId\":\"08d1938d-00f3-456d-9ee9-15537b935900\",\"commandTime\":\"2017-11-22 21:38:37\"}";
		
		
		// Server에 접속하기
		Socket socket = new Socket(serverIp, serverPort);		
		DataOutputStream write;		
		write = new DataOutputStream(socket.getOutputStream());
		
		// Server에 String 타입으로 보내기
		write.writeUTF(json);
				
		write.close();
		socket.close();
	}	
}

