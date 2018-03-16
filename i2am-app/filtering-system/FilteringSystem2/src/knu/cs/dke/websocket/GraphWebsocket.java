package knu.cs.dke.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import knu.cs.dke.prog.util.Constant;

@ServerEndpoint("/user/sampling_result")
public class GraphWebsocket {
	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		//System.out.println("!!!!!!!!broadCast "+message);
		if(message.equals("disconnect")){
			this.onClose(Constant.UserSession);
		}else{
		synchronized (clients) {
			for (Session client : clients) {
				client.getBasicRemote().sendText(message);
//				if (!client.equals(session)) {
//					client.getBasicRemote().sendText(message);
//				}
			}
		}
		}
	}
	
	@OnOpen
	public void onOpen(Session session) {
//		System.out.println("open: "+session.getId());
		System.out.println("open?");
		clients.add(session);
		InputData _input = new InputData();
		_input.inputData(this);
	}
	
	@OnClose
	public void onClose(Session session) {
		clients.remove("close: "+session.getId());
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
//		System.out.println("error: "+session.getId());
		t.printStackTrace();
	}
}


class InputData{
	void inputData(GraphWebsocket gWebsocket) {
		while(true){
			String result = makeJSON();
			try {
				gWebsocket.onMessage(result, Constant.UserSession);
				Thread.sleep(5000);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	private String makeJSON(){
		int n=3; // # of layers
		int m=6; // # of samples
		Random oRandom = new Random();
		
		JSONObject jsonObj = new JSONObject();
		JSONArray outerArray = new JSONArray();
	
		JSONObject jObject_result = new JSONObject();
		JSONObject jObject_origin = new JSONObject();
		for(int i=0; i<n;i++){
//			JSONObject jObject = new JSONObject();
			String data = "";
			for(int j=0; j<m;j++){
				data += (","+(oRandom.nextInt(30)+1));
			}
			data = data.replaceFirst(",", "");
			System.out.println(i+"] "+data);
			jObject_result.put("sb"+i, data);
			//outerArray.add(i, jObject);
		}
		outerArray.add(0, jObject_result);
		
		for(int i=0; i<n;i++){
//			JSONObject jObject = new JSONObject();
			String data = "";
			for(int j=0; j<m;j++){
				data += (","+(oRandom.nextInt(30)+1));
			}
			data = data.replaceFirst(",", "");
			System.out.println(i+"] "+data);
			jObject_origin.put("origin"+i, data);
			//outerArray.add(i, jObject);
		}
		outerArray.add(1, jObject_origin);
		jsonObj.put("result", outerArray);
		//String json = outerArray.toString();
		
		String json = jsonObj.toJSONString();
		System.out.println(json);
	return json;
	}
	
}
