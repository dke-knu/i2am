package knu.cs.dke.prog.esper;

import java.io.IOException;
import java.util.ArrayList;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.UpdateListener;

import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.DateParser;
import knu.cs.dke.vo.TwitterEvent;

public class Listener implements UpdateListener{
	public void update(EventBean[] newEvents, EventBean[] oldEvents){
		DateParser dateParser = new DateParser();
		String createdAt = "";
		EventBean event = null;
		event = newEvents[0];
		String partition = "------------------------------------------\r\n";
		if(Constant.Dataset.equals("Twitter")){
			try {
				//날짜 원래대로 돌려놓음
				createdAt = dateParser.longToDate(event.get("createdAt").toString());
			} catch (PropertyAccessException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if(Constant.Algorithm.equals("bayesian")){
			Constant.BayesianResult.add(new TwitterEvent(event.get("userName").toString(),event.get("userId").toString(),Long.parseLong(event.get("createdAt").toString()),event.get("lang").toString(),event.get("text").toString(),(ArrayList<String>) event.get("hashTag")));
		}else{
			//real time
			String inputString = null;
			if(Constant.Dataset.equals("Twitter")){
				inputString = partition+"userName: "+event.get("userName")+"\r\n"+"language: "+event.get("lang")+"\r\n"+"createdAt: "+createdAt+"\r\n"+"text: "+event.get("text")+"\r\n";
			}else{
				//gaussian
				inputString = partition+event.get("numeric")+"\r\n";
			}
			try {
				Constant.BroadCaster.onMessage(inputString,Constant.UserSession);
				Constant.FileWriter.write(inputString);
				Constant.FileWriter.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
