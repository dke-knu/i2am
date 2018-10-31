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

    public void initMethod(Map<Source, Boolean> map) {
        Connection con = null;
        PreparedStatement stmt = null;
        String sql = null;

        try {
            con = this.getConnection();
            stmt = con.prepareStatement("SELECT * FROM tbl_src, tbl_user WHERE F_OWNER=tbl_user.IDX AND USES_LOAD_SHEDDING='Y'");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String srcName = rs.getString("NAME");
                String userId = rs.getString("ID");
                boolean value = rs.getString("SWITCH_MESSAGING").equals("N") ? false : true;
                map.put(new Source(srcName, userId), value);
                System.out.println("srcName: " + srcName + ", userId: " + userId +", value: "+value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSwicthValue(Source source, String switchValue) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = this.getConnection();
            stmt = con.prepareStatement("SELECT * FROM tbl_user WHERE ID=?");
            stmt.setString(1, source.getUserId());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String userIdx = rs.getString("IDX");

            stmt = con.prepareStatement("UPDATE tbl_src SET SWITCH_MESSAGING=? WHERE NAME=? and F_OWNER=?");
            stmt.setString(1, switchValue);
            stmt.setString(2, source.getSrcName());
            stmt.setString(3, userIdx);
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

//    public String getSrcId(String userId, String srcName){
//        Connection con = null;
//        Statement stmt = null;
//        String sql = null;
//        String srcId="";
//        try {
//            con = this.getConnection();
//            stmt = con.createStatement();
//            sql = "select * from tbl_src where NAME = '"+ srcName +"' and F_OWNER in (select IDX from tbl_user where ID = '"+ userId  +"')";
//            ResultSet rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                srcId = rs.getString("IDX");
////                System.out.println("src: " + srcId );
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (stmt != null) {
//                    stmt.close();
//                }
//                if (con != null) {
//                    con.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return srcId;
//    }

    public boolean addLog(Source source, String message) {

        Connection con = null;
        PreparedStatement stmt = null;
        String sql = null;
        try {
            con = this.getConnection();
            stmt = con.prepareStatement("SELECT * FROM tbl_user WHERE ID=?");

            stmt.setString(1, source.getUserId());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String userIdx = rs.getString("IDX");

            sql = "INSERT INTO tbl_log (f_user, logging_type, logging_message) VALUES" +
                    " (" + "(SELECT F_OWNER FROM tbl_src WHERE NAME ='" + source.getSrcName() +
                    "' AND F_OWNER='"+userIdx+"') , '" + "INFO" + "' , '" +
                    (message + "(" + source.getSrcName() + ")") + "')";

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
