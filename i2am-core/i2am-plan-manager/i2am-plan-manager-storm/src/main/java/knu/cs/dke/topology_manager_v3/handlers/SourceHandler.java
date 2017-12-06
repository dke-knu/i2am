package knu.cs.dke.topology_manager_v3.handlers;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.topology_manager_v3.SourceList;
import knu.cs.dke.topology_manager_v3.sources.CustomSource;
import knu.cs.dke.topology_manager_v3.sources.DBSource;
import knu.cs.dke.topology_manager_v3.sources.KafkaSource;
import knu.cs.dke.topology_manager_v3.sources.Source;

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

			if (after.equals("ACTIVE")) this.activeSource();
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
	}

	public void createSource() {

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");

		// Content Basic Info. 
		String owner = (String) content.get("owner");
		String srcName = (String) content.get("srcName");
		String createdTime = (String) content.get("createdTime");

		String intelliEngine = (String) content.get("usesIntelligentEngine");
		String testData = null;

		if(intelliEngine.equals("Y")) {
			testData = (String) content.get("testDataName");
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

			source = new KafkaSource(srcName, createdTime, owner, intelliEngine, testData, sourceType, "N", ip, port, topic);

			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter db = new DbAdapter();
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

			source = new DBSource(srcName, createdTime, owner, intelliEngine, testData, sourceType, "N", dbIp, dbPort, dbId, dbPw, dbName, dbQuery);

			// List에 저장☆
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter dbdb = new DbAdapter();
			dbdb.addSource(source);	

			break;

		case "CUSTOM":			
			source = new CustomSource(srcName, createdTime, owner, intelliEngine, "N", testData, sourceType, "N");
			
			sources.add(source);
			// DB Adapter로 DB에 저장★
			DbAdapter customDb = new DbAdapter();
			customDb.addSource(source);
			
			break;

		default:
			System.out.println("[Source Handler] Source Type Error.");
			break;
		}		

		// 지능형 엔진 사용 시 소스 및 소스의 파일 정보를 엔진에 전송해주어야할둣
		if(intelliEngine.equals("Y")) {
			
			String message = source.getOwner() + "," + source.getSourceName();

			Socket socket = null;
			OutputStream os = null;
			OutputStreamWriter osw = null;
			BufferedWriter bw = null;
			
			try {
				socket = new Socket("MN", 7979);
				os = socket.getOutputStream();
				osw = new OutputStreamWriter(os);
				bw = new BufferedWriter(osw);

				bw.write(message);			
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
		}		
	}

	public void destroySource(String sourceKey) {
		sources.remove(sources.get(sourceKey));		
	}

	public void alterSource() { }

	public void activeSource() {		

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		String name = (String) content.get("srcName");

		Source source = sources.get(name);
		source.setStatus("ACTIVE");

		sources.set(source);	
		
		DbAdapter db = new DbAdapter();
		db.changeSourceStatus(source);

		// Thread Start.
		Thread srcThread = new Thread(source);		
		srcThread.start();
	}

	public void deactiveSource() {

		// Content.
		JSONObject content = (JSONObject) command.get("commandContent");
		String name = (String) content.get("srcName");
		
		Source source = sources.get(name);
		source.setStatus("DEACTIVE");

		sources.set(source);

		DbAdapter db = new DbAdapter();
		db.changeSourceStatus(source);

		// Thread Stop.
		Thread srcThread = new Thread(source);		
		if(srcThread.isAlive()) srcThread.stop();	
	}
}
