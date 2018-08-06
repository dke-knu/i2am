package i2am.query.parser;

import java.util.Arrays;
import java.util.Map;

public class Node {
	private Node l;
	private Operator op;
	private Node r;
	private String leaf;
	
	public Node (Node l, Operator op, Node r) {
		this.l = l;
		this.op = op;
		this.r = r;
	}
	
	public Node getLeft() {
		return this.l;
	}
	
	public Operator getOperator() {
		return this.op;
	}
	
	public Node getRight() {
		return this.r;
	}

	public Node (String leaf) {
		this.leaf = leaf;
	}
	
	public String getLeaf() {
		assert leaf != null;
		return this.leaf;
	}
	
	public boolean evaluate (Map<String, String> tuple) {
		return getOperator().evaluate(getLeft(), getRight(), tuple);
	}
	
	public static Node parse(String query) {
		String[] splitted = query.split(" ");
		if ( splitted.length == 1 ) {
			return new Node(query);
		} else if ( splitted.length == 5 ) {
			return new Node(
					parse(splitted[1]), new Operator(splitted[2]), parse(splitted[3]) );
		} else {
			int opIdx = 0;
			for (int i=1, skip=0; i<splitted.length-1; i++) {
				if ( splitted[i].equals("(") )	skip++;
				else if ( splitted[i].equals(")") && --skip==0 ) {
					opIdx = i + 1;
					break;
				}
			}
			String left = String.join(" ", Arrays.copyOfRange(splitted, 1, opIdx));
			String right = String.join(" ", Arrays.copyOfRange(splitted, opIdx+1, splitted.length-1));
			
			return new Node(
						parse(left), new Operator(splitted[opIdx]), parse(right) );
		}
	}

	@Override
	public String toString() {
		if (this.leaf != null) return this.leaf;
		else return String.join(" ", 
				"(", this.l.toString(), this.op.toString(), this.r.toString(), ")");
		
	}
}
