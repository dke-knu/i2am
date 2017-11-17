package i2am.benchmark.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

public class ConsumerRunner implements Runnable {

	private String zookeepers;
	private String topic;
	private String groupId;
	private int runtime;

	private static final int NUM_THREADS = 1;

	public ConsumerRunner(String[] zookeepers, short zkPort, String topic, String groupId, int runtime) {
		// TODO Auto-generated constructor stub
		StringBuilder sb = new StringBuilder();
		for (String zookeeper: zookeepers) {
			sb.append(zookeeper + ":" + zkPort + ",");
		}
		this.zookeepers = sb.substring(0, sb.length()-1);
		this.topic = topic;
		this.groupId = groupId;
		this.runtime = runtime;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Properties props = new Properties();
		props.put("group.id", this.groupId);
		props.put("zookeeper.connect", this.zookeepers);
		props.put("auto.commit.interval.ms", "1000");      
		ConsumerConfig consumerConfig = new ConsumerConfig(props);

		ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConfig);
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(this.topic, NUM_THREADS);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(this.topic);

		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
		FutureTask<Performance> task = null;
		for (final KafkaStream<byte[], byte[]> stream : streams) {
			//         executor.execute(new Runnable() {
			//            @Override
			//            public void run() {
			//               int consumtion = 0;
			//               for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : stream) {
			//                  System.out.println(new String(messageAndMetadata.message())+","+System.currentTimeMillis()+","+(++consumtion));
			//               }
			//            }
			//         });
			task = new FutureTask<Performance>(
					new Callable<Performance>() {
						public Performance call() throws Exception {
							Performance p = new Performance();

							for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : stream) {

								// Old version: csv
								//                     String[] messages = new String(messageAndMetadata.message()).split(",");
								//                     String wordcount = messages[0];
								//                     int production = Integer.parseInt(messages[1]);
								//                     long createdTime = Long.parseLong(messages[2]);
								//                     long inputTime = Long.parseLong(messages[3]);
								//                     long outputTime = Long.parseLong(messages[4]);
								//                     long destroyedTime = System.currentTimeMillis();

								// New version: JSON
								JSONParser parser = new JSONParser();
								JSONObject messages = (JSONObject) parser.parse(
										new String(messageAndMetadata.message()));

								// System.out.println(messages.toJSONString());

								// Get. 커밋할려고 단 주석222
								String wordcount = ((JSONObject) messages.get("tweet")).toJSONString();                     
								int production = ((Number) messages.get("production")).intValue();
								long createdTime = ((Number) messages.get("createdTime")).longValue();
								long inputTime = ((Number) messages.get("inputTime")).longValue();
								long outputTime = ((Number) messages.get("outputTime")).longValue();
								long destroyedTime = System.currentTimeMillis();

								if (production > p.maxProduction)
									p.maxProduction = production;
								p.maxConsumption++;
								p.totalInputQueueingTime += (inputTime - createdTime);
								p.totalProcessingTime += (outputTime - inputTime);
								p.totalOutputQueueingTime += (destroyedTime - outputTime);
							}
							return p;
						}
					});
			executor.execute(task);
		}
		try {
			Thread.sleep(runtime*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		consumer.shutdown();
		executor.shutdown();

		try {
			System.out.println(task.get(5, TimeUnit.SECONDS).log());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class Performance {
		private long maxProduction = 0;
		private long maxConsumption = 0;
		private long totalInputQueueingTime = 0;
		private long totalProcessingTime = 0;
		private long totalOutputQueueingTime = 0;

		private String log() {
			return new String(
					"production\tconsumption\tinput-queueing-time\tprocessing-time\toutput-queueing-time\n" +
							+maxProduction+"\t"+maxConsumption+"\t"+totalInputQueueingTime+"\t"+totalProcessingTime+"\t"+totalOutputQueueingTime);

		}
	}
}