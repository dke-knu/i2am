package org.i2am.load.shedding.engine;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DbAdapter {
    private static final Class<?> klass = (new Object() {}).getClass().getEnclosingClass();
    private Map<String, String> conf = new HashMap<String,String>();

    // singleton
    private volatile static DbAdapter instance;

    public static DbAdapter getInstance(Map<String,String> conf) {
        if (instance == null) {
            synchronized (DbAdapter.class) {
                if (instance == null) {
                    instance = new DbAdapter(conf);
                }
            }
        }
        return instance;
    }

    protected DbAdapter(Map<String,String> conf) {
        this.conf=conf;
    }

    private Connection cn;

    // DB Connection
    protected Connection getConnection() throws SQLException {
        String driverName = conf.get("driverName");
        String url = conf.get("url");
        String user = conf.get("user");
        String password = conf.get("password");

        try {
            Class.forName(driverName);
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

    public void initMethod(Map<String,Boolean> map){
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("SELECT * FROM tbl_src WHERE USES_LOAD_SHEDDING='Y'");

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String srcName = rs.getString("NAME");
                boolean value =  rs.getString("SWITCH_MESSAGING").equals("N")? false : true;
                map.put(srcName,value);
                System.out.println("src: "+srcName+", value: "+value);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void setSwicthValue(String srcName, String switchValue) {
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("UPDATE tbl_src SET SWITCH_MESSAGING=? WHERE NAME=?");
            pstmt.setString(1, switchValue);
            pstmt.setString(2, srcName);
            pstmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
