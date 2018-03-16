package dbTester;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTester {

	// JDBC driver name and Database URL
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
	static final String DB_URL = "jdbc:mariadb//127.0.0.1/Unnamed";

	// Database user Info.			
	static final String user = "user";
	static final String password = "1234";


	public static void main(String[] args)  {

		Connection connection = null;
		Statement stmt = null;

		try {

			// Register JDBC driver
			Class.forName("org.mariadb.jdbc.Driver");

			// Open a Connection
			System.out.println("Connecting to a selected database...");

			connection = DriverManager.getConnection("jdbc:mariadb://127.0.0.1/test_database", "user1", "1234");
			System.out.println("Connected database successfully...");

			//STEP 4: Execute a query
			System.out.println("Creating table in given database...");
			stmt = connection.createStatement();

			String query = "select * from test_table";

			stmt.executeUpdate(query);
			
			ResultSet result = stmt.getResultSet();
			ResultSetMetaData rsmd = result.getMetaData();
			
			int columns = rsmd.getColumnCount();
						
			while(result.next()) {				
				for (int i = 1; i <= columns; i++) {
			           if (i > 1) System.out.print(",  ");
			           String columnValue = result.getString(i);
			           System.out.print(columnValue + " " + rsmd.getColumnName(i));
			       }
			       System.out.println("");				
			}
			
			System.out.println("Created table in given database...");
			
		} catch( SQLException se ) {
			se.printStackTrace();
		} catch( Exception e ) {
			e.printStackTrace();
		} finally {
			
			try {
				if (stmt != null ) {
					stmt.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
			try {
				if (connection != null ) {
					connection.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		System.out.println("[DB] Good Bye!");
	}
}
