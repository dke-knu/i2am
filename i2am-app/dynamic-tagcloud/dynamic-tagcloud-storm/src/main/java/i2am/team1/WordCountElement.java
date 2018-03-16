package i2am.team1;

public class WordCountElement {
	protected String hashtag;
	protected Long count;
	protected String keyword;
	
	protected WordCountElement (String hashtag, Long count, String keyword) {
		this.hashtag = hashtag;
		this.count = count;
		this.keyword = keyword;
	}
	
//	protected String toJson () {
//		return "\"" + hashtag + "\"," + count + ",\"" + keyword + "\"";
//	}
}
