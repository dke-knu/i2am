package knu.cs.dke.topology_manager.handlers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.storm.shade.org.eclipse.jetty.util.thread.ThreadPool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager.SourceList;
import knu.cs.dke.topology_manager.sources.CustomSource;
import knu.cs.dke.topology_manager.sources.DBSource;
import knu.cs.dke.topology_manager.sources.KafkaSource;
import knu.cs.dke.topology_manager.sources.Source;
import knu.cs.dke.topology_manager.sources.SourceSchema;

public class SourceHandler {

	private SourceList sources;	
	private JSONObject command;
		
	public SourceHandler(SourceList sources, String command) throws ParseException {

		this.sources = sources;
		JSONParser jsonParser = new JSONParser();
		this.command = (JSONObject) jsonParser.parse(command);			
	}
	
	public void excute() throws ParseException, InterruptedException, UnknownHostException, IOException {		

		String commandType = (String) command.get("commandType");
		System.out.println("[Source Handler] Command Type: " + commandType);		

		switch (commandType) {

		case "CREATE_SRC":
			this.createSource();
			break;

		case "CHANGE_STATUS_OF_SRC":				
			JSONObject content = (JSONObject) command.get("commandContent");			
			String after = (String) content.get("after");

			if (after.equals("ACTIVE")) {								
				this.activeSource();
			}
			else if (after.equals("DEACTIVE")) this.deactiveSource();
			break;

		case "DESTROY_SRC":
			this.destroySource();
			break;

		case "ALTER_SRC":
			break;

		default:
			System.out.println("[Source Handler] Command is not exist.");
			break;			
		}		

		sources.printSummary();
	}

	public void createSource() throws UnknownHostException, IOException {
		
		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");

		// Content Basic Info. 
		String owner = (String) content.get("owner");
		String srcName = (String) content.get("srcName");
		String createdTime = (String) content.get("createdTime");

		String conceptDrift = (String) content.get("usesConceptDriftEngine");
		String loadShedding = (String) content.get("usesLoadShedding");
		String intelliEngine = (String) content.get("usesIntelligentEngine");
		String testData = "";
		String target = "";

		if(intelliEngine.equals("Y")) {
			testData = (String) content.get("testDataName");
			target = (String) content.get("target");
		}				
		
		// Data Schema!!
		JSONArray columns = (JSONArray) content.get("dataScheme");
		ArrayList<SourceSchema> data = new ArrayList<SourceSchema>();
		
		for(int i=0; i<columns.size(); i++) {
			
			JSONObject column = (JSONObject) columns.get(i);
			System.out.println("Schema :" + column);
			
			SourceSchema temp = new SourceSchema(Integer.parseInt((String) column.get("column_index")), (String) column.get("column_name"), (String) column.get("column_type"));
			
			data.add(temp);
		}

		// Source Type.
		String sourceType = (String) content.get("srcType");
		Source source = null;

		switch(sourceType) {

		case "KAFKA":			
			JSONObject kafka = (JSONObject) content.get("kafkaParams");
			String ip = (String) kafka.get("zookeeperIp");
			String port = (String) kafka.get("zookeeperPort");
			String topic = (String) kafka.get("topic");			

			if(intelliEngine.equals("Y")) {
				source = new KafkaSource(srcName, createdTime, owner, sourceType, data, "WAITING", loadShedding, intelliEngine, testData, target, ip, port, topic);
			}
			else {
				source = new KafkaSource(srcName, createdTime, owner, sourceType, data, "NOT_USED", loadShedding, intelliEngine, ip, port, topic);
			}

			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter.getInstance().addSource(source);			
			break;

		case "DATABASE":
			JSONObject database = (JSONObject) content.get("databaseParams");			
			String dbIp = (String) database.get("databaseIp");
			String dbPort = (String) database.get("databasePort");
			String dbId = (String) database.get("databaseId");
			String dbPw = (String) database.get("databasePw");
			String dbName = (String) database.get("database");
			String dbQuery = (String) database.get("query");

			if(intelliEngine.equals("Y")) {
				source = new DBSource(srcName, createdTime, owner, sourceType, data, "WAITING", loadShedding, intelliEngine, testData, target,
						dbIp, dbPort, dbId, dbPw, dbName, dbQuery);
			}
			else {
				source = new DBSource(srcName, createdTime, owner, sourceType, data, "NOT_USED", loadShedding, intelliEngine,
						dbIp, dbPort, dbId, dbPw, dbName, dbQuery);
			}

			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter.getInstance().addSource(source);	

			break;

		case "CUSTOM":			
			
			if(intelliEngine.equals("Y")) {
				source = new CustomSource(srcName, createdTime, owner, sourceType, data, "WAITING", loadShedding, intelliEngine, testData, target);
			}
			else {
				source = new CustomSource(srcName, createdTime, owner, sourceType, data, "NOT_USED", loadShedding, intelliEngine);
			}

			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter.getInstance().addSource(source);

			break;

		default:
			System.out.println("[Source Handler] Source Type Error.");
			break;
		}		
		
		DbAdapter.getInstance().addLog(owner, "INFO", "Source is created." + " (" + source.getSourceName() + ")");
		

		 // 지능형 엔진 사용 시 > 소스 및 소스의 파일 정보 > 지능형 엔진에 전송
		 // Concept Drift 엔진에 전송
		if(intelliEngine.equals("Y")) {

			MessageSender ms = new MessageSender();			
			ms.sendToConceptDrift("new-src", source.getOwner(), source.getSourceName());
			ms.sendToIntelligentEngine("new-src", source.getOwner(), source.getSourceName());
		}	
		
		// 로드쉐딩 사용 시 
		if(loadShedding.equals("Y")) {			
			MessageSender lsm = new MessageSender();
			lsm.sendToLoadSheddingManager("creation", source.getOwner(), source.getSourceName());
		}
	}

