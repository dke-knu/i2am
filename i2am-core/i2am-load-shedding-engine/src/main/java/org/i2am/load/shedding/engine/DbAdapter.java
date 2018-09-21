package org.i2am.load.shedding.engine;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DbAdapter {
    private static final Class<?> klass = (new Object() {
    }).getClass().getEnclosingClass();
    private Map<String, String> conf = new HashMap<String, String>();

    // singleton
    private volatile static DbAdapter instance;

    public static DbAdapter getInstance(Map<String, String> conf) {
        if (instance == null) {
            synchronized (DbAdapter.class) {
                if (instance == null) {
                    instance = new DbAdapter(conf);
                }
            }
        }
        return instance;
    }

    protected DbAdapter(Map<String, String> conf) {
        this.conf = conf;
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

    public void initMethod(Map<String, Boolean> map) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;

        try {
            con = this.getConnection();
            pstmt = con.prepareStatement("SELECT * FROM tbl_src WHERE USES_LOAD_SHEDDING='Y'");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String srcIdx = rs.getString("IDX");
                boolean value = rs.getString("SWITCH_MESSAGING").equals("N") ? false : true;
                map.put(srcIdx, value);
                System.out.println("src: " + srcIdx + ", value: " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSwicthValue(String srcName, String switchValue) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = this.getConnection();
            stmt = con.prepareStatement("UPDATE tbl_src SET SWITCH_MESSAGING=? WHERE NAME=?");
            stmt.setString(1, switchValue);
            stmt.setString(2, srcName);
            stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSrcId(String userId, String srcName){
        Connection con = null;
        Statement stmt = null;
        String sql = null;
        String srcId="";
        try {
            con = this.getConnection();
            stmt = con.createStatement();
            sql = "select IDX from tbl_src where NAME = '"+ srcName +"' and F_OWNER in (select IDX from tbl_user where ID = '"+ userId  +"')";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            srcId = rs.getString("IDX");


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return srcId;
    }

    public boolean addLog(String srcIdx, String message) {

        Connection con = null;
        Statement stmt = null;
        String sql = null;
        try {
            con = this.getConnection();
            stmt = con.createStatement();
            sql = "SELECT NAME FROM tbl_src WHERE IDX =" + srcIdx;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String srcName = rs.getString("NAME");

            sql = "INSERT INTO tbl_log (f_user, logging_type, logging_message) VALUES (" + "(SELECT F_OWNER FROM tbl_src WHERE IDX ='" + srcIdx + "') , '" + "INFO" + "' , '" +
                    (message + "(" + srcName + ")") + "')";

            rs = stmt.executeQuery(sql);

            if (rs.next())
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


}
