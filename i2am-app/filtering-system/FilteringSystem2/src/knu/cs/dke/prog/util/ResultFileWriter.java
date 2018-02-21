package knu.cs.dke.prog.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


//결과 파일 생성하는 Class
public class ResultFileWriter {
//	BufferedWriter fw = null;
	
	//file write start
	public void start(){
		String fileName=Constant.DownloadRoute+Constant.Algorithm+".txt";
		
		try{
			Constant.FileWriter = new BufferedWriter(new FileWriter(fileName));
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//file write end
	public void finish() throws IOException{
		Constant.FileWriter.close();
		Constant.FileWriter = null;
	}

}
