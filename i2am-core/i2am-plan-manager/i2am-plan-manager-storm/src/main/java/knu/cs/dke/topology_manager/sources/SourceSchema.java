package knu.cs.dke.topology_manager.sources;

public class SourceSchema {

	private int columnIndex;
	private String columnName;
	private String columnType;
	
	public SourceSchema(int columnIndex, String columnName, String columnType) {
		
		this.columnIndex = columnIndex;
		this.columnName = columnName;
		this.columnType = columnType;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
}
