package knu.cs.dke.topology_manager_v2_test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import org.json.simple.JSONObject;

public class TestClient2CreatePlan implements Runnable {
	private static int idx = 1;
	
	public void next() {
		Socket socket = null;
		DataOutputStream write;
		
		try {
			socket = new Socket("localhost", 11111);

			write = new DataOutputStream(socket.getOutputStream());
			write.writeUTF(createPlan().toJSONString());
			
			write.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONObject createPlan() {
		JSONObject command = new JSONObject();
		command.put("commandID", UUID.randomUUID().toString());
		command.put("user", "user"+idx++);
		command.put("commandType", "createPlan");
		
		return command;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			next();
		}
	}
}
