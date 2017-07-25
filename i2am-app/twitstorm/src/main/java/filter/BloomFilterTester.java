package filter;

public class BloomFilterTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		BloomFilter bf = new BloomFilter(16);
		
		bf.add("apple");
		bf.Show();
		
		bf.add("banana");
		bf.Show();
		
		bf.add("fineapple");
		bf.Show();
		
		bf.add("apple");
		bf.Show();
		
		
	}

}
