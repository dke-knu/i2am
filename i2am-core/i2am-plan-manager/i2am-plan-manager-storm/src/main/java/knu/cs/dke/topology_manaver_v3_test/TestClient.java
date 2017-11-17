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
		String json = "{\"commandType\":\"CREATE_PLAN\",\"commander\":\"0KUK@naver.com\",\"commandContent\":{\"owner\":\"0KUK@naver.com\",\"algorithms\":[{\"algorithmType\":\"BINARY_BERNOULLI_SAMPLING\",\"algorithmIdx\":1,\"algorithmParams\":{\"windowSize\":11,\"sampleSize\":11}}],\"srcName\":\"SRC1\",\"dstName\":\"DST1\",\"planName\":\"sdfsd\",\"createdTime\":\"2017-11-17 10:37:27\"},\"commandId\":\"9dda7be7-0435-43fd-84b0-7355c8269939\",\"commandTime\":\"2017-11-17 10:37:27\"}";
		
		//JSONParser parser = new JSONParser();
		
		
		
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

