package knu.cs.dke.topology_manager.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager.DestinationList;
import knu.cs.dke.topology_manager.Plan;
import knu.cs.dke.topology_manager.PlanList;
import knu.cs.dke.topology_manager.SourceList;
import knu.cs.dke.topology_manager.topolgoies.ASamplingFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.BinaryBernoulliSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.BloomFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.HashSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.KSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.KalmanFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.NRKalmanFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.PrioritySamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.QueryFilteringTopology;
import knu.cs.dke.topology_manager.topolgoies.ReservoirSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.SystematicSamplingTopology;
import knu.cs.dke.topology_manager.topolgoies.UCKSamplingTopology;


public class TopologyHandler {

	private JSONObject command;
	private PlanList plans;
	private SourceList sources;
	private DestinationList destinations;	

	public TopologyHandler(String command, PlanList plans, SourceList sources, DestinationList destinations) throws ParseException {		

		JSONParser parser = new JSONParser();
		this.command = (JSONObject) parser.parse(command);		
		this.plans = plans;
		this.sources = sources; 
		this.destinations = destinations;		
	}

	public void excute() throws ParseException, NotAliveException, AuthorizationException, TException, InterruptedException, IOException {		

		System.out.println("[Topology Handler] new Commands");
		String commandType = (String) command.get("commandType");
		System.out.println("[Topology Handler] Command Type: " + commandType);		

		switch (commandType) {

		case "CREATE_PLAN":
			this.createPlan();
			break;

		case "DESTROY_PLAN":			
			this.destroyPlan();
			break;

		case "CHANGE_STATUS_OF_PLAN":			
			this.changeStatus();
			break;

		default:
			System.out.println("[Topology Handler] Command is not exist.");
			break;			
		}		
	}

	private void createPlan() throws TTransportException {		

		// Parse Content!
		JSONObject content = (JSONObject) command.get("commandContent");		

		String planName = (String) content.get("planName");
		String createdTime = (String) content.get("createdTime");
		String owner = (String) content.get("owner");

		String source = (String) content.get("srcName");
		String destination = (String) content.get("dstName");

		String srcTopic = sources.get(source).getTransTopic();
		String dstTopic = destinations.get(destination).getTransTopic();

		// Algorithms
		JSONArray algorithms = (JSONArray) content.get("algorithms");

		List<ASamplingFilteringTopology> topologies = new ArrayList<ASamplingFilteringTopology>();

		int algorithmsSize = algorithms.size();

		for ( int i=0; i<algorithmsSize; i++ ) {

			JSONObject algorithm = (JSONObject) algorithms.get(i);			
			String algorithmType = (String) algorithm.get("algorithmType");

			ASamplingFilteringTopology temp = null;

			switch(algorithmType) {			

			case "BINARY_BERNOULLI_SAMPLING":
				JSONObject bb_params = (JSONObject) algorithm.get("algorithmParams");				
				int bb_sampleSize = ((Number) bb_params.get("sampleSize")).intValue();
				int bb_windowSize = ((Number) bb_params.get("windowSize")).intValue();
				temp = new BinaryBernoulliSamplingTopology(createdTime, planName, i, "BINARY_BERNOULLI_SAMPLING", bb_sampleSize, bb_windowSize);				
				break;

			case "HASH_SAMPLING":
				JSONObject hash_params = (JSONObject) algorithm.get("algorithmParams");
				int hash_sampleSize = ((Number) hash_params.get("sampleRatio")).intValue();
				int hash_windowSize = ((Number) hash_params.get("windowSize")).intValue();
				String hash_function = (String) hash_params.get("hashFunction");
				// int bucket_size = ((Number) hash_params.get("bucketsize")).intValue();
				temp = new HashSamplingTopology(createdTime, planName, i, "HASH_SAMPLING", hash_sampleSize, hash_windowSize, hash_function);
				break;				

			case "PRIORITY_SAMPLING":
				JSONObject priority_params = (JSONObject) algorithm.get("algorithmParams");
				int priority_sampleSize = ((Number) priority_params.get("sampleSize")).intValue();
				int priority_windowSize = ((Number) priority_params.get("windowSize")).intValue();	
				temp = new PrioritySamplingTopology(createdTime, planName, i, "PRIORITY_SAMPLING", priority_sampleSize, priority_windowSize);
				break;

			case "RESERVOIR_SAMPLING":
				JSONObject reservoir_params = (JSONObject) algorithm.get("algorithmParams");
				int reservoir_sampleSize = ((Number) reservoir_params.get("sampleSize")).intValue();
				int reservoir_windowSize = ((Number) reservoir_params.get("windowSize")).intValue();
				temp = new ReservoirSamplingTopology(createdTime, planName, i, "RESERVOIR_SAMPLING", reservoir_sampleSize, reservoir_windowSize);				
				break;		

			case "SYSTEMATIC_SAMPLING":
				JSONObject systematic_params = (JSONObject) algorithm.get("algorithmParams");	
				int interval = ((Number) systematic_params.get("interval")).intValue();												
				temp = new SystematicSamplingTopology(createdTime, planName, i, "SYSTEMATIC_SAMPLING", interval);				
				break;		

			case "K_SAMPLING":
				JSONObject k_params = (JSONObject) algorithm.get("algorithmParams");
				int k_sampling_rate = ((Number) k_params.get("samplingRate")).intValue();
				temp = new KSamplingTopology(createdTime, planName, i, "K_SAMPLING", k_sampling_rate);
				break;

			case "QUERY_FILTERING":
				JSONObject query_params = (JSONObject) algorithm.get("algorithmParams");
				String query_keywords = (String) query_params.get("keywords");
				temp = new QueryFilteringTopology(createdTime, planName, i, "QUERY_FILTERING", query_keywords);								
				break;

			case "BLOOM_FILTERING":
				JSONObject bloom_params = (JSONObject) algorithm.get("algorithmParams");
				int bloom_size = ((Number) bloom_params.get("bucketSize")).intValue();
				String bloom_keywords = (String) bloom_params.get("keywords");
				temp = new BloomFilteringTopology(createdTime, planName, i, "BLOOM_FILTERING", bloom_size, bloom_keywords);
				break;

			case "KALMAN_FILTERING":				
				JSONObject kalman_params = (JSONObject) algorithm.get("algorithmParams");
				double qValue = ((Number) kalman_params.get("qVal")).doubleValue();
				double rValue = ((Number) kalman_params.get("rVal")).doubleValue();				
				temp = new KalmanFilteringTopology(createdTime, planName, i, "KALMAN_FILTERING", qValue, rValue);
				break;

			case "NOISE_RECOMMEND_KALMAN_FILTERING":
				JSONObject nr_kalman_params = (JSONObject) algorithm.get("algorithmParams");
				double nr_qValue = ((Number) nr_kalman_params.get("qVal")).doubleValue();								
				temp = new NRKalmanFilteringTopology(createdTime, planName, i, "NR_KALMAN_FILTERING", nr_qValue);
				break;			

			case "UC_K_SAMPLING":
				JSONObject uc_k_params = (JSONObject) algorithm.get("algorithmParams");
				int uc_k_sampleRate = ((Number) uc_k_params.get("samplingRate")).intValue();
				double uc = ((Number) uc_k_params.get("ucUnderBound")).doubleValue();
				temp = new UCKSamplingTopology(createdTime, planName, i, "UC_K_SAMPLING", uc_k_sampleRate, uc);
				break;			

			default:
				System.out.println("[Topology Hander] Algoritm Type Error");
				break;			
			}	

			if( i == 0 )
			{ 
				// 첫 토폴로지: Source Trans Topic
				temp.setInputTopic(srcTopic);				
			} 
			else
			{
				// 중간의 그 어딘가...
				temp.setInputTopic(topologies.get(i-1).getOutputTopic());
			}

			if ( i == algorithmsSize-1 ) { // 마지막 토폴로지: Destination Trans Topic
				temp.setOutputTopic(dstTopic);
			}

			topologies.add(temp);
		}

		// Created Plan!
		Plan plan = new Plan(planName, createdTime, "DEACTIVE", owner, source, destination);
		plan.setTopologies(topologies);		

		plans.add(plan);

		// Plan to DB ...
		DbAdapter db = new DbAdapter();
		db.addPlan(plan);

		// Topology Params to Redis ...
		RedisAdapter redis = new RedisAdapter();
		redis.addPlanParams(plan);	

	}

