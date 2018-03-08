import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

class SwitchMod {
	private boolean changeSwitch = true;

	public boolean getSwitch(){
		return changeSwitch;
	}

	public void setSwitch(boolean changeSwitch){
		this.changeSwitch = changeSwitch;
	}
}

public class I2AMProducer {
	private String brokers;
	private String topic;

	private Producer<String, String> producer;

	private SwitchMod mod;

	public I2AMProducer(String id, String srcName) {
		this.brokers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,"
				+ "114.70.235.43:19095,114.70.235.43:19096,114.70.235.43:19097,"
				+ "114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";
		this.topic = getInputTopic(id, srcName);

		Properties props = new Properties();
		props.put("metadata.broker.list", this.brokers);
		props.put("serializer.class", "kafka.serializer.StringEncoder");

		ProducerConfig producerConfig = new ProducerConfig(props);
		producer = new Producer<String, String>(producerConfig);

		// for load-shedding
		mod = new SwitchMod();
		Thread thread = new Thread(new DBThread(mod, topic));
		thread.start();
	}

	public void send(String message) {
		if (this.topic == null) {
			throw new NullPointerException("Source is not available.");
		}
		// for load-shedding
		if(mod.getSwitch()) {
			producer.send(new KeyedMessage<String, String>(this.topic, message));
		}
	}

	private String getInputTopic(String id, String srcName) {
		return getDbInstance().getInputTopic(id, srcName);
	}

	private DbAdapter getDbInstance() {
		return DbAdapter.getInstance();
	}
}

// for load-shedding
class DBThread implements Runnable {

	private String topic;
	private SwitchMod mod;	

	public DBThread(SwitchMod mod, String topic) {
		this.topic = topic;
		this.mod = mod;
	}

	@Override
	public void run() {
		while(true){ 
			mod.setSwitch(getSwtichValue(topic));
			
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean getSwtichValue(String topic) {
		return getDbInstance().getSwtichValue(topic);
	}

	private DbAdapter getDbInstance() {
		return DbAdapter.getInstance();
	}
}
