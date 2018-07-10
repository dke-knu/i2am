package knu.cs.dke.topology_manager.handlers;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

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

	public void excute() throws ParseException {		

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
			break;

		case "ALTER_SRC":
			break;

		case "ACTIVE_SRC":
			break;

		default:
			System.out.println("[Source Handler] Command is not exist.");
			break;			
		}		

		sources.printSummary();
	}

	public void createSource() {
		
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
				source = new KafkaSource(srcName, createdTime, owner, sourceType, data, conceptDrift, loadShedding, intelliEngine, testData, target, ip, port, topic);
			}
			else {
				source = new KafkaSource(srcName, createdTime, owner, sourceType, data, conceptDrift, loadShedding, intelliEngine, ip, port, topic);
			}

			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter db = DbAdapter.getInstance();
			db.addSource(source);			
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
				source = new DBSource(srcName, createdTime, owner, sourceType, data, conceptDrift, loadShedding, intelliEngine, testData, target,
						dbIp, dbPort, dbId, dbPw, dbName, dbQuery);
			}
			else {
				source = new DBSource(srcName, createdTime, owner, sourceType, data, conceptDrift, loadShedding, intelliEngine,
						dbIp, dbPort, dbId, dbPw, dbName, dbQuery);
			}

			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter dbdb = DbAdapter.getInstance();
			dbdb.addSource(source);	

			break;

		case "CUSTOM":			
			
			if(intelliEngine.equals("Y")) {
				source = new CustomSource(srcName, createdTime, owner, sourceType, data, conceptDrift, loadShedding, intelliEngine, testData, target);
			}
			else {
				source = new CustomSource(srcName, createdTime, owner, sourceType, data, conceptDrift, loadShedding, intelliEngine);
			}

			

			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter customDb = DbAdapter.getInstance();
			customDb.addSource(source);

			break;

		default:
			System.out.println("[Source Handler] Source Type Error.");
			break;
		}		

//		// 지능형 엔진 사용 시 > 소스 및 소스의 파일 정보 > 지능형 엔진에 전송
//		// Concept Drift 엔진에 전송
//		if(intelliEngine.equals("Y")) {
//
//			// "user-id", "src-name"
//			// String message = source.getOwner() + "," + source.getSourceName();
//
//			// JSON
//			JSONObject message = new JSONObject();
//			message.put("message", "new-src");
//			message.put("user-id", source.getOwner());
//			message.put("src-name", source.getSourceName());			
//
//			Socket socket = null;
//			OutputStream os = null;
//			OutputStreamWriter osw = null;
//			BufferedWriter bw = null;
//
//			Socket socket2 = null;
//			OutputStream os2 = null;
//			OutputStreamWriter osw2 = null;
//			BufferedWriter bw2 = null;
//
//			try {
//				// Intelligent Engine.
//				socket = new Socket("MN", 7979);
//				os = socket.getOutputStream();
//				osw = new OutputStreamWriter(os);
//				bw = new BufferedWriter(osw);
//				bw.write(message.toJSONString());
//
//				// Concept Drift.
//				// 165.132.214.219 39393
//				socket2 = new Socket("165.132.214.219", 39393);
//				os2 = socket2.getOutputStream();
//				osw2 = new OutputStreamWriter(os2);
//				bw2 = new BufferedWriter(osw2);
//				bw2.write(message.toJSONString());
//			}
//			catch (Exception e ) {
//				e.printStackTrace();
//			}
//			finally {
//				try {
//					bw.close();
//					osw.close();
//					os.close();
//					socket.close();
//
//					bw2.close();
//					osw2.close();
//					os2.close();
//					socket2.close();
//				}
//				catch(Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}		
	}

	public void destroySource(String sourceKey) {
		// sources.remove(sources.get(sourceKey));

		// Source를 삭제하려면,
		// Source가 포함된 Plan들을 삭제해야함
		// Plan을 삭제하려면, 
		// Plan에 포함된 Topology들을 삭제해야함
		// Topology들을 삭제하려면,
		// Topology Params들을 삭제해야함
	}

	public void alterSource() {

		// Parameter 업데이트만 하는 것으로...ㅎ


	}

	public void activeSource() {		

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		String owner = (String) content.get("owner");
		String name = (String) content.get("srcName");		

		Source source = sources.get(owner, name);
		source.setStatus("ACTIVE");			

		DbAdapter db = DbAdapter.getInstance();
		db.changeSourceStatus(source);

		sources.set(source);

		// Send to Concept Drift
		// JSON
		JSONObject message = new JSONObject();
		message.put("message", "activate-src");
		message.put("user-id", source.getOwner());
		message.put("src-name", source.getSourceName());			

		Socket socket = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		try {
			// Concept Drift
			socket = new Socket("165.132.214.219", 39393);
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			bw.write(message.toJSONString());
		}
		catch (Exception e ) {
			e.printStackTrace();
		}
		finally {
			try {
				bw.close();
				osw.close();
				os.close();
				socket.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		// Thread Start.
		source.start();
		System.out.println("[Source Handler]" + source.getName() + " is started!");
	}
	
	public void updateRecommendation() {
		
		// Parse 
		String message = (String) command.get("message");
		String user = (String) command.get("user-id");
		String srcname = (String) command.get("src-name");
		String recommendation = (String) command.get("recommendation");
		
		// DB Adapter에서 해당 Source의 recommendation을 Update!
	}

	public void deactiveSource() {

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		String owner = (String) content.get("owner");
		String name = (String) content.get("srcName");

		Source source = sources.get(owner, name);
		source.setStatus("DEACTIVE");		

		DbAdapter db = DbAdapter.getInstance();
		db.changeSourceStatus(source);

		// Thread Stop.				
		if(source.isAlive()) source.stop();		
		sources.set(source);

		System.out.println("[Source Handler]" + source.getName() + " is stopped!");
	}

	public void sendToConceptDrift() {

	}

	public void sendToLoadShedding() {

	}
}