	private void destroyPlan() throws NotAliveException, AuthorizationException, TException, InterruptedException {

		// Parse Content!
	
		/*
		Plan temp = plans.get();
		temp.killTopologies();		
		plans.remove(temp);

		// DB 에서도 삭제 해야되에에에엥
		DbAdapter db = new DbAdapter();
		db.changePlanStatus(temp);
		// 1. Topology Params 삭제 
		// 2. Topology 삭제
		// 3. Plan 삭제		 
		// 4. ..... Redis에서도 삭제
		 */
	}

	private void changeStatus() throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {

		// Command Content.
		JSONObject content = (JSONObject) command.get("commandContent");

		// Modified Content.
		String plan = (String) content.get("planName");
		String status = (String) content.get("after");
		String modifiedTime = (String) content.get("modifiedTime");

		// Modified Plan.
		Plan temp = plans.get(plan); // 해당 플랜
		temp.setStatus(status);
		temp.setModifiedTime(modifiedTime);

		// Plan Update
		plans.set(temp);

		System.out.println("[Topology Handler] Change Status...");
		System.out.println(temp.getPlanName());
		System.out.println(temp.getStatus());
		System.out.print("Ignore?");

		// DB에서도 해당 플랜의 상태와 ModifiedTime 변경
		DbAdapter db = new DbAdapter();
		db.changePlanStatus(temp);

		// 만약 상태가 active로 바뀐다면!
		if ( status.equals("ACTIVE") ) {			
			if (!temp.isSubmitted()) {
				temp.submitTopologies();
			}
			else {
				temp.activateTopologies();
			}			
		} 
		else if ( status.equals("DEACTIVE") ) {
			temp.deactivateTopologies();
		}
		else
			System.out.println("[Topology Handler] Status Type Error.");		
	}


}
