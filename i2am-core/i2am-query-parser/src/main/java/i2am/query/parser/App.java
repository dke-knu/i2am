package i2am.query.parser;

import java.util.HashMap;
import java.util.Map;

public class App 
{
    public static void main( String[] args )
    {
//        String query1 = "( C1 NGT 1 )";
//        String query2 = "( ( C1 NGT 1 ) AND ( C2 NEQ 1 ) )";
//        String query3 = "( ( ( C1 NGT 1 ) AND ( C2 NEQ 1 ) ) AND ( C1 NLT 2 ) )";
//        String query4 = "( ( C1 NGT 1 ) AND ( ( C1 NLT 2 ) AND ( C2 NEQ 1 ) ) )";
//        String query5 = "( ( ( C1 NGT 1 ) AND ( C2 NEQ 1 ) ) AND ( ( C2 NGT 0 ) AND ( C3 NEQ 1 ) ) )";
//        Node n1 = Node.parse(query1);
//        System.out.println(n1.toString().equals(query1));
//        Node n2 = Node.parse(query2);
//        System.out.println(n2.toString().equals(query2));
//        Node n3 = Node.parse(query3);
//        System.out.println(n3.toString().equals(query3));
//        Node n4 = Node.parse(query4);
//        System.out.println(n4.toString().equals(query4));
//        Node n5 = Node.parse(query5);
//        System.out.println(n5.toString().equals(query5));
//        
//        DbAdapter.getInstance().addQuery(n5, "test@a.b", "src01", "topology01");
//        System.out.println(n5);
//        n5 = DbAdapter.getInstance().getQuery("topology01");
//        System.out.println(n5);
//
//        // evaluation test
//        String[] schema = DbAdapter.getInstance().getSchema("topology01");
//        
//        String tuple = "2,0,1";
//        String[] splittedTuple = tuple.split(",");
//        Map<String, String> tupleMap = new HashMap<String, String>(); 
//        for (int i=0; i<splittedTuple.length; i++) {
//        	tupleMap.put(schema[i], splittedTuple[i]);
//        }
//        System.out.println(n5.evaluate(tupleMap));
    	
//    	String query = "( ( ( ( C1 NLT -1 ) OR ( C1 NGT 1 ) ) AND ( C2 TIN hello ) ) AND ( C3 FROM 1521126000 ) )";
//    	Node node = Node.parse(query);
//    	DbAdapter.getInstance().addQuery(node, "test@a.b", "src01", "topology01");
    	
    	Node node = DbAdapter.getInstance().getQuery("topology01");
    	
    	String[] schema = DbAdapter.getInstance().getSchema("topology01");
    	String tuple = "2,hello world,1521126001";
    	String[] splittedTuple = tuple.split(",");
    	Map<String, String> tupleMap = new HashMap<String, String>(); 
    	for (int i=0; i<splittedTuple.length; i++) {
    		tupleMap.put(schema[i], splittedTuple[i]);
    	}
    	
    	System.out.println(node.evaluate(tupleMap));
    }
}
