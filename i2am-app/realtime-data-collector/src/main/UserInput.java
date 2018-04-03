package main;
import java.util.HashMap;
import java.util.Map;

public class UserInput {
	
	private String URL;
	private String serviceKey=null;
	private Map<String, Object> serviceKeyMap;
	private Map<String ,String> serviceKeyList = new HashMap<String, String>();
	
	private Map<String, Object> urlHeader; 
	private Map<String, String> urlHeaderList = new HashMap<String, String>();
	
	private Map<String, Object> parameter=null;	
	private Map<String, String> parameterList = new HashMap<String, String>();	
	
	private String timeInterval;
	private String directory;
	private String filename;
	private String format = "xml";
		
	public String getURL() { return URL;}
	public String getServiceKey() { return serviceKey; }
	public String getTimeInterval() { return timeInterval;}
	public String getDirectory() { return directory;}
	public String getFilename() { return filename;}
	public String getFormat() { return format;}
	
	public Map<String, String> getParameter() {
		if(parameter==null) return null;
		for(String key: parameter.keySet()) {
			String value = (String) parameter.get(key);
			parameterList.put(key, value);		
		}
		return parameterList;
	}
	
	public Map<String, String> getserviceKeyMap() {
		if(serviceKeyMap == null) return null;
		for(String key:serviceKeyMap.keySet()) {
			String value = (String)serviceKeyMap.get(key);
			serviceKeyList.put(key, value);			
		}
		return serviceKeyList;
	}
	
	public Map<String, String> geturlHeader() {
		if(urlHeader == null) return null;		
		for(String key:urlHeader.keySet()) {
			String value = (String)urlHeader.get(key);
			urlHeaderList.put(key, value);			
		}
		return urlHeaderList;
	}	
	
	public void setURL(Object url) { this.URL = (String) url; }
	public void setTimeInterval(Object time) { this.timeInterval = (String) time; }
	public void setDirectory(Object dir) { this.directory = (String) dir; }
	public void setFilename(Object file) { this.filename = (String) file; }
	public void setFormat(Object format) { this.format = (String) format; }
	
	@SuppressWarnings("unchecked")
	public boolean setServiceKey(Object serviceKey) {
		if (serviceKey instanceof String) {
			this.serviceKey = (String) serviceKey;
			return true;
		} else {
			this.serviceKeyMap = (Map<String, Object>) serviceKey;
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public void setParameter(Object param) { this.parameter = (Map<String, Object>) param; }
	
	@SuppressWarnings("unchecked")
	public void seturlHeader(Object urlHeader) { this.urlHeader = (Map<String, Object>) urlHeader; }
	
}
