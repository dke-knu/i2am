package i2am.plan.manager.kafka.examples;

import i2am.plan.manager.kafka.I2AMProducer;

public class RandomTimeSeries {
	final static double initial = 1;

	public static void main(String[] args) {
		I2AMProducer producer = new I2AMProducer("abc@naver.com", "SRC1");
		
		double current = initial;
		while (true) {
			System.out.println(current);
//			producer.send(String.valueOf(current));
			
			current = nextRand(current);
		}
	}
	
	public static double nextRand(double previous) {
		return previous + (Math.random() * 2 - 1);
	}

}
