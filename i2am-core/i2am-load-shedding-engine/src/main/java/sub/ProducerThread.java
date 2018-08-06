package sub;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

class SwitchMod {
    private boolean dbSwitch = false;

    public boolean getDbSwitch() {
        return dbSwitch;
    }

    public void setDbSwitch(boolean dbSwitch) {
        this.dbSwitch = dbSwitch;
    }
}

class DBThread implements Runnable {
    private String topic;
    private SwitchMod mod;

    public DBThread(SwitchMod mod, String topic) {
        this.topic = topic;
        this.mod = mod;
    }

//    @Override
    public void run() {
        while (true) {
            mod.setDbSwitch(getSwitchValue(topic));
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean getSwitchValue(String topic){
        return getDbInstance().getSwtichValue(topic);
    }
    private DbAdapter getDbInstance(){
        return DbAdapter.getInstance();
    }

}

public class ProducerThread implements Runnable {
    private Properties config = new Properties();
    private String topicName;
    private SwitchMod mod;
    private KafkaProducer<String, String> producer = null;

    //생성자
    public ProducerThread(String topicName) {
        this.topicName = topicName;
        config.put("bootstrap.servers","192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092");
        config.put("acks", "all");
        config.put("block.on.buffer.full", "true");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<String, String>(config);
        //for load-shedding
        mod = new SwitchMod();
        Thread thread = new Thread(new DBThread(mod, topicName));
        thread.start();
    }

    public void send(String message) {
        if (this.topicName == null) {
            throw new NullPointerException("Source is not available");
        }
        //for load-shedding
        //switch == false -> longshedding off & switch == true -> load shedding on
        if (!mod.getDbSwitch()) {
            producer.send(new ProducerRecord<String, String>(this.topicName, message));
        }
    }

    public void run() {
        int i = 0;
        byte[] bytes = null;
        String message = "hello";

        System.out.println("[프로듀서시작]");

        while (true) {
            send(message);
        }
    }
}
