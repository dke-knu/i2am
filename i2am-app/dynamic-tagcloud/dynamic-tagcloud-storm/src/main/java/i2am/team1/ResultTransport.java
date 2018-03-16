package i2am.team1;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.storm.trident.operation.BaseFilter;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.utils.Utils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResultTransport extends BaseFilter {
	@Override
	public void prepare(Map conf, TridentOperationContext context) {
	}

	@Override
	public boolean isKeep(TridentTuple tuple) {
		List<WordCountElement> elements = (List<WordCountElement>) tuple.get(0);
		if (elements.isEmpty())	return true;
		
		WebSocketClient wc = getWebSocket();
		wc.connect();
		while (!wc.getConnection().isOpen())	Utils.sleep(10);
		

		JSONObject jsonObj = new JSONObject();
		JSONArray outerArray = new JSONArray();
		for (WordCountElement e : elements) {
			JSONObject innerObj = new JSONObject();
			innerObj.put("word", e.hashtag).put("weight", e.count).put("keyword", e.keyword);
			outerArray.put(innerObj);
		}
		jsonObj.put("data", outerArray);

		System.err.println(jsonObj.toString());

		wc.send("T::"+jsonObj.toString());
		wc.close();
		return true;
	}

	public WebSocketClient getWebSocket() {
		WebSocketClient wc = null;
		try {
			String url = "ws://" + ":8080/dynamic-tagcloud/websocket"; // webserver ip
			wc = new WebSocketClient(new URI(url), new Draft_17()) {

				@Override
				public void onOpen(ServerHandshake handshakedata) {
					System.out.println("########" + handshakedata.getHttpStatusMessage());
				}

				@Override
				public void onMessage(String message) {
					System.out.println("#############@" + message);
				}

				@Override
				public void onError(Exception ex) {
					System.err.println(ex.getMessage());
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					System.err.println(reason);
				}
			};
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return wc;
	}
}
