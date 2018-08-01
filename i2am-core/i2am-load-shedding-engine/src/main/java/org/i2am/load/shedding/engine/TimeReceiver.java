package org.i2am.load.shedding.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TimeReceiver implements Runnable {

    public class lsInfo {
        Queue<Long> window = new LinkedList<Long>();
        double preVar = 0.0;
        long sumJmx = 0;
    }

    private String message;
    private Map<String, lsInfo> varMap;

    private String hostname;
    private int port;

    private int winSize;

    public TimeReceiver(Map conf, Map lsMap, int winSize) throws IOException {
        this.hostname = (String) conf.get("hostname");
        this.port = Integer.parseInt((String) conf.get("trPort"));
        this.winSize = winSize;
    }

    @Override
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

                new Thread(new TimeReceiver.ParsingThread(message, varMap)).start();

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public class ParsingThread implements Runnable {

        private String message;
        private String planId;
        private long time;
        private Map<String, lsInfo> varMap;

        public ParsingThread(String message, Map<String, lsInfo> varMap) {
            this.message = message;
            this.varMap = varMap;
        }

        @Override
        public void run() {

            String[] messages = message.split(",");
            time = Long.parseLong(messages[1]) - Long.parseLong(messages[0]); //receiveTime - sendTime
            planId = messages[2];
            double curVar = 0.0;
            double var = 0.0;

            lsInfo tmp = new lsInfo();
            tmp = varMap.get(planId);
            tmp.window.add(time);

            if (tmp.window.size() <= winSize) {
                tmp.sumJmx += time;
                curVar = (double) tmp.sumJmx / tmp.window.size();
            } else {
                tmp.sumJmx += time;
                tmp.sumJmx -= tmp.window.poll();
                curVar = (double) tmp.sumJmx / winSize;
            }

            var = curVar - tmp.preVar;

            tmp.preVar = curVar;


        }
    }

}
