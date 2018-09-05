package sub;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerExample {
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        config.put("bootstrap.servers", "192.168.56.100:9092,192.168.56.101:9092,192.168.56.102:9092");
        config.put("group.id", "test-consumer-group");
        config.put("session.timeout.ms", "10000");
        config.put("zookeeper.connect", "192.168.56.100:2181,192.168.56.101:2181,192.168.56.102:2181");
        config.put("auto.commit.interval.ms", "1000");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");


        // planID 받아오는 코드 필요함
        String planId = "topic1";

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(config);
        consumer.subscribe(Arrays.asList(planId,"topic2","topic3"));

        Socket socket = null;
        String message = null;

        int cnt = -1;
        int interval = 100;

        try {
            socket = new Socket();
            System.out.println("[연결 요청]");
            socket.connect(new InetSocketAddress("localhost", 5005));
            System.out.println("[연결 성공]");
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                long rTime = System.currentTimeMillis();

                for (ConsumerRecord<String, String> record : records) {

                    // 사용자 정의 destination 으로 msg 보내기 - 여기서는 출력함
                    //System.out.println(record.value().split(",")[0]);

                    // interval 간격으로 LSM에게 메시지 보냄
                    if (++cnt % interval == 0) {
                        message = record.value().split(",")[1] +","+ rTime + "," + record.topic();
                        os.writeUTF(message);
                        os.flush();
                        System.out.println("[데이터 보내기 성공]"+message);
                        cnt = 0;
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            consumer.close();
            socket.close();
        }
    }
}