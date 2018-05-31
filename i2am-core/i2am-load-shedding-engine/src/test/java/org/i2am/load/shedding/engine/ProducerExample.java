package org.i2am.load.shedding.engine;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Properties;

public class ProducerExample {

    public static void main(String[] args) throws IOException {
        Properties configs = new Properties();
        configs.put("bootstrap.servers", "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092");
        configs.put("acks", "all");
        configs.put("block.on.buffer.full", "true");
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(configs);

//        for (int i = 0; i < 5; i++) {
        while(true){
           // System.out.println("11보냄");
            producer.send(new ProducerRecord<String, String>("topic1", "hello"));
            producer.send(new ProducerRecord<String, String>("topic2", "hello"));

            System.out.println("보냄");
        }
//        producer.flush();
//        producer.close();
    }
}
