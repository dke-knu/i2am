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

    private static final String GETTARGETQUERY = "SELECT F_TARGET FROM tbl_intelligent_engine WHERE F_SRC = (SELECT F_SRC FROM tbl_plan WHERE IDX = (SELECT F_PLAN FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
    private static final String GETTARGETINDEXQUERY = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = ?";
    private static final String GETBLOOMHASHFUNCTIONQUERY = "SELECT HASH_FUNCTION1, HASH_FUNCTION2, HASH_FUNCTION3 FROM tbl_params_bloom_filtering WHERE IDX = (SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?)";

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

    public String getTarget(String topologyName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETTARGETQUERY);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        String targetName = resultSet.getString("F_TARGET");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return targetName;
    }

    public int getTargetIndex(String targetName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETTARGETINDEXQUERY);
        preparedStatement.setString(1, targetName);
        resultSet = preparedStatement.executeQuery();
        int targetIndex = resultSet.getInt("COLUMN_INDEX");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return targetIndex-1; // MariDB's target index start from 1
    }

    public String[] getBloomHashFunction(String topologyName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETBLOOMHASHFUNCTIONQUERY);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        String hashFunctions[] = new String[3];
        hashFunctions[0] = resultSet.getString("HASH_FUNCTION1");
        hashFunctions[1] = resultSet.getString("HASH_FUNCTION2");
        hashFunctions[2] = resultSet.getString("HASH_FUNCTION3");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return hashFunctions;
    }
}
