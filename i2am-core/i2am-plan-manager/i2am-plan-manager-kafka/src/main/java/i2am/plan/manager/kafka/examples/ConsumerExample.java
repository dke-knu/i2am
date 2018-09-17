package i2am.plan.manager.kafka.examples;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import i2am.plan.manager.kafka.I2AMConsumer;

public class ConsumerExample {

	public static void main(String[] args) throws IOException {
		
		Queue<String> q = new LinkedBlockingQueue<String>(100);
		
		new I2AMConsumer("hajinkim@kangwon.ac.kr", "hajin_dst").receive(q);

		while (true) {
			String message;
			do {
				message = q.poll();
			} while(message == null);
			System.out.println( message );
		}
	}
}
