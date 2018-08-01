package org.i2am.load.shedding.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadSheddingManager {
    public static Map<String, Boolean> jmxTopics = Collections.synchronizedMap(new HashMap<String, Boolean>());
    private static Map<String, String> conf = new HashMap<String, String>();
    public Map<String, lsInfo> varMap = Collections.synchronizedMap(new HashMap<String, lsInfo>());

    // 생성자
    public LoadSheddingManager(Map conf) throws IOException {
        this.conf = conf;
    }

    // 초기화 - DB에서 플랜(토픽)정보 읽어서 jmxTopics 맵에 저장
    public void initJmxTopics() {
        DbAdapter.getInstance(conf).initMethod(jmxTopics);
    }

    // 각 플랜(토픽)별 로드쉐딩 조건 체크하여 jmxTopics 값 변경
    public void loadSheddingCheck(double threshold, double var, String planId) {
        if (var > threshold && !jmxTopics.get(planId)) {
            System.out.println("[LOADSHEDDING ON!]");
            DbAdapter.getInstance(conf).setSwicthValue(planId, "true");
            jmxTopics.put(planId, true);
        }
        if (var < threshold && jmxTopics.get(planId)) {
            System.out.println("[LOADSHEDDING OFF!]");
            DbAdapter.getInstance(conf).setSwicthValue(planId, "false");
            jmxTopics.put(planId, false);
        }
    }

    public static void main(String args[]) throws Exception {
        //configuration information
        Map<String, String> conf = new HashMap<String, String>();
        // for socket connection
        conf.put("hostname", "localhost");
        conf.put("mrPort", "5004");
        conf.put("lsmPort", "5005");
        // for DB connection
        conf.put("driverName", "org.mariadb.jdbc.Driver");
        conf.put("url", "jdbc:mariadb://localhost:3306/tutorial");
        conf.put("user", "root");
        conf.put("password", "1234");
        // for loadshedding
        conf.put("threshold", "300");
        conf.put("windowSize", "4");
        // for JMX
        //conf.put("hosts", "192.168.56.100,192.168.56.101,192.168.56.102");

        LoadSheddingManager lsm = new LoadSheddingManager(conf);

        // 초기화 후 시작
        lsm.initJmxTopics();
        lsm.start();
    }

    public void start() throws Exception {
        double threshold = Double.parseDouble(conf.get("threshold"));
        int winSize = Integer.parseInt(conf.get("windowSize"));

        MessageReceiver messageReceiver = new MessageReceiver(jmxTopics, conf);
        new Thread(messageReceiver).start();

        byte[] bytes = null;
        String message;

        String hostname = conf.get("hostname");
        int port = Integer.parseInt(conf.get("lsmPort"));

        //socket connection
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(hostname, port));

            System.out.println("[LSM 연결 기다림]");
            Socket socket = serverSocket.accept();
            System.out.println("[LSM 연결 수락함]");

            while (true) {
                bytes = new byte[100];
                InputStream is = socket.getInputStream();
                int readByteCount = is.read(bytes);
                message = new String(bytes, 0, readByteCount, "UTF-8");
                System.out.println("[LSM 메시지받음] " + message);

                //메시지 받은 후 parsingThread 동작시킴
                new Thread(new ParsingThread(message, winSize, threshold)).start();

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // 각 플랜별 이동평균 계산을 위한 클래스
    public class lsInfo {
        Queue<Long> window = new ConcurrentLinkedQueue<Long>();
        double preVar = 0.0;
        long sumJmx = 0;
    }

    public class ParsingThread implements Runnable {
        private String message;
        private String planId;
        private long time;
        private int winSize;
        private double threshold;

        public ParsingThread(String message, int winSize, double threshold) {
            this.message = message;
            this.winSize = winSize;
            this.threshold = threshold;
        }

        @Override
        public void run() {

            String[] messages = message.split(",");
            time = Long.parseLong(messages[1]) - Long.parseLong(messages[0]); //receiveTime - sendTime
            planId = messages[2];
            double curVar = 0.0;

            lsInfo tmp;

            if(!varMap.containsKey(planId)){
                varMap.put(planId,new lsInfo());
            }

            tmp = varMap.get(planId);
            tmp.window.add(time);
            tmp.sumJmx += time;

            if (tmp.window.size() <= winSize) {
                curVar = (double) tmp.sumJmx / tmp.window.size();
            } else {
                tmp.sumJmx -= tmp.window.poll();
                curVar = (double) tmp.sumJmx / winSize;
            }

            System.out.println("TAtime: "+time+", planId: "+planId+", preVar: "+tmp.preVar+", curVar: "+curVar+", sum: "+tmp.sumJmx+", var: "+(curVar-tmp.preVar));

            // loadshedding check
            loadSheddingCheck(threshold, curVar-tmp.preVar, planId);
            tmp.preVar = curVar;

        }
    }
}