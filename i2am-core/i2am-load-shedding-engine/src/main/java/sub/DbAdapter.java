package sub;

import java.sql.*;

public class DbAdapter {
    private static final Class<?> klass = (new Object() {}).getClass().getEnclosingClass();

    // singleton
    private volatile static DbAdapter instance;
    public static DbAdapter getInstance() {
        if(instance == null) {
            synchronized(DbAdapter.class) {
                if(instance == null) {
                    instance = new DbAdapter();
                }
            }
        }
        return instance;
    }

    protected DbAdapter() {}

    private Connection cn;

    // DB Connection
    protected Connection getConnection() throws SQLException {
        String driverName = "org.mariadb.jdbc.Driver";
        String url = "jdbc:mariadb://localhost:3306/tutorial";
        String user = "root";
        String password = "1234";

        try {

            Class.forName(driverName);
//            this.cn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/tutorial","root","1234");
            this.cn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Load error: " + e.getStackTrace());
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getStackTrace());
        }
        return cn;
    }

    protected void close(Connection con) throws SQLException {
        con.close();
    }

    public boolean getSwtichValue(final String topic){
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;

        boolean switchValue = false;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("SELECT value FROM SWITCH WHERE topicName=?");
            pstmt.setString(1, topic);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                boolean id = rs.getBoolean("value");
                return id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return switchValue;
    }

    public void setSwicthValue(final String topic, String switchValue){
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("UPDATE switch SET value=? WHERE topicName=?");
            pstmt.setString(1, switchValue);
            pstmt.setString(2, topic);
            pstmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
