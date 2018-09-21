package org.i2am.load.shedding.engine;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class MessageReceiver implements Runnable {
    private Map<String, Boolean> jmxTopics;
    private Map<String, String> conf;

    private String hostname;
    private int port;

    public MessageReceiver(Map jmxTopics, Map conf) throws IOException {
        this.jmxTopics = jmxTopics;
        this.conf=conf;
        this.hostname = (String) conf.get("hostname");
        this.port = Integer.parseInt((String) conf.get("mrPort"));
    }

    public void run() {

        //socket
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(hostname, port));

            while (true) {
                System.out.println("[MR 연결 기다림]");
                Socket clientSocket = serverSocket.accept();
                System.out.println("[MR 연결 수락함]");

//                DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = br.readLine();

                System.out.println("[MR 메시지받음] " + message);

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

        public void run() {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonStr);

                String userId = (String) jsonObject.get("user_id");
                String message = (String) jsonObject.get("message");
                String srcName = (String) jsonObject.get("src-name");
                String srcId = DbAdapter.getInstance(conf).getSrcId(userId, srcName);

                if (message.equals("creation")) {
                    map.put(srcId, false);
                    System.out.println("[srcId] "+srcId+" 추가");
                } else if (message.equals("deletion")) {
                    map.remove(srcId);
                    System.out.println("[srcId] "+srcId+" 삭제");
                }

            } catch (Exception e) {
            }
        }
    }
}
