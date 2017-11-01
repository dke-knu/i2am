package knu.cs.dke.topology_manaver_v3_test;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestClient {

	private static String serverIp = "localhost";
	private static int serverPort = 11111;
		
	public static void main(String[] args) throws UnknownHostException, IOException {

		// JSON 생성하기
		JSONSample plan = new JSONSample();
		plan.setCommand();		
		
		// Server에 접속하기
		Socket socket = new Socket(serverIp, serverPort);		
		DataOutputStream write;		
		write = new DataOutputStream(socket.getOutputStream());
		
		// Server에 String 타입으로 보내기
		write.writeUTF(plan.getStringPlan());
				
		write.close();
		socket.close();
	}
	
}
