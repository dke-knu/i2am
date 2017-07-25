package filter;

import java.util.List;

public class BloomFilter {
	
	// Array의 크기?
	private boolean[] filter;
	private HashFunction hash;
	
	// 몇개의 함수를 적용할 것인가?	
	
	public BloomFilter(int filter_size) {	
		
		this.filter = new boolean[filter_size];
		this.hash = new HashFunction(filter_size);
	}
		
	public void add(String input) {		
		
		boolean exist = isExist(input);
		
		if(exist!=true) {		
			List<Integer> hash_keys = hash.Hash(input);
			
			for( int i=0; i<hash_keys.size(); i++ ) {			
				filter[hash_keys.get(i)] = true;				
			}
			System.out.println("필터 등록!");
			
		} else {
			System.out.println("이미 존재하는 단어이다!");
		}
	}
	
	public boolean isExist(String input) {
		
		int boolean_count = 0;		
		List<Integer> hash_keys = hash.Hash(input);			
		
		for( int i=0; i<hash_keys.size(); i++) {
			
			//System.out.println(hash_keys.get(i));
			
			if( filter[hash_keys.get(i)] == true )
				boolean_count += 1;			
		}		
		
		if (boolean_count == hash_keys.size()) // 존재하면 TRUE, 없으면 FALSE.
			return true;
		else
			return false;
	}

	public void Show() {		
		
		for ( int i=0; i<filter.length; i++ ) {
			System.out.print(filter[i]+" ");
		}
		System.out.println();
		
	}	
}
