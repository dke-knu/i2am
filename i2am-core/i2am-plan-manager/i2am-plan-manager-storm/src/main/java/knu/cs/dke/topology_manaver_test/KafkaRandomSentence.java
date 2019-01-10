package knu.cs.dke.topology_manaver_test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaRandomSentence {

	public static void main(String[] args) throws InterruptedException {

		String[] sentences = new String[]{
				"She always speaks to him in a loud voice.",
				"Italy is my favorite country",
				"in fact. I plan to spend two weeks there next year.",
				"Please wait outside of the house",
				"When I was little I had a car door slammed shut on my hand. I still remember it quite vividly.",
				"The mysterious diary records the voice.",
				"Rock music approaches at high velocity.",
				"I want more detailed information.",
				"I was very proud of my nickname throughout high school but today- I couldn’t be any different to what my nickname was.",
				"Writing a list of random sentences is harder than I initially thought it would be.",
				"A song can make or ruin a person’s day if they let it get to them.",
				"Cats are good pets. for they are clean and are not noisy.",
				"A purple pig and a green donkey flew a kite in the middle of the night and ended up sunburnt.",
				"We need to rent a room for our party.",			
				"The memory we used to share is no longer coherent."
		};
		
				
		String output_topic = "7fbd4bce-8ca5-4c51-aebe-5bc3274d4cf6";		
		String groupId = UUID.randomUUID().toString(); 

		String server = "114.70.235.43:19092";

		String servers = "114.70.235.43:19092,114.70.235.43:19093,114.70.235.43:19094,114.70.235.43:19095,"
				+ "114.70.235.43:19096,114.70.235.43:19097,114.70.235.43:19098,114.70.235.43:19099,114.70.235.43:19100";	

		// Producer Props
		Properties produce_props = new Properties();
		produce_props.put("bootstrap.servers", servers);
		produce_props.put("acks", "all");
		produce_props.put("retries", 0);
		produce_props.put("batch.size", 16384);
		produce_props.put("linger.ms", 1);
		produce_props.put("buffer.memory", 33554432);
		produce_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		produce_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<>(produce_props);
	
		
		int index = 0;		
		String message = "";		
		Random random = new Random();		
		
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
		Date currentTime;
		String mTime;
		
		
		while (true) {						
			
				currentTime = new Date();
				mTime = mSimpleDateFormat.format(currentTime);
			
				message = index + "," +  mTime + "," + sentences[random.nextInt(15)] + "," + random.nextInt(100);
			
				System.out.println(message);
				producer.send(new ProducerRecord<String, String>(output_topic, message));
				Thread.sleep(100);
				
				index += 1;
		}
		

	}

}
