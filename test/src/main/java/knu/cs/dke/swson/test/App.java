package knu.cs.dke.swson.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	    try {
	        String url = "http://10.61.11.141:8080/ics-api/v1.0/account/token";

	        URL obj = new URL(url);
	        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);
			conn.setDoInput(true);

	        conn.setRequestMethod("POST");
	        conn.connect();

	        String data = "{\"auth\":{\"username\":\"athene\",\"password\":\"athene\"}}";
	      
	        OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
	        
	        wr.write(data.toString());
	        wr.flush();
	        
	        StringBuilder sb = new StringBuilder();  
	        
	        int HttpResult =conn.getResponseCode(); 
	      
	        if(HttpResult ==HttpURLConnection.HTTP_OK){
	      
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));  
	      
	            String line = null;  
	      
	            while ((line = br.readLine()) != null) {  
	             sb.append(line + "\n");  
	            }  
	     
	            br.close();  
	      
	            System.out.println(""+sb.toString());  
	      
	        }else{
	            System.out.println(conn.getResponseMessage());  
	        }  
	       }
	       catch (Exception e) {
	        e.printStackTrace();
	       }
	       finally {
	        
	       }
	}

}