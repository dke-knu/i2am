package knu.cs.dke.prog.util.filter;

import java.util.List;

public class BloomFilter {
	public static boolean[] BloomFilter;
	
	public static boolean isExist(List<Integer> idx){
		boolean exist = true;
		for(int i=0; i<idx.size();i++){
			if(!BloomFilter[idx.get(i)]){ //하나라도 일치하지 않으면 false
				exist = false;
			}
		}
		return exist;
	}
	
}
