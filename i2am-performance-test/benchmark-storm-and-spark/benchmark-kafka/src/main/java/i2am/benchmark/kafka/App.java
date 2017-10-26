package i2am.benchmark.kafka;

public class App 
{
    public static void main( String[] args )
    {
    	String[] kafkas = args[0].split(","); // e.g. 192.168.56.100,192.168.56.101,192.168.56.102 
    	String[] zookeepers = args[1].split(","); // e.g. 192.168.56.100,192.168.56.101,192.168.56.102 
    	short kafkaPort = Short.parseShort(args[2]);
    	short zkPort = Short.parseShort(args[3]);
    	String topicOfProducer = args[4];
    	String topicOfConsumer = args[5];
    	int interval = Integer.parseInt(args[6]);
    	int runtime = Integer.parseInt(args[7]); 
    	
    	Thread consumer = new Thread(new ConsumerRunner(zookeepers, zkPort, topicOfConsumer, "test-group", runtime));
    	Thread producer = new Thread(new ProducerRunner(kafkas, kafkaPort, topicOfProducer, interval));
    	
    	producer.start();
    	consumer.start();
    	
		try {
			for (int i=1; i<=10; i++) {
				Thread.sleep(runtime*1000/10);
				System.out.println("TESING IN PROGRESS... " + (i*10) + "%");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	producer.interrupt();
    	producer.stop();
    	
		try {
			System.out.println("SHUTTING DOWN... ");
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
