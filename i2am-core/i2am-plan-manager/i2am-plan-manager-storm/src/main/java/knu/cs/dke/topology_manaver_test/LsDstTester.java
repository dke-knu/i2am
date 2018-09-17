package knu.cs.dke.topology_manaver_test;

import i2am.plan.manager.kafka.I2AMConsumer;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class LsDstTester {

	public static void main(String[] args) throws InterruptedException, IOException {

		I2AMConsumer i2AMConsumer = new I2AMConsumer("hajinkim@kangwon.ac.kr","hajin_dst");
//		I2AMConsumer i2AMConsumer2 = new I2AMConsumer("hajinkim@kangwon.ac.kr","hajin_dst2");

		Queue<String> q = new LinkedBlockingQueue<String>(100);
		i2AMConsumer.receive(q);
//		i2AMConsumer2.receive(q);

		while (true) {
			String message;
			do {
				message = q.poll();
			} while(message == null);
		}

	}
}
