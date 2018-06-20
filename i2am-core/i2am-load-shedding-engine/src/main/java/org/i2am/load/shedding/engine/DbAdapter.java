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

        boolean switchValue = false;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("SELECT * FROM SWITCH");
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String tn = rs.getString("topicName");
                boolean sv = rs.getBoolean("value");
                System.out.println("TN: "+tn+", sv: "+sv);

                map.put(tn,sv);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public boolean getSwtichValue(final String topic) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;

        boolean switchValue = false;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("SELECT value FROM SWITCH WHERE topicName=?");
            pstmt.setString(1, topic);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                switchValue = rs.getBoolean("value");
                return switchValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return switchValue;
    }

    //method Overloading
    public void setSwitchValue(String switchValue) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;
        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("UPDATE switch SET value=?");
            pstmt.setString(1, switchValue);
            pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSwicthValue(String topic, String switchValue) {
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
