package knu.cs.dke.vo;

import java.util.ArrayList;

public class TwitterEvent {
	private String UserName = null;
	private String UserId = null;
	private long CreatedAt = 0;
	private String Lang = null;
	private String Text = null;
	private ArrayList<String> HashTag = null;
	
	public TwitterEvent(String userName, String userId, long createdAt, String lang, String text,
			ArrayList<String> hashTag) {
		super();
		UserName = userName;
		UserId = userId;
		CreatedAt = createdAt;
		Lang = lang;
		Text = text;
		HashTag = hashTag;
		
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public long getCreatedAt() {
		return CreatedAt;
	}
	public void setCreatedAt(long createdAt) {
		CreatedAt = createdAt;
	}
	public String getLang() {
		return Lang;
	}
	public void setLang(String lang) {
		Lang = lang;
	}
	public String getText() {
		return Text;
	}
	public void setText(String text) {
		Text = text;
	}
	public ArrayList<String> getHashTag() {
		return HashTag;
	}
	public void setHashTag(ArrayList<String> hashTag) {
		HashTag = hashTag;
	}

}
