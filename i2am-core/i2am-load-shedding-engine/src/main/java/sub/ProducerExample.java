package sub;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Properties;

public class ProducerExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        Properties configs = new Properties();
        configs.put("bootstrap.servers", "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092");
        configs.put("acks", "all");
        configs.put("block.on.buffer.full", "true");
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(configs);
        String message = null;

        byte[] bytes = new byte[100];
        for (int j = 0; j < 500; j++) {
            long time = System.currentTimeMillis();
            message = new String(bytes) + "," + String.valueOf(time);
            producer.send(new ProducerRecord<String, String>("topic1", message));
//            producer.send(new ProducerRecord<String, String>("topic2", message));
//            producer.send(new ProducerRecord<String, String>("topic3", message));
            System.out.println(j+", 보냄");
        }
        producer.flush();
        producer.close();
    }
}
