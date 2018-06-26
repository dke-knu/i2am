package i2am.plan.manager.kafka.examples;

import i2am.plan.manager.kafka.I2AMProducer;

public class RandomTimeSeries {
	final static double initial = 1;

	public static void main(String[] args) throws InterruptedException {
		I2AMProducer producer = new I2AMProducer("alice@gmail.com", "TimeSeriesSource");
		
		double current = initial;
		while (true) {
			// System.out.println(current);
			producer.send(String.valueOf(current));			
			current = nextRand(current);
			
			Thread.sleep(100);
		}
	}
	
	public static double nextRand(double previous) {
		return previous + (Math.random() * 2 - 1);
	}

}
