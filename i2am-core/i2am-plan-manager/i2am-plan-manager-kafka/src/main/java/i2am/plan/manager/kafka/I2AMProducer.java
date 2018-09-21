package i2am.plan.manager.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

class SwitchMod {
    private boolean changeSwitch = false;

    public boolean getSwitch() {
        return changeSwitch;
    }

    public void setSwitch(boolean changeSwitch) {
        this.changeSwitch = changeSwitch;
    }
}

public class I2AMProducer {
    private String brokers;
    private String topic;
    private String srcName;
    private String id;

    private double ratio = 0.01;

    private KafkaProducer<String, String> producer;

    private SwitchMod mod;

    public I2AMProducer(String id, String srcName) {
        this.brokers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
                + "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";

        this.topic = getInputTopic(id, srcName);
        this.srcName = srcName;
        this.id=id;

        Properties props = new Properties();
        props.put("bootstrap.servers", this.brokers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<String, String>(props);
        // for load-shedding
        this.mod = new SwitchMod();
        Thread thread = new Thread(new DBThread(this.mod, this.topic));
        thread.start();
    }

    public void close() {
        producer.close();
    }

    public void send(String message, long no) throws InterruptedException {
        if (this.topic == null) {
            throw new NullPointerException("Source is not available.");
        }
        // mod.getSwtich -- false -> LS off , true -> LS on
        if (!mod.getSwitch()) {
            producer.send(new ProducerRecord<String, String>(this.topic,
                    message + "," + System.currentTimeMillis() + "," + this.srcName + "," + this.id + "," + no));
//			System.out.println("["+cnt+"][Sending] "+message+","+System.currentTimeMillis()+","+this.srcName);
        } else {
            if (Math.random() < ratio) {
                producer.send(new ProducerRecord<String, String>(this.topic,
                        message + "," + System.currentTimeMillis() + "," + this.srcName + "," + this.id + "," + no));
//				System.out.println("["+cnt+"][Sending] "+message+","+System.currentTimeMillis()+","+this.srcName);
            }
        }
    }

    public void send(String message) throws InterruptedException {
        if (this.topic == null) {
            throw new NullPointerException("Source is not available.");
        }
        // mod.getSwtich -- false -> LS off , true -> LS on
        if (!mod.getSwitch()) {
            producer.send(new ProducerRecord<String, String>(this.topic,message + "," + System.currentTimeMillis() + "," + this.srcName+","+this.id));
        } else {
            if (Math.random() <= ratio) {
                producer.send(new ProducerRecord<String, String>(this.topic,
                        message + "," + System.currentTimeMillis() + "," + this.srcName+","+this.id));
            }
        }
    }

    private String getInputTopic(String id, String srcName) {
        return getDbInstance().getInputTopic(id, srcName);
    }

    private String getSrcIdx(String id, String srcName) {
        return getDbInstance().getSrcIdx(id, srcName);
    }

    private DbAdapter getDbInstance() {
        return DbAdapter.getInstance();
    }
}

// for load-shedding
class DBThread implements Runnable {

    private String topic;
    private SwitchMod mod;

    public DBThread(SwitchMod mod, String topic) {
        this.topic = topic;
        this.mod = mod;
    }

    @Override
    public void run() {
        while (true) {
            this.mod.setSwitch(getSwtichValue(this.topic));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //수정하기
    private boolean getSwtichValue(String topic) {
//		boolean res = getDbInstance().getSwtichValue(topic);
//		System.out.println("[DB Check]["+topic+"]"+res);
//		return res;
        return getDbInstance().getSwtichValue(topic);
    }

    private DbAdapter getDbInstance() {
        return DbAdapter.getInstance();
    }

}
