package filter;

import java.util.ArrayList;
import java.util.List;

public class HashFunction {

	private int bloomFilterSize;	

	public HashFunction(int size) {
		this.bloomFilterSize = size;
	}

	public List<Integer> Hash(String input) {
		
		List<Integer> hashed = new ArrayList<Integer>();		
		
		hashed.add(HashFunction1(input));
		hashed.add(HashFunction2(input));
		hashed.add(HashFunction3(input));
		
		return hashed;
	}
	
	public int HashFunction1(String input) {				
		int hashed_key = Math.abs(input.hashCode());
		
		return hashed_key%bloomFilterSize;		
	}

	public int HashFunction2(String input) {

		int b = 378551;
		int a = 63689;
		int hashed_key = 0;		
		char[] keys = input.toCharArray();		

		for( int i=0; i<keys.length; i++ ) {
			hashed_key = hashed_key * a + keys[i];
			a    = a * b;
		}		
		hashed_key = Math.abs(hashed_key);
		
		return hashed_key%bloomFilterSize;
	}

	public int HashFunction3(String input) {	

		int hashed_key = 1315423911;		
		char[] keys = input.toCharArray();		
		
		for( int i=0; i<keys.length; i++ ) {
			hashed_key ^= ((hashed_key << 5) + keys[i] + (hashed_key >> 2));
		}
		hashed_key = Math.abs(hashed_key);		

		return hashed_key%bloomFilterSize;
	}
}
