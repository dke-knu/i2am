package i2am.plan.manager.kafka.examples;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import i2am.plan.manager.kafka.I2AMConsumer;

public class ConsumerExample {

	public static void main(String[] args) {
		
		Queue<String> q = new LinkedBlockingQueue<String>(100);
		
		new I2AMConsumer("alice@gmail.com", "TimeSeriesDestination").receive(q);

		while (true) {
			String message;
			do {
				message = q.poll();
			} while(message == null);
			System.out.println( message );
		}
	}
}
