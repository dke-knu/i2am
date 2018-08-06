package sub;

public class ThreadTest {
    public static void main(String args[]) {
        ConsumerThread consumerThread1 = new ConsumerThread("topic1");
        ConsumerThread consumerThread2 = new ConsumerThread("topic2");
        ConsumerThread consumerThread3 = new ConsumerThread("topic3");
        ConsumerThread consumerThread4 = new ConsumerThread("topic4");
        /*ConsumerThread consumerThread5 = new ConsumerThread("topic1");
        ConsumerThread consumerThread6 = new ConsumerThread("topic2");
        ConsumerThread consumerThread7 = new ConsumerThread("topic3");
        ConsumerThread consumerThread8 = new ConsumerThread("topic4");
        ConsumerThread consumerThread11 = new ConsumerThread("topic1");
        ConsumerThread consumerThread12 = new ConsumerThread("topic2");
        ConsumerThread consumerThread13 = new ConsumerThread("topic3");
        ConsumerThread consumerThread14 = new ConsumerThread("topic4");
        ConsumerThread consumerThread15 = new ConsumerThread("topic1");
        ConsumerThread consumerThread16 = new ConsumerThread("topic2");
        ConsumerThread consumerThread17 = new ConsumerThread("topic3");
        ConsumerThread consumerThread18 = new ConsumerThread("topic4");*/


        ProducerThread producerThread1 = new ProducerThread("topic1");
        ProducerThread producerThread2 = new ProducerThread("topic2");
        ProducerThread producerThread3 = new ProducerThread("topic3");
        ProducerThread producerThread4 = new ProducerThread("topic4");
        /*ProducerThread producerThread5 = new ProducerThread("topic1");
        ProducerThread producerThread6 = new ProducerThread("topic2");
        ProducerThread producerThread7 = new ProducerThread("topic3");
        ProducerThread producerThread8 = new ProducerThread("topic4");
        ProducerThread producerThread9 = new ProducerThread("topic1");
        ProducerThread producerThread10 = new ProducerThread("topic2");
        ProducerThread producerThread11 = new ProducerThread("topic3");
        ProducerThread producerThread12 = new ProducerThread("topic4");*/

        /*ConsumerThread consumerThread = new ConsumerThread("test1123");
        ProducerThread producerThread = new ProducerThread("test1123");*/

        new Thread(consumerThread1).start();
        new Thread(consumerThread2).start();
        new Thread(consumerThread3).start();
        new Thread(consumerThread4).start();
        /*new Thread(consumerThread5).start();
        new Thread(consumerThread6).start();
        new Thread(consumerThread7).start();
        new Thread(consumerThread8).start();*/
        /*new Thread(consumerThread11).start();
        new Thread(consumerThread12).start();
        new Thread(consumerThread13).start();
        new Thread(consumerThread14).start();
        new Thread(consumerThread15).start();
        new Thread(consumerThread16).start();
        new Thread(consumerThread17).start();
        new Thread(consumerThread18).start();*/


        try {
            //new Thread(producerThread).start();

            new Thread(producerThread1).start();
            new Thread(producerThread2).start();
            new Thread(producerThread3).start();
            new Thread(producerThread4).start();
            /*new Thread(producerThread5).start();
            new Thread(producerThread6).start();
            new Thread(producerThread7).start();
            new Thread(producerThread8).start();
            new Thread(producerThread9).start();
            new Thread(producerThread10).start();
            new Thread(producerThread11).start();
            new Thread(producerThread12).start();*/

        } catch (Exception e) {
        }
        while (true) {
        }
    }
}
