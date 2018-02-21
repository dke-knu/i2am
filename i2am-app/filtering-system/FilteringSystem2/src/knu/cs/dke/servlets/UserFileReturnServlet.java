package knu.cs.dke.servlets;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import knu.cs.dke.prog.esper.EsperEngine;
import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.ResultFileWriter;

/**
 * Servlet implementation class UserFileReturnServlet
 */
@WebServlet("/return_file")
public class UserFileReturnServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("do get");
		request.setCharacterEncoding("euc-kr");
		response.setContentType("text/html; charset=euc-kr");
		System.out.println("hey hello~~");
		response.setHeader("Refresh","0; url=http://114.70.235.39:9999/FilteringSystem/setting");
			try{
				if(!request.getParameter("file").isEmpty()){
					if(Integer.parseInt(request.getParameter("file")) == 1){
						if(Constant.InputType.equals("input_api")){
							Constant.StreamTwitterInput._twitterStream.shutdown();
							Constant.StreamTwitterInput = null;
						}
						System.out.println("------------file out!");

						ResultFileWriter resultFileWriter = new ResultFileWriter();
						resultFileWriter.finish();

						response.setContentType("application/octet-stream");
						response.setHeader("Content-Disposition","attachment;filename=result.txt");

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
						Constant.FileWriter = null;
						response.sendRedirect("./setting");
					}
				}
				Constant.EsperEngine.service.destroy();
				System.out.println( "Esper~~~");
				Constant.EsperEngine.service.removeAllServiceStateListeners();
				Constant.EsperEngine = null;
				Constant.BroadCaster.onClose(Constant.UserSession);


			}catch(Exception e){
				e.printStackTrace();

			}

	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().print("hello");
	}

}
