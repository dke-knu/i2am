package org.i2am.load.shedding.engine;

import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class LoadSheddingManager {
    public static Map<String, Boolean> jmxTopics = Collections.synchronizedMap(new HashMap<String, Boolean>());
    private static Map<String, String> conf = new HashMap<String, String>();
    public Map<String, LSInfo> varMap = Collections.synchronizedMap(new HashMap<String, LSInfo>());

//    FileWriter fw = new FileWriter("D:\\대학원\\11.LoadShedding\\정보처리학회논문\\실험2\\ex01_1.csv");

//    public void writeFile(String msg, String srcName) throws IOException {
//        if(srcName.equals("hajin_src1")){
//            fw1.write(msg+"\n");
//            fw1.flush();
//        }else if(srcName.equals("hajin_src2")){
//            fw2.write(msg+"\n");
//            fw2.flush();
//        }else if(srcName.equals("hajin_src3")){
//            fw3.write(msg+"\n");
//            fw3.flush();
//        }
//    }

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
    public void setConf() {
        // for socket connection
        conf.put("hostname", "localhost");
        conf.put("mrPort", "5004");
        conf.put("lsmPort", "5006");

        // 우리 시스템 DB
        conf.put("driverName", "org.mariadb.jdbc.Driver");
        conf.put("url", "jdbc:mariadb://114.70.235.43:3306/i2am");
        conf.put("user", "plan-manager");
        conf.put("password", "dke214");

        // for loadshedding
        conf.put("threshold", "5000");
        conf.put("windowSize", "4");
    }

    // 초기화 - DB에서 플랜(토픽)정보 읽어서 jmxTopics 맵에 저장
    public void initJmxTopics() {
        DbAdapter.getInstance(conf).initMethod(jmxTopics);
    }

    // 각 플랜(토픽)별 로드쉐딩 조건 체크하여 jmxTopics 값 변경
    public void loadSheddingCheck(double var, String srcName, double threshold) throws IOException {
        if (var > threshold && !jmxTopics.get(srcName)) {
            System.out.println("[LOADSHEDDING ON!]");
            DbAdapter.getInstance(conf).setSwicthValue(srcName, "Y");
            jmxTopics.put(srcName, true);
        }
        if (var <= threshold && jmxTopics.get(srcName)) {
            System.out.println("[LOADSHEDDING OFF!]");
            DbAdapter.getInstance(conf).setSwicthValue(srcName, "N");
            jmxTopics.put(srcName, false);
        }
    }

    public void start() throws Exception {
        MessageReceiver messageReceiver = new MessageReceiver(jmxTopics, conf);
        new Thread(messageReceiver).start();

        String hostname = conf.get("hostname");
        int port = Integer.parseInt(conf.get("lsmPort"));

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(hostname, port));

        while (true) {
            Socket clientSocket = null;
            try {
                System.out.println("[LSM 연결 기다림]");
                clientSocket = serverSocket.accept();
                InetAddress ia = clientSocket.getInetAddress();

                System.out.println("[LSM 연결 수락함]"+clientSocket);

                new Thread(new MsgSendingThread(clientSocket)).start();

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public class MsgSendingThread implements Runnable {
        private Socket clientSocket;

        public MsgSendingThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            boolean check = true;
            do {
                try {
                    DataInputStream is = new DataInputStream(this.clientSocket.getInputStream());
                    String message = is.readUTF();
                    check = calculateVar(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (check);
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 각 플랜별 이동평균 계산을 위한 클래스
    public class LSInfo {
        Queue<Long> window;
        double preVar;
        long sumJmx;

        public LSInfo(){
            this.preVar=0.0;
            this.sumJmx=0;
            this.window = new LinkedList<Long>();
        }
    }

    public double movingAverage(String srcName, int winSize, long time) {
        double curVar = 0.0;
        double var = 0;

        if (!varMap.containsKey(srcName)) {
            varMap.put(srcName, new LSInfo());
        }

        LSInfo tmp = varMap.get(srcName);
        tmp.window.add(time);
        tmp.sumJmx += time;

        if (tmp.window.size() <= winSize) {
            curVar = (double) tmp.sumJmx / tmp.window.size();
        } else {
            tmp.sumJmx -= tmp.window.poll();
            curVar = (double) tmp.sumJmx / winSize;
        }

//        System.out.println("TAtime: " + time + ", srcName: " + srcName + ", preVar: " + tmp.preVar + ", curVar: " + curVar + ", sum: " + tmp.sumJmx + ", var: " + (curVar - tmp.preVar));
        var = (curVar - tmp.preVar);
        tmp.preVar = curVar;

        return var;
    }

    public boolean  calculateVar(String message) throws IOException {
        String srcName;
        long time;

        int winSize = Integer.parseInt(conf.get("windowSize"));
        double threshold = Double.parseDouble(conf.get("threshold"));

        String[] messages = message.split(",");
        time = Long.parseLong(messages[1]) - Long.parseLong(messages[0]); //receiveTime - sendTime
        srcName = messages[2];

        // loadshedding check
        if(jmxTopics.containsKey(srcName)) {
            //이동평균 변화량 로드 쉐딩
//            double var = movingAverage(srcName, winSize, time);
//            loadSheddingCheck(var, srcName, threshold);

//            System.out.println("time: "+time);
            //지연시간 로드 쉐딩
            loadSheddingCheck(time, srcName, threshold);
            return true;
        } else {
            return false;
        }
    }
}