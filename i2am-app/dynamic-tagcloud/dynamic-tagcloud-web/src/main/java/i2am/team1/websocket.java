package i2am.team1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@ServerEndpoint("/websocket")
public class websocket {
private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	private DBConnection dbcon = null;
	/*
	 * when message(including socket open message) received
	 */
	
	private String TridentSessionId;
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		dbcon = new DBConnection();
		String m[] = message.split("::");
		if(m[0].equals("CLIENT")){
			dbcon.insertKeywordsAndSession(m[1], session.getId()); //connection close
		} else {
			TridentSessionId = session.getId();
			
			ArrayList<String> allSessions = dbcon.selectAllSessions();
			
			if(allSessions.size()>0){
				
				JSONObject[] data = new JSONObject[allSessions.size()];
				for(int i=0, length=data.length;i<length;++i)
					data[i] = new JSONObject();
				
				String[] selectedSessions = null;
				
				JSONParser parser = new JSONParser();
				try {
					Object obj = parser.parse(m[1]);
					JSONObject jobj = (JSONObject) obj;
//					System.out.println(jobj);
					
					JSONArray jarr = (JSONArray) jobj.get("data");
					Iterator<Object> iterator = jarr.iterator();
					int idx;
					while(iterator.hasNext()) {
						JSONObject jobj2 = (JSONObject)parser.parse(iterator.next().toString());
						
						selectedSessions = dbcon.selectSession(jobj2.get("keyword").toString());
						for(String s: selectedSessions) {
							idx = allSessions.indexOf(s);
							data[idx].put(jobj2.get("word"), jobj2.get("weight"));
						}
					}
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				synchronized(clients) {
					int i;
					for(String client: allSessions) {
//						System.err.println(client);
						i = allSessions.indexOf(client);
						if(client.equals(TridentSessionId)) continue;
//						System.err.println(i+","+data.length);
						if(!data[i].isEmpty()) {
							for(Session s: clients){
								if(s.getId().equals(client)){
									s.getBasicRemote().sendText(data[i].toString());
									break;
								}
							}
						}
					}
				}
			}
		}
		dbcon.close();
	}
	
	@OnOpen
	public void onOpen(Session session) {
//		System.out.println("open: "+session.getId());
		clients.add(session);
	}
	
	@OnClose
	public void onClose(Session session) {
//		clients.remove("close: "+session.getId());
		if(!session.getId().equals(TridentSessionId)){
			dbcon = new DBConnection();
			dbcon.deleteSession(session.getId());
			dbcon.close();
		}
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
//		System.out.println("error: "+session.getId());
		t.printStackTrace();
	}
}
