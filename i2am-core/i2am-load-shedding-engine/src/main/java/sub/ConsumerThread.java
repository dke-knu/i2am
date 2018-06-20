package sub;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class ConsumerThread implements Runnable {
    private Properties config = new Properties();
    private String topicName;

    public ConsumerThread(String topicName){
        this.topicName = topicName;
        config.put("bootstrap.servers", "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092");
        //config.put("session.timeout.ms", "10000");
        config.put("group.id", "test-consumer-group");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }

    public void run() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(config);
        consumer.subscribe(Arrays.asList(topicName));
        System.out.println("[컨수머시작]: "+topicName);
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(100*2);
            for (ConsumerRecord<String, String> record : records) {
                if (record.topic().equals(topicName)) {
                } else {
                    System.out.println("[컨수머에러");
                    throw new IllegalStateException("get message on topic " + record.topic());
                }
            }
        }
    }
}
