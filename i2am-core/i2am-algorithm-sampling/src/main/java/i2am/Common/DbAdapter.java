package i2am.Common;

import i2am.metadata.DbAdmin;

import javax.sql.DataSource;
import java.sql.*;

public class DbAdapter {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private final DbAdmin dbAdmin;
    private final DataSource ds;
    private volatile static DbAdapter instance;

    private static final String GETTARGETINDEXQUERY = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = (SELECT F_TARGET FROM tbl_intelligent_engine WHERE F_SRC = (SELECT F_SRC FROM tbl_plan WHERE IDX = (SELECT F_PLAN FROM tbl_topology WHERE TOPOLOGY_NAME = ?)))";
    private static final String GETHASHFUNCTIONQUERY = "SELECT HASH_FUNCTION FROM tbl_params_hash_sampling WHERE IDX = (SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?)";

    private DbAdapter(){
        dbAdmin = DbAdmin.getInstance();
        ds = dbAdmin.getDataSource();
    }

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

    public int getTargetIndex(String topologyName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETTARGETINDEXQUERY);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        int targetIndex = resultSet.getInt("COLUMN_INDEX");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return targetIndex-1; // MariDB's target index start from 1
    }

    public String getHashFunction(String topologyName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETHASHFUNCTIONQUERY);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        String hashFunction = resultSet.getString("HASH_FUNCTION");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return hashFunction;
    }
}