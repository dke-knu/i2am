package i2am.Common;

import java.sql.*;

public class DbAdapter {
    private String host;
    private String databaseName;
    private String userID;
    private String password;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public DbAdapter(){
        host = "jdbc:mariadb://114.70.235.43:3306/";
        databaseName = "i2am";
        userID = "plan-manager";
        password = "dke214";
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(host + databaseName, userID, password);
    }
}
