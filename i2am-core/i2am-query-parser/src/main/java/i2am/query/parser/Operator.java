package i2am.query.parser;

import java.util.Arrays;
import java.util.Map;

public class Operator {
	private enum OPERATOR {TEQ, TNE, TIN, TNI, NEQ, NNE, NGT, NLT, NGE, NLE, FROM, TO, AND, OR};
    private final static OPERATOR[] RELATIONAL_OPERATOR = 
    	{OPERATOR.TEQ, OPERATOR.TNE, OPERATOR.TIN, 
         OPERATOR.TNI, OPERATOR.NEQ, OPERATOR.NNE, 
         OPERATOR.NGT, OPERATOR.NLT, OPERATOR.NGE, 
         OPERATOR.NLE, OPERATOR.FROM, OPERATOR.TO}; 
    private final static OPERATOR[] LOGICAL_OPERATOR = 
    	{OPERATOR.AND, OPERATOR.OR};

	private OPERATOR op;
	
	public Operator (String op) {
		this.op = OPERATOR.valueOf(op);
	}
	
	public boolean isRelationalOperator() { 
        return Arrays.asList(RELATIONAL_OPERATOR).contains(this.op);
	}
	
	public boolean isLogicalOperator() {
        return Arrays.asList(LOGICAL_OPERATOR).contains(this.op);
	}
	
	public static boolean isLogicalOperator(String op) {
        return Arrays.asList(LOGICAL_OPERATOR).contains(OPERATOR.valueOf(op));
	}
	
	public String toString() {
		return this.op.name();
	}

	public boolean evaluate(Node l, Node r, Map<String, String> tuple) {
		// for logical operator
		if (this.op.equals(OPERATOR.AND))
			return l.evaluate(tuple) 
					&& r.evaluate(tuple);
		else if (this.op.equals(OPERATOR.OR))
			return l.evaluate(tuple)
					|| r.evaluate(tuple); 
		
		// for text
		String textL = tuple.get(l.getLeaf());
		String textR = r.getLeaf();
		if (this.op.equals(OPERATOR.TEQ))
			return textL.equals(textR);
		else if (this.op.equals(OPERATOR.TNE))
			return !textL.equals(textR);
		else if (this.op.equals(OPERATOR.TIN))
			return textL.contains(textR);
		else if (this.op.equals(OPERATOR.TNI))
			return !textL.contains(textR);

		// for numeric
		double numericL = Double.parseDouble(textL);
		double numericR = Double.parseDouble(textR);
		if (this.op.equals(OPERATOR.NEQ))
			return numericL == numericR;
		else if (this.op.equals(OPERATOR.NNE))
			return numericL != numericR;
		else if (this.op.equals(OPERATOR.NGT))
			return numericL > numericR;
		else if (this.op.equals(OPERATOR.NLT))
			return numericL < numericR;
		else if (this.op.equals(OPERATOR.NGE))
			return numericL >= numericR;
		else if (this.op.equals(OPERATOR.NLE))
			return numericL <= numericR;
		
		// for timestamp
		else if (this.op.equals(OPERATOR.FROM))
			return numericL >= numericR;
		else if (this.op.equals(OPERATOR.TO))
			return numericL <= numericR;
		else
			throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}
}
