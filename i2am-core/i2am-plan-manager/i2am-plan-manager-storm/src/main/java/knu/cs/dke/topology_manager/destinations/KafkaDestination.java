package knu.cs.dke.topology_manager.destinations;

import java.util.Properties;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import i2am.plan.manager.kafka.I2AMConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaDestination extends Destination {

    private String topic;
    private String zookeeperIp;
    private String zookeeperPort;

    public KafkaDestination(String destinationName, String createdTime, String owner, String dstType,
                            String zookeeperIp, String zookeeperPort, String topic) {

        super(destinationName, createdTime, owner, dstType);

        this.zookeeperIp = zookeeperIp;
        this.zookeeperPort = zookeeperPort;
        this.topic = topic;
    }

    @Override
    public void run() {

        // Consumer: Read from User's Source
        // Needed Parameters: server IP&Port, topic name ...
        String read_server = "114.70.235.43:19092";

        String read_servers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
                + "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";

//		String read_topics = super.getTransTopic();
        String groupId = UUID.randomUUID().toString(); // Offset을 초기화 하려면 새로운 이름을 줘야한다. 걍 랜덤!

        // Consumer Props
        Properties consume_props = new Properties();
        consume_props.put("bootstrap.servers", read_servers);
        consume_props.put("group.id", groupId);
        consume_props.put("enable.auto.commit", "true");
        consume_props.put("auto.offset.reset", "earliest");
        consume_props.put("auto.commit.interval.ms", "1000");
        consume_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consume_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // Producer: Server Kafka --> User's
        String write_server = zookeeperIp + ":" + zookeeperPort;
        String write_topic = this.topic;

        Properties produce_props = new Properties();
        produce_props.put("bootstrap.servers", write_server);
        produce_props.put("acks", "all");
        produce_props.put("retries", 0);
        produce_props.put("batch.size", 16384);
        produce_props.put("linger.ms", 1);
        produce_props.put("buffer.memory", 33554432);
        produce_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        produce_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        ////////////////////
        //* Read & Write *///
        //////////////////////
        /////////////////////

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(produce_props);
        I2AMConsumer consumer = new I2AMConsumer(super.getOwner(), super.getDestinationName());
        try {
            // Consume.
            Queue<String> q = new LinkedBlockingQueue<String>(100);
            consumer.receive(q);
            while (true) {
                String message;
                while (!q.isEmpty()) {
                    message = q.poll();
                    System.out.println("[KafkaDestination Msg]"+message);
                    producer.send(new ProducerRecord<String, String>(write_topic, message));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
            producer.close();
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getZookeeperIp() {
        return zookeeperIp;
    }

    public void setZookeeperIp(String zookeeperIp) {
        this.zookeeperIp = zookeeperIp;
    }

    public String getZookeeperPort() {
        return zookeeperPort;
    }

    public void setZookeeperPort(String zookeeperPort) {
        this.zookeeperPort = zookeeperPort;
    }

}
