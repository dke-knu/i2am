package sub;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

//import java.util.Properties;

public class ConsumerExample {
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();

//        config.put("bootstrap.servers", "192.168.56.100:9092");
        config.put("bootstrap.servers", "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092");
        config.put("group.id", "test-consumer-group");
        config.put("session.timeout.ms", "10000");
        config.put("zookeeper.connect", "192.168.56.100:2181,192.168.56.101:2181,192.168.56.102:2181");
        config.put("auto.commit.interval.ms", "1000");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(config);
        consumer.subscribe(Arrays.asList("topic1", "topic2", "topic3", "topic4"));
//        consumer.subscribe(Arrays.asList("topic1", "topic2"));

        ConsumerRecords<String, String> records = consumer.poll(1000);

        while (true) {
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record.timestamp() + " | topic: " + record.topic() + ", value: " + record.value());
            }
            records = consumer.poll(1000);
        }
    }
}