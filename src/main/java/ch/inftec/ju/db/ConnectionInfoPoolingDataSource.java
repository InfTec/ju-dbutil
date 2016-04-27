package ch.inftec.ju.db;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import ch.inftec.ju.util.AssertUtil;

public class ConnectionInfoPoolingDataSource extends PoolingDataSource {
	private Logger logger = LoggerFactory.getLogger(ConnectionInfoPoolingDataSource.class);
	
	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		Properties props = new Properties();
		
		// If class name hasn't been set explicitly, try to evaluate it
		if (StringUtils.isEmpty(this.getClassName())) {
			String className = null;
			if (connectionInfo.getConnectionString().startsWith("jdbc:derby:")) {
				className = "org.apache.derby.jdbc.EmbeddedXADataSource";
			} else if (connectionInfo.getConnectionString().startsWith("jdbc:h2:")) {
				className = "org.h2.jdbcx.JdbcDataSource";
			} else if (connectionInfo.getConnectionString().startsWith("jdbc:hsqldb:")) {
				className = "org.hsqldb.jdbcDriver";
			} else if (connectionInfo.getConnectionString().startsWith("jdbc:mysql:")) {
				className = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
			} else if (connectionInfo.getConnectionString().startsWith("jdbc:oracle:")) {
				className = "oracle.jdbc.xa.client.OracleXADataSource";
			}
			
			if (className != null) {
				logger.debug("Evaluated XA DataSource className: {}", className);
				this.setClassName(className);
			}
		}
		
		// The driver properties must match methods of the DataSource class
		
		AssertUtil.assertNotNull("ClassName attribute cannot be evaluated and must be set explicitly", this.getClassName());
		
		// Derby only has the databaseName property, no URL property for XA.
		if (this.getClassName().startsWith("org.apache.derby.")) {
			String dbName = connectionInfo.getConnectionString();
			if (dbName.startsWith("jdbc:derby:")) {
				dbName = dbName.substring("jdbc:derby:".length());
			}
			props.put("databaseName", dbName);
		// The other drivers have an URL property
		} else {
			props.put("URL", connectionInfo.getConnectionString());
		}
		
		if (StringUtils.isNotEmpty(connectionInfo.getUserName())) {
			props.put("user", connectionInfo.getUserName());
		}
		if (StringUtils.isNotEmpty(connectionInfo.getPassword())) {
			props.put("password", connectionInfo.getPassword());
		}
		
		this.setDriverProperties(props);
		
		// Set other properties
		this.setUniqueName(connectionInfo.getName());
	}
}
