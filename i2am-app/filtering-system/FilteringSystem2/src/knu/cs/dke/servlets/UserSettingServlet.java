package knu.cs.dke.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.websocket.Session;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.oreilly.servlet.MultipartRequest;

import knu.cs.dke.prog.DBProcess;
import knu.cs.dke.prog.Preprocessing;
import knu.cs.dke.prog.esper.EsperEngine;
import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.ResultFileWriter;
import knu.cs.dke.vo.Condition;
import knu.cs.dke.vo.ConditionLog;
import knu.cs.dke.websocket.BroadCaster;

@WebServlet("/setting")
public class UserSettingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("euc-kr");
		response.setContentType("text/html; charset=euc-kr");
		System.out.println("hey hello");

		//과거 조건 있었는지 확인
		request.setCharacterEncoding("UTF-8");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<ConditionLog> condition_log = new ArrayList<ConditionLog>();
		try{
			ServletContext sc = this.getServletContext();
			Class.forName(sc.getInitParameter("driver"));

			conn = DriverManager.getConnection(
					sc.getInitParameter("url"),
					sc.getInitParameter("username"),
					sc.getInitParameter("password"));
			stmt = conn.createStatement();

			//날짜별 top3
			String sql = "select * from bayesian_condition_log order by created_at desc";
			rs = stmt.executeQuery(sql);
			int i=0;
			while(rs.next() && i<3){
				int idx = rs.getInt("idx");
				String lang = rs.getString("lang");
				String keywords = rs.getString("keywords");
				String created_at = rs.getString("created_at");

				condition_log.add(new ConditionLog(idx, lang, keywords, created_at));	
				i++;
			}
			request.setAttribute("condition_log", condition_log);
			response.setHeader("Cache-Control","no-store");
			//안보냄
			RequestDispatcher rd = request.getRequestDispatcher("/user/UserSettingForm.jsp");
			rd.forward(request, response);
		} catch(Exception e){
			e.printStackTrace();
		}

	}

	@SuppressWarnings("static-access")
	//FilteringSystem/setting
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("euc-kr");
		response.setContentType("text/html; charset=euc-kr");
		List<Condition> Conditions = null;	
		String Dataset = null,Algorithm= null, OutputType=null, inputType = null;
		String[] param = null;
		String source_url = null;

		Constant.FirstStart = false;

		int sizeLimit = 700*1024*1024; //700MB
		String path = Constant.UploadRoute;
		//multipart request - 파일 업로드 때문에 필요함
		MultipartRequest multipartRequest = new MultipartRequest(request,path,sizeLimit,"euc-kr");

		//사용자가 하나라도 입력 안하면 back
		try{
			Dataset = "Twitter";
			Algorithm = multipartRequest.getParameter("algorithm");
			inputType = multipartRequest.getParameter("input_method");
			param = multipartRequest.getParameterValues("conditions");	
			System.out.println("condition size "+param.length);
		}catch(Exception e){
			//			response.sendRedirect("./setting");
		}


		//전역변수에 값 설정
		//Constant.Dataset = Dataset;
		//지금은 다른 데이터 안쓰므로 Twitter로 아예 정함
		//Constant.Dataset = "Twitter";
		Constant.Algorithm = Algorithm;
		Constant.InputType = inputType;
		System.out.println("input type: "+inputType);
		if(inputType.equals("input_file")){
			if(multipartRequest.getFileNames().hasMoreElements()){
				Constant.InputFileName = multipartRequest.getOriginalFileName("stream_file");
				System.out.println("**********************input file "+Constant.InputFileName);
			}
		}else{
			//real time
			source_url = multipartRequest.getParameter("realtime_source");
			System.out.println("source_url: "+source_url);
			if(source_url.contains("twitter")){
				Constant.Dataset = "Twitter";
			}else{
				//가우시안
				Constant.Dataset = "Gaussian";
			}
		}
		String b_lang = null;
		String b_keywords = null;

		//condition object로 옮기기
		Preprocessing preprocess = new Preprocessing();

		if(Algorithm.equals("bayesian")){
			b_lang = multipartRequest.getParameter("languages");
			b_keywords = "all";

			String epl = preprocess.Preprocessing(Conditions);
			Constant.EPL = epl;

			DBProcess dbProcess = new DBProcess();
			int idx = dbProcess.saveLog(b_lang, b_keywords);
			if(idx > 0){
				response.setHeader("Cache-Control","no-store");
				RequestDispatcher rd = request.getRequestDispatcher("/labeling");
				response.sendRedirect("./labeling?dataset="+Dataset+"&algorithm="+Algorithm+"&idx="+idx);
			}
		}else{
			try {
				Conditions = preprocess.conditionSplit(param);
				System.out.println(Conditions.size());
				for(int i=0; i<Conditions.size(); i++){
					System.out.println(Conditions.get(i).getName());
				}
				String epl = preprocess.Preprocessing(Conditions);
				Constant.EPL = epl;
				System.out.println(epl);

				//실시간 return 
//				System.out.println("실시간 수행할거야..!!!!");
				//api로 할때는 한 창에서 모두 가능
				ResultFileWriter resultFileWriter = new ResultFileWriter();
				resultFileWriter.start();

				response.sendRedirect("./user/broadcast.jsp");
			} catch (Exception e) {
				e.printStackTrace();
			}


		}
	}


}
