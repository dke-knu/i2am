package i2am.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.taskdefs.SQLExec.DelimiterType;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbAdmin { 
	private static Logger logger = LoggerFactory.getLogger((new Object() { /**/
	}).getClass().getEnclosingClass());
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		DbAdmin dbAdmin = DbAdmin.getInstance();
		dbAdmin.runScript(); 
	} 

	private final String db_driverClassName;
	private final String db_url;
	private final String db_dbname;
	private final String db_name;
	private final String db_password;
	private final String db_sql_delimiter;
	
	// singleton 
	private volatile static DbAdmin instance;
	public static DbAdmin getInstance() {
		if(instance == null) {
			synchronized(DbAdmin.class) {
				if(instance == null) {
					instance = new DbAdmin();
				}
			}
		}
		return instance;
	} 
	
	public DbAdmin() {
		Properties props = PropertiesFactory.getInstance().getObject();
		
		this.db_driverClassName = props.getProperty("db_driverClassName");

		this.db_url = props.getProperty("db_url");
		this.db_dbname = props.getProperty("db_dbname");
		this.db_name = props.getProperty("db_name");
		this.db_password = props.getProperty("db_password");

		this.db_sql_delimiter = ";";

	}

	public DataSource getDataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(db_driverClassName);
		ds.setUrl(db_url);
		ds.setUsername(db_name);
		ds.setPassword(db_password);
		return ds;
	}

	public void runScript() throws FileNotFoundException, IOException {
		String script_dir = "master/config/mariadb/";
		String filepaths[][] = {
			new String[]{script_dir+"db_user_drop.sql", "system", "false"},
			new String[]{script_dir+"db_user_create.sql", "system", "true"},
			new String[]{script_dir+"db_table_drop.sql", "user", "false"},
			new String[]{script_dir+"db_table_create.sql", "user", "true"},
			new String[]{script_dir+"db_table_populate.sql", "user", "true"}
		};

		for (int i = 0; i < filepaths.length; i++) {
			execute(filepaths[i]);
		}
	}
	
	private final class SqlExecuter extends SQLExec {
		private boolean throwException;

		public SqlExecuter(Project project, boolean throwException) {
			setProject(project);
			setTaskType("sql");
			setTaskName("sql");

			this.throwException = throwException;
		}

		@Override
		protected void execSQL(String sql, PrintStream out) throws SQLException {
			String sql2 = getProject().getGlobalFilterSet().replaceTokens(sql);
			if (logger.isDebugEnabled()) {
				logger.debug(sql2);
			}

			try {
				super.execSQL(sql2, out);
			} catch (SQLException e) {
				if (this.throwException) {
					logger.error("{}, {}", sql2, e);
					throw e;
				}
			}
		}
	}

	private void execute(String[] args) {

		///////////////////////
		// [1] �씤�옄 泥섎━
		///////////////////////
		String filepath = args[0];
		String user = args[1];
		boolean throwException = Boolean.valueOf(args[2]);
		logger.info("{}", filepath+", "+user+", "+throwException);
		String classpath = System.getenv("java.class.path");
		
		///////////////////////
		// [2] project �깮�꽦
		///////////////////////
		Project project = new Project();
		Path path = new Path(project, classpath);
		project.init();

		///////////////////////
		// [4] fileset �깮�꽦
		///////////////////////
		FileSet fileset = new FileSet();
		fileset.setProject(project);
		fileset.setDir(new File("./"));
		fileset.appendIncludes(new String[] {filepath});
		logger.info(fileset.getDir().getAbsoluteFile().toString());

		///////////////////////
		// [3] executer �깮�꽦
		///////////////////////
		SqlExecuter executer = new SqlExecuter(project, throwException);
		executer.setUrl(db_url);
		executer.setUserid(db_name);
		executer.setPassword(db_password);
		executer.setAutocommit(true); // for create database
		executer.setClasspath(path);
		executer.setDriver(db_driverClassName);
		executer.setDelimiter(db_sql_delimiter);
		executer.setDelimiterType((DelimiterType) EnumeratedAttribute.getInstance(DelimiterType.class, "row"));
		executer.addFileset(fileset);

		///////////////////////
		// [4] executer �떎�뻾
		///////////////////////
		executer.execute();
	}
}
