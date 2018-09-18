package knu.cs.dke.topology_manager.handlers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;

public class MessageSender {

	// Concept Drift Detection Engine
	static final String CONCEPT_DRIFT_IP = "165.132.214.219";
	static final int CONCEPT_DRIFT_PORT = 39393;
	
	// Intelligent Recommendation Engine
	// IP -> MN
	static final String INTELLIGENT_ENGINE_IP = "114.70.235.43";
	static final int INTELLIGENT_ENGINE_PORT = 7979;
	
	// Intelligent Kalman Filtering
	// IP -> MN
	static final String INTELLIGENT_KALMAN_IP = "114.70.235.43";
	static final int INTELLIGENT_KALMAN_PORT = 40523;
		
	// Load Shedding Manager
	static final String LOAD_SHEDDIING_MANAGER_IP = "114.70.235.43";
	static final int LOAD_SHEDDIING_MANAGER_PORT = 5004;
	
	
	public void sendToConceptDrift(String messageType, String owner, String srcName) throws UnknownHostException, IOException {		
		
		JSONObject message = new JSONObject();
		message.put("message", messageType);
		message.put("user-id", owner);
		message.put("src-name", srcName);
		
		Socket socket = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;		
		
		try {
			socket = new Socket(CONCEPT_DRIFT_IP, CONCEPT_DRIFT_PORT);
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			bw.write(message.toJSONString());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				bw.close();
				osw.close();
				os.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
	
	public void sendToIntelligentEngine(String messageType, String owner, String srcName) {		
		
		JSONObject message = new JSONObject();
		message.put("message", messageType);
		message.put("user-id", owner);
		message.put("src-name", srcName);		
		
		Socket socket = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;		
		
		try {
			socket = new Socket(INTELLIGENT_ENGINE_IP, INTELLIGENT_ENGINE_PORT);
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			bw.write(message.toJSONString());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				bw.close();
				osw.close();
				os.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}

	public void sendToIntelligentKalman(String messageType, String owner, String planName) throws UnknownHostException, IOException {	
		
		JSONObject message = new JSONObject();
		message.put("message", messageType);
		message.put("user-id", owner);
		message.put("plan-name", planName);	
		
		Socket socket = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;		
		
		try {
			socket = new Socket(INTELLIGENT_KALMAN_IP, INTELLIGENT_KALMAN_PORT);
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			bw.write(message.toJSONString());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				bw.close();
				osw.close();
				os.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	public void sendToLoadSheddingManager(String messageType, String owner, String srcName) {		
		
		// creation, deletion
		JSONObject message = new JSONObject();
		message.put("message", messageType);
		message.put("user-id", owner);
		message.put("src-name", srcName);		
		
		Socket socket = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;		
		
		try {
			socket = new Socket(LOAD_SHEDDIING_MANAGER_IP, LOAD_SHEDDIING_MANAGER_PORT);
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			bw.write(message.toJSONString());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				bw.close();
				osw.close();
				os.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
}
