package org.i2am.load.shedding.engine;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class MessageReceiver implements Runnable {
    private String message;
    private Map<String, Boolean> jmxTopics;

    private String hostname;
    private int port;

    public MessageReceiver(Map jmxTopics, Map conf) throws IOException {
        this.jmxTopics = jmxTopics;
        this.hostname = (String) conf.get("hostname");
        this.port = Integer.parseInt((String) conf.get("port"));
    }

    public void run() {
        int i = 0;
        byte[] bytes = null;
        String topic = null;

        //socket
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(hostname, port));

            while (true) {
                System.out.println("[연결 기다림]");
                Socket socket = serverSocket.accept();
                System.out.println("[연결 수락함]");

                bytes = new byte[100];
                InputStream is = socket.getInputStream();
                int readByteCount = is.read(bytes);
                message = new String(bytes, 0, readByteCount, "UTF-8");
                System.out.println("[메시지받음] " + message);

                //json message parsing - type --> if/else if
                //ParsingThread ps = new ParsingThread(message, jmxTopics); --> Creation and Deletion 까지 수행
                new Thread(new ParsingThread(message, jmxTopics)).start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    class ParsingThread implements Runnable {
        private String jsonStr;
        private Map<String, Boolean> map;

        public ParsingThread(String jsonStr, Map map) {
            this.jsonStr = jsonStr;
            this.map = map;
        }

//        @Override
        public void run() {
            // parsing
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonStr);

                String decision = (String) jsonObject.get("decision");
                String topic = (String) jsonObject.get("topic");

                if (decision.equals("creation")) {
                    map.put(topic, false);
                } else if (decision.equals("deletion")) {
                    map.remove(topic);
                }

            } catch (Exception e) {
            }
        }
    }
}
