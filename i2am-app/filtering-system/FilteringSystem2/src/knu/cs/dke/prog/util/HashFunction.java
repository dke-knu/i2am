package knu.cs.dke.prog.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

public class HashFunction {
	static int bloomFilterSize = 900;

	public static List<Integer> hash(String original){
		List<Integer> hashed = new ArrayList<Integer>();
		hashed.add(HashFunction1(original));
		hashed.add(HashFunction2(original));
		hashed.add(HashFunction3(original));
		
		return hashed;
	}
	public static List<Integer> hash(ArrayList<String> origin_list){
		List<Integer> hashed = new ArrayList<Integer>();
		
		if(origin_list != null){
			for(int i=0; i<origin_list.size();i++){
				hashed.add(HashFunction1(origin_list.get(i)));
				hashed.add(HashFunction2(origin_list.get(i)));
				hashed.add(HashFunction3(origin_list.get(i)));
			}
		}
		return hashed;
	}
	
	public static List<Integer> hash(ArrayList<String> origin_list,String original){
		List<Integer> hashed = new ArrayList<Integer>();
		hashed.add(HashFunction1(original));
		hashed.add(HashFunction2(original));
		hashed.add(HashFunction3(original));
		if(origin_list != null){
			for(int i=0; i<origin_list.size();i++){
				hashed.add(HashFunction1(origin_list.get(i)));
				hashed.add(HashFunction2(origin_list.get(i)));
				hashed.add(HashFunction3(origin_list.get(i)));
			}
		}
		
		return hashed;
	}
	public static List<Integer> hash(String origin_1, String origin_2){
		List<Integer> hashed = new ArrayList<Integer>();
		hashed.add(HashFunction1(origin_1));
		hashed.add(HashFunction2(origin_1));
		hashed.add(HashFunction1(origin_2));
		hashed.add(HashFunction2(origin_2));
		
		return hashed;
	}
	//조건 세개일때
	public static List<Integer> hash(String origin_1, String origin_2, String origin_3){
		List<Integer> hashed = new ArrayList<Integer>();
		hashed.add(HashFunction1(origin_1));
		hashed.add(HashFunction2(origin_1));
		hashed.add(HashFunction3(origin_1));
		hashed.add(HashFunction1(origin_2));
		hashed.add(HashFunction2(origin_2));
		hashed.add(HashFunction3(origin_2));
		hashed.add(HashFunction1(origin_3));
		hashed.add(HashFunction2(origin_3));
		hashed.add(HashFunction3(origin_3));
		
		return hashed;
	}
	public static List<Integer> hash(ArrayList<String> origin_list, String origin_1, String origin_2){
		List<Integer> hashed = new ArrayList<Integer>();
		hashed.add(HashFunction1(origin_1));
		hashed.add(HashFunction2(origin_1));
		hashed.add(HashFunction3(origin_1));
		hashed.add(HashFunction1(origin_2));
		hashed.add(HashFunction2(origin_2));
		hashed.add(HashFunction3(origin_2));
		if(origin_list != null){
			for(int i=0; i<origin_list.size();i++){
				hashed.add(HashFunction1(origin_list.get(i)));
				hashed.add(HashFunction2(origin_list.get(i)));
				hashed.add(HashFunction3(origin_list.get(i)));
			}
		}
		
		return hashed;
	}
	
	
	
	
	
	
	
	//java에서 제공하는 String HashFunction
	public static int HashFunction1(String original){
		int hashed = original.hashCode()%bloomFilterSize;
		if(hashed<0) hashed *= -1;
		return hashed; 
	}
	public static int HashFunction1(int original){
		String str_original = original+"";
		int hashed = str_original.hashCode()%bloomFilterSize;
		if(hashed<0) hashed *= -1; 
		return hashed; 
	}
	public static int HashFunction1(double original){
		String str_original = original+"";
		int hashed = str_original.hashCode()%bloomFilterSize;
		if(hashed<0) hashed *= -1;
		return hashed;
	}
	//twitter의 hashTag 와 같이 한번에 여러개
	public static List<Integer> HashFunction1(ArrayList<String> origin_list){
		//list로 들어온 데이터들 해시..
		List<Integer> hashed_list = new ArrayList<Integer>();
		for(int i=0; i<origin_list.size();i++){
			int hashed = origin_list.get(i).hashCode()%bloomFilterSize;
			if(hashed<0) hashed *= -1;
			hashed_list.add(hashed);
		}
		return hashed_list;
	}
	
