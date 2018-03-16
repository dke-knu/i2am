package knu.cs.dke.vo;

//안쓰고있음.. 안쓸듯...?
public class ConditionLog {
	private int idx = 0;
	private String lang = null;
	private String keywords = null;
	private String createdAt = null;
	
	public ConditionLog(int idx, String lang, String keywords, String createdAt) {
		super();
		this.idx = idx;
		this.lang = lang;
		this.keywords = keywords;
		this.createdAt = createdAt;
	}

	public int getIdx() {
		return idx;
	}
	public void setIdx(int idx) {
		this.idx = idx;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	
	

}
