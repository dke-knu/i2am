package knu.cs.dke.prog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.HashFunction;
import knu.cs.dke.prog.util.filter.BloomFilter;
import knu.cs.dke.vo.Condition;


public class Preprocessing {
	private static String Dataset;
	private static String DataSet = null;
	private static String Algorithm = null;
	private static String[] Conditions = null;
	private static String epl = null;
	private String epl2 = null;
	
	private List<String> numericType = Arrays.asList("createdAt");
	private List<String> arrayType = Arrays.asList("hashTag");
	
	
	//조건
	public List<Condition> conditionSplit(String[] param){
//		Condition[] conditions = new Condition[param.length];
		List<Condition> conditions = new ArrayList<Condition>();
		int conCount = 0;
		for(int i=0; i<param.length; i++){
//			System.out.println();
		}
		if(!param[0].isEmpty()){
			System.out.println("param length:"+param.length);
			for(int i=0; i<param.length;i++){
				if(!param[i].equals("empty")){
					if(param[i].contains("!")){
						System.out.println("-----1");
						System.out.println(param[i]);
						String[] con = param[i].split("!=");
						conditions.add(new Condition(con[0],con[1],"!="));
						conCount++;
					}else if(param[i].contains("<")){
						System.out.println("-----2");
						String[] con = param[i].split("<");
						conditions.add(new Condition(con[0],con[1],"<"));
						conCount++;
					}else if(param[i].contains(">")){
						System.out.println("------3");
						String[] con = param[i].split(">");
						conditions.add(new Condition(con[0],con[1],">"));
						conCount++;
					}else{
						System.out.println("-------equal~!");	
						String[] con = param[i].split("=");
						conditions.add(new Condition(con[0],con[1],"="));
						conCount++;
					}
				}
			}
		}
		//hashTag 유무 확인, 존재하면 가장 앞으로 이동
		for(int i=0; i<conditions.size();i++){
			if(conditions.get(i).getName().contains("hashTag")){
				Collections.swap(conditions, 0, i);
			}
		}
		
		return conditions;
	}
	
	//샘플링
	public void Preprocessing(String dataset, String algorithm){
		
	}
	
	//필터링
	public String Preprocessing( List<Condition> conditions){
		String query = null;
		//데이터 셋
		Dataset = Constant.Dataset+"Event";
		
		//알고리즘과 조건에 맞는 쿼리
		if(Constant.Dataset.equals("Twitter")){
			if(Constant.Algorithm.equals("bloom")){ 	//bloom filter
				String params = "";
				//각 조건들 hash함수들에 적용
				for(int i=0; i<conditions.size();i++){
//					params += ",HashFunction.HashFunction1("+conditions.get(i).getName()+")";
//					params += ",HashFunction.HashFunction2("+conditions.get(i).getName()+")";
					params += ","+conditions.get(i).getName();
				}
				params = params.replaceFirst(",", "");
				query = "BloomFilter.isExist(HashFunction.hash("+params+"))";
				System.out.println(conditions.size());
				
				//bloom filter에 해시 값 true로 set
				BloomFilter bf = new BloomFilter();
				bf.BloomFilter = new boolean[900];
				Arrays.fill(bf.BloomFilter, false);
				for(int i=0; i<conditions.size();i++){
					System.out.println("value:"+HashFunction.HashFunction1(conditions.get(i).getValue()));
					bf.BloomFilter[HashFunction.HashFunction1(conditions.get(i).getValue())] = true;
					bf.BloomFilter[HashFunction.HashFunction2(conditions.get(i).getValue())] = true;
					bf.BloomFilter[HashFunction.HashFunction3(conditions.get(i).getValue())] = true;
				}
			}else if(Constant.Algorithm.equals("bayesian")){	//bayesian filter
				System.out.println("bayesian filter");
				query = "BayesianFilter.filter(userName,lang,text)";
			}else{	//query filter
				epl2 = "select * from "+Dataset+" where ";
				String params = "";
				for(Condition con : conditions){
					if(numericType.contains(con.getName())){
						params += "and "+con.getName()+con.getOperator()+con.getValue()+" ";
					}else if(arrayType.contains(con.getName())){
						//hash tag 
						params +="and text like '%#"+con.getValue()+"%' ";
					} else if(con.getName().equals("text")){
						//text
						params +="or text like '%"+con.getValue()+"%' ";
					} else{
						if(con.getOperator().contains("!=")) {
//							con.setOperator("=");
							String tmp = "=";
							System.out.println("here!");
							params += "or "+con.getName()+tmp+"'"+con.getValue()+"' ";
						} else {
							System.out.println("here??");
						params += "and "+con.getName()+con.getOperator()+"'"+con.getValue()+"' ";
						}
					}
				}
				query = params.replaceFirst("and ", "");
				query = query.replaceFirst("or ", "");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! "+epl2+query);
				return epl2+query;
			}
		} else{
			//가우시안 - query filter만
			epl2 = "select * from "+Dataset+" where ";
			String params = "";
			for(Condition con : conditions){
				// > to <=, < to >=
				System.out.println("condition name :"+con.getName());
				if(con.getOperator().equals("<")){
					con.setOperator(">=");
				}else if(con.getOperator().equals(">")){
					con.setOperator("<=");
				}else{
					con.setOperator("<>");
				}
				// <-1 or >1
				params += "or "+con.getName()+con.getOperator()+con.getValue()+" ";
			}
			query = params.replaceFirst("and ", "");
			query = query.replaceFirst("or ", "");
			return epl2+query;
		}
//		epl = "select * from "+Dataset+"("+query+").win:length(1)";
		epl = "select * from "+Dataset+"("+query+")";
		System.out.println(epl);
		return epl;
	}
}
