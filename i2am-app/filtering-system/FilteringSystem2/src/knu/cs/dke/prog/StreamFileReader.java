package knu.cs.dke.prog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espertech.esper.client.EPRuntime;

import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.DateParser;
import knu.cs.dke.vo.TwitterEvent;


//스트림데이터 input
public class StreamFileReader {

	long total = 0;
	public void read(String dataType, EPRuntime runtime) throws Exception{
		//수행 시간 기록
		ExecTime timer = new ExecTime();
		
		if(dataType.equals("Twitter")){
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(Constant.UploadRoute+Constant.InputFileName),"UTF-8")));

			JSONObject jsonObj = (JSONObject)obj;

			JSONArray dataList = (JSONArray)jsonObj.get("data");
			Iterator iterator = dataList.iterator();
			//int i=0;
			while(iterator.hasNext()){ //iterator.hasNext()
				JSONObject twitData = (JSONObject) iterator.next();		    	  
				String Lang = (String)twitData.get("Lang");		    	  
				String UserName = (String)twitData.get("UserName");
				String UserId = (String)twitData.get("UserID").toString();
				String CreatedAt = (String)twitData.get("CreatedAt");
				String Text = (String)twitData.get("Text");

				String[] HashTag_arr = Text.split("#");
				ArrayList<String> HashTag = new ArrayList<String>(Arrays.asList(HashTag_arr));
				HashTag.remove(0);
				if(HashTag.size()<1){ 	//해시태그 존재하지 않음
					HashTag = null;
				} else{
					for(String hashTag: HashTag){
						hashTag.replaceAll(" ", "");
						hashTag.replaceAll("\n", "");
						hashTag.replace(",", "");
					}
				}

				DateParser dp = new DateParser();
				long newCreatedAt = dp.parse(CreatedAt);
				timer.start();
				runtime.sendEvent(new TwitterEvent(UserName,UserId,newCreatedAt,Lang,Text,HashTag));
				timer.stop();
				total += timer.getRunTimeNano();

			}
		}

		System.out.println("수행시간: "+total/1000000+" ms");
	}
	
	class ExecTime {
	    private long start;
	    private long stop;
	 
	    void start() {
	        start = System.nanoTime();
	    }
	 
	    void stop() {
	        stop = System.nanoTime();
	    }
	 
	    long getRunTimeNano() {
	    	return stop - start;
	    }
	 
	    void printExecTime() {
	        System.out.println(stop-start + " ns");
	    }
	}
}

//}
