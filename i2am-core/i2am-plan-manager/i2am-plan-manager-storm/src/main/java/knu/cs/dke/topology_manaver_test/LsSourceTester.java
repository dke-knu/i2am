package knu.cs.dke.topology_manaver_test;

import i2am.plan.manager.kafka.I2AMProducer;

public class LsSourceTester {

	public static void main(String[] args) throws InterruptedException {
		I2AMProducer i2AMProducer1 = new I2AMProducer("hajinkim@kangwon.ac.kr","hajin_src1");
		I2AMProducer i2AMProducer2 = new I2AMProducer("hajinkim@kangwon.ac.kr","hajin_src2");
		I2AMProducer i2AMProducer3 = new I2AMProducer("hajinkim@kangwon.ac.kr","hajin_src3");

		String msg1 = "";
		String msg2 = "";
		String msg3 = "";

		for(int i=0; i<700; i++){
			msg1 += Math.random()+",";
		}

		for(int i=0; i<1000; i++){
			msg2 += Math.random()+",";
		}

		for(int i=0; i<1500; i++){
			msg3 += Math.random()+",";
		}
		long i =0;

		while (true) {
			i2AMProducer1.send(msg1);
			i2AMProducer2.send(msg2 , i);
			i2AMProducer3.send(msg3 , i++);

			Thread.sleep(1);
		}

	}
}
