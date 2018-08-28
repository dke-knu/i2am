DB_INFO = {'HOST':'host',
           'PORT':0000,
           'USER':'user',
           'PASSWD':'password',
           'DB':'db name'}

QUERY_DIC = {'GET_FILE_PATH':"SELECT FILE_PATH FROM tbl_src_test_data WHERE IDX = ( SELECT F_TEST_DATA FROM tbl_src WHERE NAME = %s AND F_OWNER = ( SELECT IDX FROM tbl_user WHERE ID = %s))",
             'GET_TOPIC_NAME':"SELECT TRANS_TOPIC FROM tbl_src WHERE (NAME = %s) AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s)",
             'PUT_SELECTED_ALGO':"UPDATE tbl_src SET RECOMMENDED_SAMPLING = %s WHERE NAME = %s AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s)",
             'GET_TARGET_IDX':"SELECT column_index FROM tbl_src_csv_schema WHERE f_src = ( SELECT idx FROM tbl_src WHERE name = %s AND f_owner = (SELECT idx FROM tbl_user WHERE id = %s))"}