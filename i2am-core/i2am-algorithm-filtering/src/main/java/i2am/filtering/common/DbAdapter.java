package i2am.filtering.common;

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

//    private static final String GETTARGETINDEXQUERY = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = (SELECT F_TARGET FROM  WHERE F_TOPOLOGY = (SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
    private static final String GETBLOOMHASHFUNCTIONQUERY = "SELECT HASH_FUNCTION1, HASH_FUNCTION2, HASH_FUNCTION3 FROM tbl_params_bloom_filtering WHERE F_TOPOLOGY = (SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?)";

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

    public int getTargetIndex(String topologyName, String algorithmName) throws SQLException {
        String query = null;
        int targetIndex = 0;
        connection = ds.getConnection();
        switch (algorithmName) {
            case "BLOOM_FILTERING":
                query = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = " +
                        "(SELECT F_TARGET FROM tbl_params_bloom_filtering WHERE F_TOPOLOGY = " +
                        "(SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
                break;
            case "KALMAN_FILTERING":
                query = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = " +
                        "(SELECT F_TARGET FROM tbl_params_kalman_filtering WHERE F_TOPOLOGY = " +
                        "(SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
                break;
            case "NR_KALMAN_FILTERING":
                query = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = " +
                        "(SELECT F_TARGET FROM tbl_params_noise_recommend_kalman_filtering WHERE F_TOPOLOGY = " +
                        "(SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
                break;
            case "I_KALMAN_FILTERING":
                query = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = " +
                        "(SELECT F_TARGET FROM tbl_params_intelligent_kalman_filtering WHERE F_TOPOLOGY = " +
                        "(SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
                break;
        }
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            targetIndex = resultSet.getInt("COLUMN_INDEX");
        }
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return targetIndex; // MariDB's target index start from 1
    }

    public String[] getBloomHashFunction(String topologyName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETBLOOMHASHFUNCTIONQUERY);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        String hashFunctions[] = new String[3];
        if(resultSet.next()) {
            hashFunctions[0] = resultSet.getString("HASH_FUNCTION1");
            hashFunctions[1] = resultSet.getString("HASH_FUNCTION2");
            hashFunctions[2] = resultSet.getString("HASH_FUNCTION3");
        }
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return hashFunctions;
    }
}
