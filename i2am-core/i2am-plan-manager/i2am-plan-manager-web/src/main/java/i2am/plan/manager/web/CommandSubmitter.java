package i2am.plan.manager.web;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import i2am.plan.manager.web.bean.Algorithm;
import i2am.plan.manager.web.bean.DatabaseInfo;
import i2am.plan.manager.web.bean.KafkaInfo;

public class CommandSubmitter {
	private JSONObject command = new JSONObject();
	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public enum COMMAND_TYPE {
		CREATE_SRC, CREATE_DST, CREATE_PLAN,
		CHANGE_STATUS_OF_SRC, CHANGE_STATUS_OF_DST, CHANGE_STATUS_OF_PLAN
	};
	public enum SRC_TYPE {CUSTOM, KAFKA, DATABASE};
	public enum DST_TYPE {CUSTOM, KAFKA, DATABASE};
	public enum ALGORITHM_TYPE {
		BINARY_BERNOULLI_SAMPLING, HASH_SAMPLING, PRIORITY_SAMPLING,
		RESERVOIR_SAMPLING, STRATIFIED_SAMPLING, SYSTEMATIC_SAMPLING,
		
		QUERY_FILTERING
	};
	public enum STATUS {ACTIVE, DEACTIVE};
	
	// change status of src, change status of dst, and change status of plan. 
	public void changeStatus(String commander, COMMAND_TYPE commandType, 
			String name, STATUS after) {
		String commandId = UUID.randomUUID().toString();
		String commandTime = format.format(new Date());
		
		command.put("commandId", commandId);
		command.put("commander", commander);
		command.put("commandType", commandType.name());
		command.put("commandTime", commandTime);
		
		JSONObject commandContent = new JSONObject();
		commandContent.put("owner", commander);
		if (commandType == COMMAND_TYPE.CHANGE_STATUS_OF_SRC)
			commandContent.put("srcName", name);
		else if (commandType == COMMAND_TYPE.CHANGE_STATUS_OF_DST)
			commandContent.put("dstName", name);
		else if (commandType == COMMAND_TYPE.CHANGE_STATUS_OF_PLAN)
			commandContent.put("planName", name);
		commandContent.put("modifiedTime", commandTime);
		commandContent.put("after", after.name());
		
		command.put("commandContent", commandContent);
	}
	
	// create src. 
	public void createSrc(String commander, String srcName, SRC_TYPE srcType, 
			DatabaseInfo database, KafkaInfo kafka, boolean usesIntelli, String testDataName) {
		String commandId = UUID.randomUUID().toString();
		String commandTime = format.format(new Date());
		
		command.put("commandId", commandId);
		command.put("commander", commander);
		command.put("commandType", COMMAND_TYPE.CREATE_SRC.name());
		command.put("commandTime", commandTime);
		
		JSONObject commandContent = new JSONObject();
		commandContent.put("owner", commander);
		commandContent.put("srcName", srcName);
		commandContent.put("createdTime", commandTime);
		commandContent.put("srcType", srcType.name());
		
		if (srcType.equals(SRC_TYPE.KAFKA)) {
			commandContent.put("kafkaParams", kafka.toJSONObject());
		} else if (srcType.equals(SRC_TYPE.DATABASE)) {
			commandContent.put("databaseParams", database.toJSONObject());
		} else {
		}
		
		if (!usesIntelli) commandContent.put("usesIntelligentEngine", "N"); 
		else {
			commandContent.put("usesIntelligentEngine", "Y");
			commandContent.put("testDataName", testDataName);
		}
		
		command.put("commandContent", commandContent);
	}
	
	// create dst. 
	public void createDst(String commander, String dstName, DST_TYPE dstType, 
			DatabaseInfo database, KafkaInfo kafka) {
		String commandId = UUID.randomUUID().toString();
		String commandTime = format.format(new Date());
		
		command.put("commandId", commandId);
		command.put("commander", commander);
		command.put("commandType", COMMAND_TYPE.CREATE_DST.name());
		command.put("commandTime", commandTime);
		
		JSONObject commandContent = new JSONObject();
		commandContent.put("owner", commander);
		commandContent.put("dstName", dstName);
		commandContent.put("createdTime", commandTime);
		commandContent.put("dstType", dstType.name());
		
		if (dstType.equals(DST_TYPE.KAFKA)) {
			commandContent.put("kafkaParams", kafka.toJSONObject());
		} else if (dstType.equals(DST_TYPE.DATABASE)) {
			commandContent.put("databaseParams", database.toJSONObject());
		} else {
		}
		
		command.put("commandContent", commandContent);
	}
	
	// create plan. 
	public void createPlan(String commander, String planName, 
			String srcName, String dstName, List<Algorithm> algorithms) {
		String commandId = UUID.randomUUID().toString();
		String commandTime = format.format(new Date());
		
		command.put("commandId", commandId);
		command.put("commander", commander);
		command.put("commandType", COMMAND_TYPE.CREATE_PLAN.name());
		command.put("commandTime", commandTime);
		
		JSONObject commandContent = new JSONObject();
		commandContent.put("owner", commander);
		commandContent.put("planName", planName);
		commandContent.put("createdTime", commandTime);
		commandContent.put("srcName", srcName);
		commandContent.put("dstName", dstName);
		
		JSONArray jsonAlgorithms = new JSONArray();
		for (Algorithm a: algorithms) {
			jsonAlgorithms.add(a.toJSONObject());
		}
		commandContent.put("algorithms", jsonAlgorithms);
		
		command.put("commandContent", commandContent);
	}
	
	public void submit() {
		Thread t = new Thread(new Submitter(this.command));
		t.start();
	}
	
	private class Submitter implements Runnable {

		private final String serverIp = "114.70.235.43";
		private final int serverPort = 11111;
		
		private JSONObject obj = null;
		
		private Submitter(JSONObject obj) {
			this.obj = obj;
		}
			
		public void run() {
			Socket socket = null;
			DataOutputStream write = null;
			
			try {
			socket = new Socket(serverIp, serverPort);		
			write = new DataOutputStream(socket.getOutputStream());
			
			write.writeUTF(obj.toJSONString());
			
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (write != null) {
						write.close();
					}
					if (socket != null) {
						socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// for test
	public String printCommand() {
		System.out.println(this.command.toJSONString());
		return this.command.toJSONString();
	}
}
