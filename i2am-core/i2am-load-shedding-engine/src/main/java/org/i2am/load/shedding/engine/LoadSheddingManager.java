package org.i2am.load.shedding.engine;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class LoadSheddingManager {
    public static Map<String, Boolean> jmxTopics = Collections.synchronizedMap(new HashMap<String, Boolean>());
    private static Map<String, String> conf = new HashMap<String, String>();
    public Map<String, LSInfo> varMap = new HashMap<String, LSInfo>();

    // 생성자
    public LoadSheddingManager(Map conf) throws IOException {
        this.conf = conf;
    }

    public static void main(String args[]) throws Exception {

        LoadSheddingManager lsm = new LoadSheddingManager(conf);
        lsm.setConf();
        // 초기화 후 시작
        lsm.initJmxTopics();
        lsm.start();
    }

    //configuration information
    public void setConf(){
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

    public void start() throws Exception {
        double threshold = Double.parseDouble(conf.get("threshold"));
        int winSize = Integer.parseInt(conf.get("windowSize"));

        MessageReceiver messageReceiver = new MessageReceiver(jmxTopics, conf);
        new Thread(messageReceiver).start();

        byte[] bytes = null;
        String message=null;

        String hostname = conf.get("hostname");
        int port = Integer.parseInt(conf.get("lsmPort"));

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(hostname, port));

        Socket socket = null;
        try {
            System.out.println("[LSM 연결 기다림]");
            socket = serverSocket.accept();
            System.out.println("[LSM 연결 수락함]");

            while (true) {
                DataInputStream is = new DataInputStream(socket.getInputStream());
                message = is.readUTF();
                System.out.println("[LSM 메시지받음] " + message);

                calculateVar(message, winSize, threshold);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            serverSocket.close();
            socket.close();
        }
    }

    // 각 플랜별 이동평균 계산을 위한 클래스 - eoanswkfh
    public class LSInfo {
        Queue<Long> window = new LinkedList<Long>();
        double preVar = 0.0;
        long sumJmx = 0;
    }

    public double movingAverage(String planId, int winSize, long time){

        double curVar = 0.0;
        double var=0;

        if (!varMap.containsKey(planId)) {
            varMap.put(planId, new LSInfo());
        }

        LSInfo tmp = varMap.get(planId);
        tmp.window.add(time);
        tmp.sumJmx += time;

        if (tmp.window.size() <= winSize) {
            curVar = (double) tmp.sumJmx / tmp.window.size();
        } else {
            tmp.sumJmx -= tmp.window.poll();
            curVar = (double) tmp.sumJmx / winSize;
        }

//        System.out.println("TAtime: " + time + ", planId: " + planId + ", preVar: " + tmp.preVar + ", curVar: " + curVar + ", sum: " + tmp.sumJmx + ", var: " + (curVar - tmp.preVar));
        var = (curVar - tmp.preVar);
        tmp.preVar = curVar;

        return var;
    }

    public void calculateVar(String message, int winSize, double threshold) {
        String planId;
        long time;

        String[] messages = message.split(",");
        time = Long.parseLong(messages[1]) - Long.parseLong(messages[0]); //receiveTime - sendTime
        planId = messages[2];

        double var = movingAverage(planId, winSize, time);

        // loadshedding check
        loadSheddingCheck(threshold, var, planId);
    }
}