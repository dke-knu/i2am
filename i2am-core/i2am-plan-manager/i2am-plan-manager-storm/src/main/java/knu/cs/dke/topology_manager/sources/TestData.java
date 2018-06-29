package knu.cs.dke.topology_manager.sources;

public class TestData {

	private String onwer;
	private String fileName;
	private String createdTime;	
	private String filePath;
	private int fileSize;
	private String fileType; // CSV, XML, JSON ... 

	public TestData(String owner, String fileName, String createdTime, String filePath, int fileSize, String fileType ) {		
		this.onwer = owner;
		this.fileName = fileName;
		this.createdTime = createdTime;
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.fileType = fileType;		
	}

	public String getOnwer() {
		return onwer;
	}

	public void setOnwer(String onwer) {
		this.onwer = onwer;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}	
	
}