	public void destroySource() throws InterruptedException, UnknownHostException, IOException {
	
		// Parse Content!
		JSONObject content = (JSONObject) command.get("commandContent");
				
		// Get Information
		String srcName = (String) content.get("srcName");
		String owner = (String) content.get("owner");
				
		//
		//this.deactiveSource();
		
		// Delete!
		Source temp = sources.get(owner, srcName);	
		
		// 컨셉 드리프트 사용 시, 소스가 중지 되면 메시지 전송
		if(temp.getUseIntelliEngine().equals("Y")) {			
			MessageSender ms = new MessageSender();			
			ms.sendToConceptDrift("destroy-src", temp.getOwner(), temp.getSourceName());			
		}		
		
		// 로드쉐딩 사용 시 
		if(temp.getUseLoadShedding().equals("Y")) {			
			MessageSender lsm = new MessageSender();
			lsm.sendToLoadSheddingManager("deletion", temp.getOwner(), temp.getSourceName());
		}		
		
		sources.remove(temp);		
				
		DbAdapter.getInstance().removeSource(temp);
		DbAdapter.getInstance().addLog(owner, "INFO", "Source is destroyed." + " (" + srcName + ")");
		
	}

	public void alterSource() {
		// Parameter 업데이트만 하는 것으로...ㅎ
	}

	public void activeSource() throws UnknownHostException, IOException {		

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		String owner = (String) content.get("owner");
		String name = (String) content.get("srcName");		

		System.out.println(owner + name);
		
		Source source = sources.get(owner, name);
		source.setStatus("ACTIVE");			
		
		DbAdapter.getInstance().changeSourceStatus(source);

		sources.set(source);

		if(source.getSrcType() != "CUSTOM") {		
			// Thread Start.
			Thread run = new Thread(source, source.getSourceName());		
			sources.addThread(run);
			run.start();			
		}				
		
		// 컨셉 드리프트 사용 시, 소스가 실행 되면 메시지 전송
		if(source.getUseIntelliEngine().equals("Y")) {			
			MessageSender ms = new MessageSender();			
			ms.sendToConceptDrift("activate-src", source.getOwner(), source.getSourceName());			
		}		
				
		System.out.println("[Source Handler]" + source.getName() + " is started! - " + source.isAlive() );
		DbAdapter.getInstance().addLog(owner, "INFO", "Source is activated."  + " (" + source.getSourceName() + ")");
	}
	
/*	public void updateRecommendation() {
		
		// Parse 
		String message = (String) command.get("message");
		String user = (String) command.get("user-id");
		String srcname = (String) command.get("src-name");
		String recommendation = (String) command.get("recommendation");
		
		// DB Adapter에서 해당 Source의 recommendation을 Update!
	}*/

	public void deactiveSource() throws InterruptedException, UnknownHostException, IOException {

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		String owner = (String) content.get("owner");
		String name = (String) content.get("srcName");

		Source source = sources.get(owner, name);
		source.setStatus("DEACTIVE");		

		DbAdapter.getInstance().changeSourceStatus(source);
		
		// 컨셉 드리프트 사용 시, 소스가 중지 되면 메시지 전송
		if(source.getUseIntelliEngine().equals("Y")) {			
			MessageSender ms = new MessageSender();			
			ms.sendToConceptDrift("deactivate-src", source.getOwner(), source.getSourceName());			
		}		

		sources.set(source);
		
		if(source.getSrcType() != "CUSTOM") {		
			Thread run = sources.getThread(source.getSourceName());
			run.interrupt();			
		}				
		
		System.out.println("[Source Handler]" + source.getName() + " is stopped! - " + source.isAlive() );
		
		DbAdapter.getInstance().addLog(owner, "INFO", "Source is deactivated." + " (" + source.getSourceName() + ")");
	}

	public void sendToConceptDrift() {

	}

	public void sendToLoadShedding() {

	}
}
