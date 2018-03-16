package knu.cs.dke.vo;

//필터링시 조건들
public class Condition {
	private String name = null;
	private String value = null;
	private String operator = null;
	
	public Condition(String name, String value, String operator) {
		super();
		this.name = name;
		this.value = value;
		this.operator = operator;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
