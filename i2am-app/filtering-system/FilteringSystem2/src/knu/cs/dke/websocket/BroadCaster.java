package knu.cs.dke.websocket;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//
//import org.java_websocket.WebSocket;
//import org.java_websocket.server.WebSocketServer;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import knu.cs.dke.prog.StreamTwitterInput;
import knu.cs.dke.prog.esper.EsperEngine;
import knu.cs.dke.prog.util.Constant;

@ServerEndpoint("/user/broadcasting_web")
public class BroadCaster {
	private static Set<Session> clients = Collections
			.synchronizedSet(new HashSet<Session>());

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		System.out.println("!!!!!!!!broadCast "+message);
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
	public void onOpen(Session session) throws Exception {
		// Add session to the connected sessions set
		System.out.println(session);
		clients.add(session);
		Constant.UserSession = session;
		Constant.BroadCaster = this;
		//multi Esper 해결위한.. (하지만 안됨...)
		Constant.EsperEngine = new EsperEngine();
		Constant.EsperEngine.start();
	}

	@SuppressWarnings("static-access")
	@OnClose
	public void onClose(Session session) {
		// Remove session from the connected sessions set
		clients.remove(session);
		clients.clear();
		if(Constant.InputType.equals("input_api")){
			if(Constant.Dataset.equals("Twitter")){
				//twitter
				Constant.StreamTwitterInput._twitterStream.shutdown();
				Constant.StreamTwitterInput._twitterStream.clearListeners();
				Constant.StreamTwitterInput = null;
			} else{
				//gaussian
			}
			
		}
		System.out.println("close....!!");
		Constant.UserSession = null;
	}
    
}

