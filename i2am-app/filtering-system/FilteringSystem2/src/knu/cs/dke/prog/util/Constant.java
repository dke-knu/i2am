package knu.cs.dke.prog.util;

import java.io.BufferedWriter;
import java.util.ArrayList;

import javax.websocket.Session;

import knu.cs.dke.vo.TwitterEvent;
import knu.cs.dke.websocket.BroadCaster;
import twitter4j.TwitterStream;
import knu.cs.dke.prog.StreamTwitterInput;
import knu.cs.dke.prog.esper.EsperEngine;

public class Constant {
	public static boolean FirstStart = true;
	
	public static String Dataset = null;
	public static String Algorithm = null;
	public static String OutputType = null;
	public static String EPL = null;
	public static String InputFileName = null;
	public static String InputType = null;
	public static int TwitterTotal = 1878; //임시
	public static ArrayList<TwitterEvent> BayesianResult = new ArrayList<TwitterEvent>();
	public static BufferedWriter FileWriter = null;
	public static BroadCaster BroadCaster = null;
	public static Session UserSession = null;
	
	
	
	public static EsperEngine EsperEngine = null;
	
	public static StreamTwitterInput StreamTwitterInput = null;
	
	//twitter API keys
	public static final String ACCESS_TOKEN = "762929783063715841-Yc0EBbsL6zLl9s4pw3OMfxz8pI1pVjr";
	public static final String ACCESS_SECRET = "TJ2a3wJY9wrGZ5Du6QJkfzrVEMX547SBEMUOOU2hrosqc";
	public static final String CONSUMER_KEY = "3dJTXIjttxIhZQpg71mJGGXCA";
	public static final String CONSUMER_SECRET = "MwOvJO3T8c76oDUyAfeihhVSZtcY1xv32IxcOH1BS99ntKb5fS";

	//client -> server / server -> client 파일 업로드 다운로드 경로
	public static final String UploadRoute = "D:\\IITP_Esper\\FilteringSystem\\InputFile\\";
	public static final String DownloadRoute = "D:\\IITP_Esper\\FilteringSystem\\ResultFile\\";
	
}
