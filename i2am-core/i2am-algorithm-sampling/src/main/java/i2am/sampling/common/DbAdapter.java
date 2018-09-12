package i2am.sampling.common;

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

//    private static final String GETTARGETINDEXQUERY = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = (SELECT F_TARGET FROM ? WHERE F_TOPOLOGY = (SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
    private static final String GETHASHFUNCTIONQUERY = "SELECT HASH_FUNCTION FROM tbl_params_hash_sampling WHERE F_TOPOLOGY = (SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?)";

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
        connection = ds.getConnection();
        switch (algorithmName) {
            case "HASH_SAMPLING":
                query = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = " +
                        "(SELECT F_TARGET FROM tbl_params_hash_sampling WHERE F_TOPOLOGY = " +
                        "(SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
                break;
            case "PRIORITY_SAMPLING":
                query = "SELECT COLUMN_INDEX FROM tbl_src_csv_schema WHERE IDX = " +
                        "(SELECT F_TARGET FROM tbl_params_priority_sampling WHERE F_TOPOLOGY = " +
                        "(SELECT IDX FROM tbl_topology WHERE TOPOLOGY_NAME = ?))";
                break;
        }
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        int targetIndex = 0;
        if(resultSet.next())
            targetIndex = resultSet.getInt("COLUMN_INDEX");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return targetIndex; // MariDB's target index start from 0
    }

    public String getHashFunction(String topologyName) throws SQLException {
        connection = ds.getConnection();
        preparedStatement = connection.prepareStatement(GETHASHFUNCTIONQUERY);
        preparedStatement.setString(1, topologyName);
        resultSet = preparedStatement.executeQuery();
        String hashFunction = null;
        if(resultSet.next())
            hashFunction = resultSet.getString("HASH_FUNCTION");
        preparedStatement.close();
        resultSet.close();
        connection.close();
        return hashFunction;
    }
}