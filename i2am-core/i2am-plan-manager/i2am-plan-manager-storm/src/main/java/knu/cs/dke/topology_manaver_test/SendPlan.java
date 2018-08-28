package knu.cs.dke.topology_manaver_test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;

public class SendPlan {

	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		
		// Ip & Port
		String ip = "114.70.235.43";
		int port = 40523;
		
		// JSON Message
		JSONObject message = new JSONObject();
		//message.put("message", "new-src");
		message.put("user-id", "sbpark@kangwon.ac.kr");
		message.put("plan-name", "plan_sb");
		
		Socket socket = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		socket = new Socket(ip, port);
		os = socket.getOutputStream();
		osw = new OutputStreamWriter(os);
		bw = new BufferedWriter(osw);
		bw.write(message.toJSONString());
		
		bw.close();
		osw.close();
		os.close();
		socket.close();		
	}

}