	//기수변환으로 만든 HashFunction
	public static int HashFunction2(String original){
		int hashed;
//		if(original.length()>0){
		String numString = stringFormat(original);	
		
		hashed = Integer.parseInt(numString)%bloomFilterSize;

		return hashed; 
	}
	public static int HashFunction2(int original){
		
		String numString = stringFormat(original+"");
		int hashed;
		
		hashed = Integer.parseInt(numString)%bloomFilterSize;
	
		return hashed; 
	}
	public static int HashFunction2(double original){
		
		String numString = stringFormat(original+"");
		int hashed;
		
		hashed = Integer.parseInt(numString)%bloomFilterSize;
	
		return hashed; 
	}
	
	public static List<Integer> HashFunction2(ArrayList<String> origin_list){
		List<Integer> hashed_list = new ArrayList<Integer>();
		
		for(int i=0;i<origin_list.size();i++){
			int hashed;
			String numString = stringFormat(origin_list.get(i));	

			hashed = Integer.parseInt(numString)%bloomFilterSize;

			hashed_list.add(hashed);
		}
		return hashed_list;
	}
	//xxhash
	public static int HashFunction3(String original) {
		int hashed=0;
		XXHashFactory factory = XXHashFactory.fastestInstance();

		byte[] data;
		try {
			data = original.getBytes("euc-kr");
			
			XXHash32 hash32 = factory.hash32();
			int seed = 0x9747b28c; // used to initialize the hash value, use whatever
			// value you want, but always the same
			hashed = hash32.hash(data, 0, data.length, seed)%bloomFilterSize;
			if(hashed<0){
				hashed *= -1;
			}
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashed;
		
	}
	public static int HashFunction3(int original) {
		String str_original = original+"";
		int hashed=0;
		XXHashFactory factory = XXHashFactory.fastestInstance();

		byte[] data;
		try {
			data = str_original.getBytes("euc-kr");
			
			XXHash32 hash32 = factory.hash32();
			int seed = 0x9747b28c; // used to initialize the hash value, use whatever
			// value you want, but always the same
			hashed = hash32.hash(data, 0, data.length, seed)%bloomFilterSize;
			if(hashed<0){
				hashed *= -1;
			}
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashed;
		
	}
	
	//etc
	private static String reverseString(String s){
		return (new StringBuffer(s)).reverse().toString();
	}
	
	private static String stringFormat(String str){
		String result="";
		byte[] bytes = str.getBytes();  
		for (byte b : bytes) {  
			String s = String.format("%02X", b);
			int b1 =Integer.parseInt(s, 16);
			result +=b1;
		}
		return result;
	}
	
}

//public static int HashFunction2(String original){
//	int hashed;
//	if(original.length()>0){
//	String numString = stringFormat(original.substring(0,1)+original.substring(original.length()-1));	
//	
//	if (numString.length()<2){
//		hashed = Integer.parseInt(numString);
//	}else{
//		int standard = (numString.length()/2);
//		
//		hashed = Integer.parseInt(reverseString(numString.substring(standard-1, standard+1)));
//		
//	}
//	} else {
//		hashed = original.length();
//	}
//
//	return hashed; 
//}
//public static List<Integer> HashFunction2(ArrayList<String> origin_list){
//	List<Integer> hashed_list = new ArrayList<Integer>();
//	
//	for(int i=0;i<origin_list.size();i++){
//		int hashed;
//		String numString = stringFormat(origin_list.get(i).substring(0,1)+origin_list.get(i).substring(origin_list.get(i).length()-1));	
//		
//		if (numString.length()<2){
//			hashed = Integer.parseInt(numString);
//		}else{
//			int standard = (numString.length()/2);
//			
//			hashed = Integer.parseInt(reverseString(numString.substring(standard-1, standard+1)));
//			
//		}
//		hashed_list.add(hashed);
//	}
//	return hashed_list;
//}