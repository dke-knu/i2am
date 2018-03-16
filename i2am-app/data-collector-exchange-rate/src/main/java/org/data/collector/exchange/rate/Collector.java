package org.data.collector.exchange.rate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Hello world!
 *
 */
public class Collector 
{
	String urlKEB = "http://fx.kebhana.com/FER1101M.web";
	SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMddHHmm"); 
	
	public static void main( String[] args ) throws InterruptedException {
		Collector collector = new Collector();
		while (true) {
			collector.collect();
			Thread.sleep(1000);
		}
	}
	
	public void collect() {
		String last = new String();
		JSONObject obj = readJsonFromUrl(urlKEB);
		
		if (obj == null)
			System.out.println("Connection is refused.");
		
		String strDate = (String) obj.get("날짜");
		if (!strDate.equals(last)) {
			last = strDate;
			Date date = stringToDate(strDate);
			String dirPath = (date.getYear()+1900) + "/" + (date.getMonth()+1) + "/" + date.getDate();
			if (!new File(dirPath).exists())
				new File(dirPath).mkdirs();
			String filePath = dirPath + "/exchange-rate-" + fileFormat.format(date) + ".json";
			writeExchangeRateToFile(filePath, obj);
		}
	}

	public static JSONObject readJsonFromUrl(String url) {
		InputStream is = null;
		JSONParser parser = new JSONParser();
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			
			is = uc.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line.trim());
			}

			Object obj = parser.parse(sb.toString().split("var exView = ")[1]);
			return (JSONObject) obj;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}

	public static Date stringToDate(String strDate) {
		String[] split = strDate.split(" ");
		int year = Integer.parseInt(split[0].substring(0, 4));
		int month = Integer.parseInt(split[1].substring(0, 2));
		int date = Integer.parseInt(split[2].substring(0, 2));
		int hrs = Integer.parseInt(split[3].split(":")[0]);
		int min = Integer.parseInt(split[3].split(":")[1]);
		
		return new Date(year-1900, month-1, date, hrs, min);
	}

	private static void writeExchangeRateToFile(String filePath, JSONObject obj) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			String s = obj.toString();

			out.write(s);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
