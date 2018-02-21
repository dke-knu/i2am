package knu.cs.dke.vo;

public class TrainingResult {
	private String word = null;
	private int spamCount = 0;
	private int hamCount = 0;
	private double ws = 0.0;
	private double wh = 0.0;

	
	public TrainingResult(String word, int spamCount, int hamCount, double ws, double wh) {
		super();
		this.word = word;
		this.spamCount = spamCount;
		this.hamCount = hamCount;
		this.ws = ws;
		this.wh = wh;

	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getSpamCount() {
		return spamCount;
	}
	public void setSpamCount(int spamCount) {
		this.spamCount = spamCount;
	}
	public int getHamCount() {
		return hamCount;
	}
	public void setHamCount(int hamCount) {
		this.hamCount = hamCount;
	}
	public double getWs() {
		return ws;
	}
	public void setWs(double ws) {
		this.ws = ws;
	}
	public double getWh() {
		return wh;
	}
	public void setWh(double wh) {
		this.wh = wh;
	}

}
