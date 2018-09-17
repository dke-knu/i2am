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
import knu.cs.dke.topology_manager.topolgoies.IKalmanFilteringTopology;
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

		System.out.println("[Topology Handler] 플랜 생성 중...!");
		// Parse Content!
		JSONObject content = (JSONObject) command.get("commandContent");		

		// Parse Plan!
		JSONObject json_plan = (JSONObject) content.get("plan");
		
		String planName = (String) json_plan.get("planName");
		String createdTime = (String) content.get("createdTime");
		String owner = (String) content.get("owner");

		String source = (String) json_plan.get("srcName");
		String destination = (String) json_plan.get("dstName");

		String srcTopic = sources.get(owner, source).getTransTopic();
		String dstTopic = destinations.get(owner, destination).getTransTopic();

		// Algorithms
		JSONArray algorithms = (JSONArray) json_plan.get("topoloiges");

		List<ASamplingFilteringTopology> topologies = new ArrayList<ASamplingFilteringTopology>();

		int algorithmsSize = algorithms.size();

		for ( int i=0; i<algorithmsSize; i++ ) {

			JSONObject algorithm = (JSONObject) algorithms.get(i);			
			String algorithmType = (String) algorithm.get("topology_type");

			ASamplingFilteringTopology temp = null;

			switch(algorithmType) {			

			case "bbs":
				JSONObject bb_params = (JSONObject) algorithm.get("topology_params");				
				int bb_sampleSize = ((Number) bb_params.get("sample_size")).intValue();
				int bb_windowSize = ((Number) bb_params.get("window_size")).intValue();
				temp = new BinaryBernoulliSamplingTopology(createdTime, planName, i, "BINARY_BERNOULLI_SAMPLING", bb_sampleSize, bb_windowSize);				
				break;

			case "hs":
				JSONObject hash_params = (JSONObject) algorithm.get("topology_params");
				int hash_sampleSize = ((Number) hash_params.get("sample_ratio")).intValue();
				int hash_windowSize = ((Number) hash_params.get("window_size")).intValue();
				int hash_target = ((Number) hash_params.get("target")).intValue();
				String hash_function = (String) hash_params.get("hashFunction");
				// int bucket_size = ((Number) hash_params.get("bucketsize")).intValue();
				temp = new HashSamplingTopology(createdTime, planName, i, "HASH_SAMPLING", hash_sampleSize, hash_windowSize, hash_function, hash_target);
				break;				

			case "ps":
				JSONObject priority_params = (JSONObject) algorithm.get("topology_params");
				int priority_sampleSize = ((Number) priority_params.get("sample_size")).intValue();
				int priority_windowSize = ((Number) priority_params.get("window_size")).intValue();	
				int priority_target = ((Number) priority_params.get("target")).intValue();
				temp = new PrioritySamplingTopology(createdTime, planName, i, "PRIORITY_SAMPLING", priority_sampleSize, priority_windowSize, priority_target);
				break;

			case "rs":
				JSONObject reservoir_params = (JSONObject) algorithm.get("topology_params");
				int reservoir_sampleSize = ((Number) reservoir_params.get("sample_size")).intValue();
				int reservoir_windowSize = ((Number) reservoir_params.get("window_size")).intValue();
				temp = new ReservoirSamplingTopology(createdTime, planName, i, "RESERVOIR_SAMPLING", reservoir_sampleSize, reservoir_windowSize);				
				break;		

			case "ss":
				JSONObject systematic_params = (JSONObject) algorithm.get("topology_params");	
				int interval = ((Number) systematic_params.get("interval")).intValue();												
				temp = new SystematicSamplingTopology(createdTime, planName, i, "SYSTEMATIC_SAMPLING", interval);				
				break;		

			case "ks":
				JSONObject k_params = (JSONObject) algorithm.get("topology_params");
				int k_sampling_rate = ((Number) k_params.get("sample_rate")).intValue();
				temp = new KSamplingTopology(createdTime, planName, i, "K_SAMPLING", k_sampling_rate);
				break;

			case "qf":
				JSONObject query_params = (JSONObject) algorithm.get("topology_params");
				String query = (String) query_params.get("query");
				temp = new QueryFilteringTopology(createdTime, planName, i, "QUERY_FILTERING", query);		
				//Node node = Node.parse(query_keywords);
				break;

			case "bf":
				JSONObject bloom_params = (JSONObject) algorithm.get("topology_params");
				int bloom_size = ((Number) bloom_params.get("bucket_size")).intValue();
				String bloom_keywords = (String) bloom_params.get("keywords");
				int bloom_target = ((Number) bloom_params.get("target")).intValue();
				
				//'javaHashFunction','xxHash32','jsHash'
				String hashFunction1 = "javaHashFunction";
				String hashFunction2 = "xxHash32";
				String hashFunction3 = "jsHash";
				
				temp = new BloomFilteringTopology(createdTime, planName, i, "BLOOM_FILTERING", bloom_size, bloom_keywords, bloom_target, hashFunction1, hashFunction2, hashFunction3);
				break;

			case "kf":				
				JSONObject kalman_params = (JSONObject) algorithm.get("topology_params");
				double aValue = ((Number) kalman_params.get("a_value")).doubleValue();
				double qValue = ((Number) kalman_params.get("q_value")).doubleValue();
				double hValue = ((Number) kalman_params.get("h_value")).doubleValue();
				double xValue = ((Number) kalman_params.get("x_value")).doubleValue();
				double pValue = ((Number) kalman_params.get("p_value")).doubleValue();
				double rValue = ((Number) kalman_params.get("r_value")).doubleValue();
				
				int kalman_target = ((Number) kalman_params.get("target")).intValue();
				temp = new KalmanFilteringTopology(createdTime, planName, i, "KALMAN_FILTERING", aValue, qValue, hValue, xValue, pValue, rValue, kalman_target);
				break;

			case "nrkf":
				JSONObject nr_kalman_params = (JSONObject) algorithm.get("topology_params");
				double nr_aValue = ((Number) nr_kalman_params.get("a_value")).doubleValue();
				double nr_qValue = ((Number) nr_kalman_params.get("q_value")).doubleValue();
				double nr_hValue = ((Number) nr_kalman_params.get("h_value")).doubleValue();
				double nr_xValue = ((Number) nr_kalman_params.get("x_value")).doubleValue();
				double nr_pValue = ((Number) nr_kalman_params.get("p_value")).doubleValue();
				String measure = (String) nr_kalman_params.get("measure");
				
				int nr_kalman_target = ((Number) nr_kalman_params.get("target")).intValue();
				temp = new NRKalmanFilteringTopology(createdTime, planName, i, "NR_KALMAN_FILTERING", nr_aValue, nr_qValue, nr_hValue, nr_xValue, nr_pValue, measure, nr_kalman_target);
				break;		
				
			case "ikf":				
				JSONObject i_kalman_params = (JSONObject) algorithm.get("topology_params");
				double i_aValue = ((Number) i_kalman_params.get("a_value")).doubleValue();
				double i_qValue = ((Number) i_kalman_params.get("q_value")).doubleValue();
				double i_hValue = ((Number) i_kalman_params.get("h_value")).doubleValue();
				double i_xValue = ((Number) i_kalman_params.get("x_value")).doubleValue();
				double i_pValue = ((Number) i_kalman_params.get("p_value")).doubleValue();				
				
				int i_kalman_target = ((Number) i_kalman_params.get("target")).intValue();
				temp = new IKalmanFilteringTopology(createdTime, planName, i, "I_KALMAN_FILTERING", i_aValue, i_qValue, i_hValue, i_xValue, i_pValue, i_kalman_target);
				break;

			case "ucks":
				JSONObject uc_k_params = (JSONObject) algorithm.get("topology_params");
				int uc_k_sampleRate = ((Number) uc_k_params.get("sample_rate")).intValue();
				double uc = ((Number) uc_k_params.get("uc_under_bound")).doubleValue();
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
		DbAdapter.getInstance().addPlan(plan);
		DbAdapter.getInstance().addLog(owner, "INFO", "Plan is created." + " (" + plan.getPlanName() + ")");

		// Topology Params to Redis ...
		RedisAdapter.getInstance().addPlanParams(plan);	

	}

	private void destroyPlan() throws NotAliveException, AuthorizationException, TException, InterruptedException {

		// Parse Content!
		JSONObject content = (JSONObject) command.get("commandContent");
		
		// Get Information
		String planName = (String) content.get("planName");
		String owner = (String) content.get("owner");
		
		// Delete!
		Plan temp = plans.get(owner, planName);		
		temp.killTopologies();		
		plans.remove(temp);
		
		DbAdapter.getInstance().removePlan(temp);		
		DbAdapter.getInstance().addLog(owner, "INFO", "Plan is destroyed." + " (" + temp.getPlanName() + ")");
	}

	private void changeStatus() throws InvalidTopologyException, AuthorizationException, TException, InterruptedException, IOException {

		// Command Content.
		JSONObject content = (JSONObject) command.get("commandContent");

		// Modified Content.
		String plan = (String) content.get("planName");
		String owner = (String) content.get("owner");
		String status = (String) content.get("after");
		String modifiedTime = (String) content.get("modifiedTime");

		// Modified Plan.
		Plan temp = plans.get(owner, plan); // 해당 플랜
		temp.setStatus(status);
		temp.setModifiedTime(modifiedTime);

		// Plan Update
		plans.set(temp);

		System.out.println("[Topology Handler] Change Status...");
		System.out.println(temp.getPlanName());
		System.out.println(temp.getStatus());
		System.out.print("Ignore?");

		// DB에서도 해당 플랜의 상태와 ModifiedTime 변경
		DbAdapter.getInstance().changePlanStatus(temp);

		// 만약 상태가 active로 바뀐다면!
		if ( status.equals("ACTIVE") ) {						
			temp.activateTopologies();
			DbAdapter.getInstance().addLog(owner, "INFO", "Plan is activated." + " (" + temp.getPlanName() + ")");
			
			// 지능형 칼만 필터링이 포함되어 있으면, 메시지 전송
			for(ASamplingFilteringTopology i: temp.getTopologies()) {
				
				if(i.getTopologyType() == "I_KALMAN_FILTERING") {
					MessageSender ms = new MessageSender();
					ms.sendToIntelligentKalman("activate-plan", owner, plan);
				}
			}
		} 
		else if ( status.equals("DEACTIVE") ) {
			temp.deactivateTopologies();
			DbAdapter.getInstance().addLog(owner, "INFO", "Plan is dectivated." + " (" + temp.getPlanName() + ")");
		}
		else
			System.out.println("[Topology Handler] Status Type Error.");		
	}

}
