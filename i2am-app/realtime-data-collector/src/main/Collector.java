package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Collector {

	static Logger logger = Logger.getLogger(Collector.class);
	static String pattern = "[%d{yyyy-MM-dd HH:mm:ss}] %-5p [%l] - %m%n";
	static PatternLayout layout = new PatternLayout(pattern);
	static String logfilename = null;
	static String datePattern = "yyyy-MM-dd";
	private static boolean is_data_kr;

	static UserInput userinput = new UserInput();

	private static String separator = System.getProperty("file.separator");

	private static BufferedReader rd = null;

	public static URL url;

	public static void main(String[] args) throws IOException, TransformerException, InterruptedException {

		DailyRollingFileAppender appender = null;

		String jsondir;

		if (args.length < 1) {
			System.out.println(
					"json file directory is needed. USAGE: java -jar openapi_realtime_data_collector.jar [json_file_dir]");
			return;
		}
		jsondir = args[0];

		JSONObject inputjson = ReadJSON(jsondir);

		// Read Json, User Input Setting
		Map<String, Object> input = getMapFromJsonObject(inputjson);
		is_data_kr = setUserinput(input);

		logfilename = "log_" + userinput.getFilename() + ".log";

		try {
			appender = new DailyRollingFileAppender(layout, logfilename, datePattern);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		logger.addAppender(appender);

		// Data Collect TimeInterval (수집 주기)
		Long timeintveral = Long.parseLong(userinput.getTimeInterval());

		while (true) {
			collect();
			Thread.sleep(timeintveral * 1000 * 60);
		}

	}

	public static void collect() throws IOException {

		String dir, filename;

		url = URLBuilder(userinput);

		String data = readDataFromUrl(url, userinput);

		dir = userinput.getDirectory();
		long time = System.currentTimeMillis();
		String folder_name = FileDate(time);

		// Make File
		File f = new File(dir + folder_name);
		if (!f.exists()) {
			f.mkdirs();
			logger.info("Directory created");
		}
		if (!f.exists()) {
			logger.fatal("File cannot make");
			return;
		}
		// Filename(파일 이름 형식): filename-yyyy-MM-dd_HH_mm_ss.format
		SimpleDateFormat fileTime = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		String request_file_date = fileTime.format(new Date(time));
		filename = separator + userinput.getFilename() + "-" + request_file_date;

		if (userinput.getFormat().equals("xml")) {
			WriteFileXml(data, f.toString(), filename);

		} else if (userinput.getFormat().equals("txt")) {
			WriteFileText(data, f.toString(), filename);
		} else {
			WriteFileJson(data, f.toString(), filename);
		}
	}

	public static String FileDate(long time) {

		SimpleDateFormat folderTimeYear = new SimpleDateFormat("yyyy");
		SimpleDateFormat folderTimeMonth = new SimpleDateFormat("MM");
		SimpleDateFormat folderTimeDay = new SimpleDateFormat("dd");

		String folder_name_year = folderTimeYear.format(new Date(time));
		String folder_name_month = folderTimeMonth.format(new Date(time));
		String folder_name_day = folderTimeDay.format(new Date(time));

		return (folder_name_year + separator + folder_name_month + separator + folder_name_day);
	}

	public static boolean setUserinput(Map<String, Object> inputjson) {

		for (String key : inputjson.keySet()) {
			Object value = inputjson.get(key);
			if (key.equals("URL")) {
				userinput.setURL(value);
			} else if (key.equals("serviceKey")) {
				is_data_kr = userinput.setServiceKey(value);
			} else if (key.equals("parameter")) {
				userinput.setParameter(value);
			} else if (key.equals("timeInterval")) {
				userinput.setTimeInterval(value);
			} else if (key.equals("dir")) {
				userinput.setDirectory(value);
			} else if (key.equals("filename")) {
				userinput.setFilename(value);
			} else if (key.equals("format")) {
				userinput.setFormat(value);
			} else if (key.equals("urlHeader")) {
				userinput.seturlHeader(value);
			} else {
				logger.error("Json file has invalid value");
			}
		}
		return is_data_kr;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMapFromJsonObject(JSONObject json) {

		Map<String, Object> map = null;

		try {
			map = new ObjectMapper().readValue(json.toJSONString(), Map.class);
		} catch (JsonParseException e) {
			logger.fatal("Json Parse Error! Please check your json file.");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.fatal("Json Mapping Error! Please check your json file.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	// Set URL
	public static URL URLBuilder(UserInput user) throws UnsupportedEncodingException {

		Map<String, String> para = user.getParameter();
		URL userurl = null;
		StringBuilder urlBuilder = new StringBuilder(user.getURL());
		if (user.getServiceKey() == null && user.getserviceKeyMap() == null) {

		} else {
			if (is_data_kr) {
				urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + user.getServiceKey());
			} else {
				for (String serviceKeyname : user.getserviceKeyMap().keySet()) {
					urlBuilder.append("?" + serviceKeyname + "=" + user.getserviceKeyMap().get(serviceKeyname));
				}
			}
		}
		if (para != null) {
			// Parameter
			for (String key : para.keySet()) {
				urlBuilder.append("&" + URLEncoder.encode(key, "UTF-8") + "=" + para.get(key));
			}
		}
		try {
			userurl = new URL(urlBuilder.toString());
			logger.info("URL Build Success: " + userurl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.fatal("URL Build Error");
			e.printStackTrace();
		}
		return userurl;
	}

	// HTTP Request and Get data
	public static String readDataFromUrl(URL url, UserInput user) throws IOException {
		int responsecode = 0;
		HttpURLConnection conn = null;
		Map<String, String> urlHeader = user.geturlHeader();

		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000 * 5);
			conn.setReadTimeout(5000 * 5);
			conn.setDoOutput(true);
			if (urlHeader != null) {
				for (String key : urlHeader.keySet()) {
					conn.setRequestProperty(key, urlHeader.get(key));
				}
			}

		} catch (SocketTimeoutException e) {
			logger.warn("HTTP request timeout!");
			return null;
		} catch (IOException e3) {
			// e3.printStackTrace();
			return null;
		}

		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		try {
			responsecode = conn.getResponseCode();
		} catch (SocketTimeoutException e) {
			logger.warn("Get ResponseCode is timeout");
			return null;
			// TODO: handle exception
		}
		if ((responsecode >= 200 && responsecode <= 300)) {
			try {
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			} catch (IOException e) {
				logger.error("Read timed out");
				return null;
			}
		} else {
			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			logger.error("HTTP Request Error!");
		}
		StringBuilder sb = new StringBuilder();
		String line;

		try {
			while ((line = rd.readLine()) != null) {
				line += ("\n");
				sb.append(line);
			}
			logger.info("Reading Data Success");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		conn.disconnect();

		try {
			rd.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	// Write File to XML form
	public static void WriteFileXml(String xml, String dir, String filename) {
		xml = xml.replace("\n", "");
		try {
			Document testdoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(xml.getBytes()));

			try {
				File file = new File(dir, filename + ".xml");
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!file.exists()) {
					logger.fatal("File not created");
					return;
				}
				;

				StreamResult resultfile = new StreamResult(new File(dir + filename + ".xml"));
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.transform(new DOMSource(testdoc), resultfile);
				logger.info("Write File Success");
			} catch (Exception e) {
				logger.error("Write File Error");
			}
		} catch (Exception e) {
			logger.error("Write File Error");
		}

	}

	// Write File to XML form
	public static void WriteFileJson(String json, String dir, String filename) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(json);
		String resultjson = gson.toJson(je);

		try {
			File file = new File(dir, filename + ".json");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!file.exists()) {
				logger.fatal("File not created");
				return;
			}
			;

			FileOutputStream out = new FileOutputStream(file);
			out.write(resultjson.getBytes());
			out.close();
		} catch (Exception e) {
			logger.error("Write File Error");
		}
	}

	public static void WriteFileText(String text, String dir, String filename) {

		try {
			File file = new File(dir, filename + ".txt");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!file.exists()) {
				logger.fatal("File not created");
				return;
			}
			;
			BufferedWriter output = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));
			output.write(text);
			output.close();
		} catch (Exception e) {
			logger.error("Write File Error");
		}

	}

	public static JSONObject ReadJSON(String jsondir) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(jsondir));
			JSONObject jsonObject = (JSONObject) obj;
			return jsonObject;
		} catch (Exception e) {
			logger.fatal("JSON file error");
			// TODO: handle exception
		}
		logger.info("Reading JSON file is Success");
		return null;
	}
}
