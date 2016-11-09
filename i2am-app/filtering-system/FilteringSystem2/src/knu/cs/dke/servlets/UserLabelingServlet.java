package knu.cs.dke.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import knu.cs.dke.prog.DBProcess;
import knu.cs.dke.prog.esper.EsperEngine;
import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.DateParser;
import knu.cs.dke.prog.util.ResultFileWriter;
import knu.cs.dke.prog.util.filter.BayesianFilter;
import knu.cs.dke.vo.TrainingResult;
import knu.cs.dke.vo.TwitterEvent;


@WebServlet("/labeling")
public class UserLabelingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	int idx=0;	


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("euc-kr");

		ArrayList<TwitterEvent> twits = new ArrayList<TwitterEvent>();
		ArrayList<TwitterEvent> hidden_twits = new ArrayList<TwitterEvent>();
		//그 조건의 idx
		idx = Integer.parseInt(request.getParameter("idx"));
		//language and keywords
		DBProcess dbProcess = new DBProcess();
		//true: language와 key만 받아옴
		String langNkeywords = dbProcess.getLog(idx,true);

		String lang = langNkeywords.split("\\|")[0];
		String[] keywords = langNkeywords.split("\\|")[1].split(",");
		keywords[keywords.length-1] = keywords[keywords.length-1].replace("\\|\\|", "");
		System.out.println("idx: "+idx+" "+lang+" "+keywords[keywords.length-1]+" !!!!!!!!!");

		//트윗 데이터 보내줌
		JSONParser parser = new JSONParser();
		Object obj=null;
		try {
			obj = parser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(Constant.UploadRoute+Constant.InputFileName),"UTF-8")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jsonObj = (JSONObject)obj;

		JSONArray dataList = (JSONArray)jsonObj.get("data");
		Iterator iterator = dataList.iterator();
		int count=0;
		while(iterator.hasNext()){ 	//다 보여줌
			JSONObject twitData = (JSONObject) iterator.next();		    	  
			String Lang = (String)twitData.get("Lang");	
			String UserName = (String)twitData.get("UserName");
			String UserId = (String)twitData.get("UserID").toString();
			String CreatedAt = (String)twitData.get("CreatedAt");
			String Text = (String)twitData.get("Text");
			if(Lang.equals(lang)){
				String[] HashTag_arr = Text.split("#");
				ArrayList<String> HashTag = new ArrayList<String>(Arrays.asList(HashTag_arr));
				HashTag.remove(0);
				if(HashTag.size()<1){ 	//해시태그 존재하지 않음
					HashTag = null;
				} else{
					for(String hashTag: HashTag){
						hashTag = hashTag.replaceAll(" ", "");
						hashTag = hashTag.replaceAll("\n", "");
						hashTag = hashTag.replace(",", "");
					}
				}

				DateParser dp = new DateParser();
				long newCreatedAt = dp.parse(CreatedAt);
				twits.add(new TwitterEvent(UserName, UserId, newCreatedAt,Lang, Text, HashTag));
			} else{
				//다른언어
				hidden_twits.add(new TwitterEvent(UserName, UserId,0,Lang, Text, null));
			}

		}
		System.out.println("size: "+twits.size()+" hidden_size: "+hidden_twits.size());
		request.setAttribute("twits", twits);
		request.getSession().setAttribute("twits", twits);
		request.getSession().setAttribute("hidden_twits", hidden_twits);
		response.setHeader("Cache-Control", "no-store");
		response.setContentType("text/html; charset=euc-kr");
		RequestDispatcher rd = request.getRequestDispatcher("/user/UserLabeling.jsp");
		rd.include(request, response);
	}

	//사용자가 추가로 선택한 데이터 기반으로 training set 만듦
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("euc-kr");
		response.setContentType("text/html; charset=euc-kr");
		String returnOrRedo = null;
		returnOrRedo = request.getParameter("ReturnOrRedo");

		ArrayList<TwitterEvent> twits = (ArrayList<TwitterEvent>) request.getSession().getAttribute("twits");
		ArrayList<TwitterEvent> hidden_twits = (ArrayList<TwitterEvent>) request.getSession().getAttribute("hidden_twits");

		if(returnOrRedo.equals("file_return")){
			//make file
			Constant.BayesianResult.clear();
			ResultFileWriter resultFileWriter = new ResultFileWriter();
			resultFileWriter.start();
			String partition = "------------------------------------------\r\n";
			for(TwitterEvent twit: twits){
				//모든 결과 write
				String inputString = partition+"userName: "+twit.getUserName()+"\r\n"+"language: "+twit.getLang()+"\r\n"+"text: "+twit.getText()+"\r\n";
				System.out.println("labeling "+inputString);
				Constant.FileWriter.write(inputString);
				Constant.FileWriter.flush();
			}
			resultFileWriter.finish();

			//return file
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition","attachment;filename=test.txt");


			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(Constant.DownloadRoute+Constant.Algorithm+".txt"));

			ServletOutputStream out = response.getOutputStream();

			byte[] outputByte = new byte[4096];
			int readByte = 0;
			while((readByte=bis.read(outputByte))!= -1){
				out.write(outputByte,0,readByte);
			}
			bis.close();
			out.flush();
			out.close();

		}else{
			//redo bayesian filter 

			System.out.println("do post idx is "+idx);
			String[] selected = request.getParameterValues("_selected_");
			int[] selected_idx = new int[selected.length];
			ArrayList<TwitterEvent> checked = new ArrayList<TwitterEvent>();
			ArrayList<TwitterEvent> unchecked = new ArrayList<TwitterEvent>();
			System.out.println("dddddddddddddd "+selected_idx[0]);
			System.out.println("selected size "+selected_idx.length);

			for(int i=0; i<selected.length;i++){
				selected_idx[i] = Integer.parseInt(selected[i]);
			}

			//checked 와 unchecked 나눔
			for(int i=0; i<twits.size();i++){
				boolean isChecked = false;
				for(int j=0; j<selected_idx.length;j++){
					if(i==selected_idx[j]){
						checked.add(twits.get(i));
						isChecked = true;
						break;
					}
				}
				if(!isChecked){
					unchecked.add(twits.get(i));
				}
			}
			if(!hidden_twits.isEmpty()){
				//hidden_twits들은 모두 ham처리 
				//ham 처리 위해서 unchecked에 넣는다
				for(int i=0; i<hidden_twits.size();i++){
					unchecked.add(hidden_twits.get(i));
				}
			}
			hidden_twits.clear();

			System.out.println("check size "+checked.size());
			System.out.println("uncheck size "+unchecked.size());
			//		
			Constant.BayesianResult.clear();
			BayesianFilter bayesianFilter = new BayesianFilter();
			if(bayesianFilter.training(checked, unchecked, idx)){
				try {
					//					unchecked.clear();
					Constant.EsperEngine = new EsperEngine();
					Constant.EsperEngine.start();
					request.setAttribute("twits", Constant.BayesianResult);
					request.getSession().setAttribute("twits", Constant.BayesianResult);
					System.out.println("BayesianResult size: "+Constant.BayesianResult.size());
					response.setHeader("Cache-Control", "no-store");
					RequestDispatcher rd = request.getRequestDispatcher("/user/UserLabeling.jsp");
					rd.include(request, response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				response.getWriter().print("fail...");
			}
		}


	}



}
