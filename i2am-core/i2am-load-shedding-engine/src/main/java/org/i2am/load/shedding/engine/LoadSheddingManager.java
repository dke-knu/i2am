package org.i2am.load.shedding.engine;

import java.util.*;

public class LoadSheddingManager {
    //public static Map<String, Boolean> jmxTopics = new HashMap<String, Boolean>();
    public static Map<String, Boolean> jmxTopics = Collections.synchronizedMap(new HashMap<String, Boolean>());
    //for all topics loadshedding
    private static boolean totalSwitch = false;

    private static Map<String, String> conf = new HashMap<String, String>();

    public LoadSheddingManager(Map conf) {
        this.conf = conf;
    }

    // LoadShedding policy
    public static long calculateThreshold() {
        long threshold = 10000000;
        return threshold;
    }

    public void initJmxTopics() {
        DbAdapter.getInstance(conf).initMethod(jmxTopics);
    }

    public void loadSheddingByTopic() throws Exception {
        long threshold = 0;
        long currentJmx = 0;

        threshold = calculateThreshold();

        while (true) {
            JmxCollector collector = new JmxCollector(conf.get("hosts"));
            long topicThreshold = threshold / jmxTopics.size();
            Set<String> keySet = jmxTopics.keySet();
            for (String key : keySet) {
                currentJmx = collector.collectJmx(key);
                String topic = key;
                System.out.print(topic + ": " + currentJmx + " ");

                loadSheddingCheck(topicThreshold, currentJmx, jmxTopics.get(topic), topic);
            }
            System.out.println();
            Thread.sleep(1000);
        }
    }

    public void loadSheddingAllTopic() throws Exception {
        long totalcurrentJmx;
        JmxCollector collector = new JmxCollector(conf.get("hosts"));

        long threshold = calculateThreshold();

        while (true) {
            totalcurrentJmx = collector.collectJmx();
//            System.out.println("total: " + totalcurrentJmx);
            System.out.println(totalcurrentJmx);


            totalSwitch = loadSheddingCheck(threshold, totalcurrentJmx, totalSwitch);
            Thread.sleep(1000);
        }
    }

    //전체
    public boolean loadSheddingCheck(double threshold, double curJmx, boolean dbSwitch){
        if (curJmx > threshold && !dbSwitch) {
            System.out.println("[LOADSHEDDING ON!]");
            dbSwitch = true;
            DbAdapter.getInstance(conf).setSwitchValue("true");
        }
        if (curJmx < threshold && dbSwitch) {
            System.out.println("[LOADSHEDDING OFF!]");
            dbSwitch = false;
            DbAdapter.getInstance(conf).setSwitchValue("false");
        }
        return dbSwitch;
    }
    //토픽별
    public void loadSheddingCheck(double threshold, double curJmx, boolean dbSwitch, String topic){
        if (curJmx > threshold && !dbSwitch) {
            System.out.println("[LOADSHEDDING ON!]");
            DbAdapter.getInstance(conf).setSwicthValue(topic, "true");
            jmxTopics.put(topic, true);
        }
        if (curJmx < threshold && dbSwitch) {
            System.out.println("[LOADSHEDDING OFF!]");
            DbAdapter.getInstance(conf).setSwicthValue(topic, "false");
            jmxTopics.put(topic, false);
        }
    }

    public void loadSheddingVarAll(double varThreshold, int winSize) throws Exception {
        JmxCollector collector = new JmxCollector(conf.get("hosts"));
        long currentJmx = 0;
        Queue<Long> window = new LinkedList<Long>();
        double preVar = 0.0;
        double curVar = 0.0;
        double var = 0.0;
        long sumJmx = 0;

        while (true) {
            currentJmx = collector.collectJmx();
            window.add(currentJmx);
            //window.Size() 가 0인지 확인 안 해도 되는지
            if (window.size() <= winSize) {
                sumJmx += currentJmx;
                curVar = (double) sumJmx / window.size();
            } else {
                sumJmx += currentJmx;
                sumJmx -= window.poll();
                curVar = (double) sumJmx / winSize;
            }
            var = curVar - preVar;
//            System.out.println("windowSize: "+window.size()+", preVar: " + preVar + ", curVar: " + curVar + ", var: " + var);
            System.out.println(var);

            totalSwitch = loadSheddingCheck(varThreshold, var, totalSwitch);
            preVar = curVar;
            Thread.sleep(1000);
        }
    }

    public class topicInfo {
        Queue<Long> window = new LinkedList<Long>();
        double preVar = 0.0;
        long sumJmx = 0;
    }

    public void loadSheddingVarByTopic(double varThreshold, int winSize) throws Exception {
        JmxCollector collector = new JmxCollector(conf.get("hosts"));
        Map<String, topicInfo> topicMap = new HashMap<String, topicInfo>();
        Set<String> keySet = jmxTopics.keySet();

        for (String topic : keySet) {
            topicMap.put(topic, new topicInfo());
        }

        long currentJmx = 0;
        Queue<Long> window = new LinkedList<Long>();
        double curVar = 0.0;
        double var = 0.0;

        topicInfo tmp;
        while (true) {
            for (String topic : keySet) {
                currentJmx = collector.collectJmx(topic);
                tmp = topicMap.get(topic);
                tmp.window.add(currentJmx);

                //window.Size() 가 0인지 확인 안 해도 되는지
                if (tmp.window.size() <= winSize) {
                    tmp.sumJmx += currentJmx;
                    curVar = (double) tmp.sumJmx / tmp.window.size();
                } else {
                    tmp.sumJmx += currentJmx;
                    tmp.sumJmx -= tmp.window.poll();
                    curVar = (double) tmp.sumJmx / winSize;
                }

                var = curVar - tmp.preVar;

                System.out.println("[" + topic + "] winSize: " + tmp.window.size() + ", preVar: " + tmp.preVar + ", curVar: " + curVar + ", var: " + var);
                loadSheddingCheck(varThreshold, var, jmxTopics.get(topic), topic);
                tmp.preVar = curVar;
            }
            Thread.sleep(1000);
        }
    }

    public static void main(String args[]) throws Exception {
        //configuration information
        Map<String, String> conf = new HashMap<String, String>();
        // for socket connection
        conf.put("hostname", "localhost");
        conf.put("port", "5004");
        // for DB connection
        conf.put("driverName", "org.mariadb.jdbc.Driver");
        conf.put("url", "jdbc:mariadb://localhost:3306/tutorial");
        conf.put("user", "root");
        conf.put("password", "1234");
        // for JMX
        conf.put("hosts", "192.168.56.100,192.168.56.101,192.168.56.102");
//        conf.put("hosts", "192.168.56.100,192.168.56.102");

        LoadSheddingManager lsm = new LoadSheddingManager(conf);

        lsm.initJmxTopics();
        lsm.start();
    }

    public void start() throws Exception {

        MessageReceiver messageReceiver = new MessageReceiver(jmxTopics, conf);
//        JmxCollector collector = new JmxCollector(conf.get("hosts"));

        new Thread(messageReceiver).start();

//        loadSheddingByTopic();
        loadSheddingAllTopic();

        double varThreshold = 100000.0;
        int winSize = 4;
//        loadSheddingVarAll(varThreshold, winSize);
//        loadSheddingVarByTopic(varThreshold, winSize);
    }
}