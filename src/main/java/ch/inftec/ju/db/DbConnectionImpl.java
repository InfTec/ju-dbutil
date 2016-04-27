package ch.inftec.ju.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import ch.inftec.ju.util.JuStringUtils;


/**
 * Implementation of the DbConnection interface.
 * <p>
 * Expects a DataSource and JdbcTemplate to be autowired by Spring.
 * @author Martin
 *
 */
final class DbConnectionImpl implements DbConnection {
	Logger log = LoggerFactory.getLogger(DbConnectionImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DataSource dataSource;
	
	private String name;
	private String schemaName;
	
//	/**
//	 * Creates a new connection creator with the specified name.
//	 * @param name Unique name of the connection
//	 * @param entityManagerFactory Factory used to created an EntityManager that will back
//	 * up the connection
//	 */
//	protected DbConnectionImpl(String name, String schemaName, EntityManagerFactory entityManagerFactory) {
//		this.name = name;
//		this.schemaName = schemaName;
//	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getSchemaName() {
		return this.schemaName;
	}

	@Override
	public List<String> getTableNames() throws JuDbException {
		DbMetaData md = new DbMetaData();
		return md.getTableNames();
	}
	
	@Override
	public String getPrimaryColumnName(String tableName) throws JuDbException {
		DbMetaData md = new DbMetaData();
		return md.getPrimaryColumnName(tableName);
	}

	@Override
	public List<String> getColumnNames(String tableName) throws JuDbException {
		DbMetaData md = new DbMetaData();
		return Arrays.asList(md.getColumnNames(tableName));			
	}
	
	@Override
	public Connection getConnection() {
		return DataSourceUtils.getConnection(this.dataSource);
	}
	
	/**
	 * Helper class to wrap around a DatabaseMetaData object.
	 * Make sure to call the close() method when the object is not used any longer.
	 * @author Martin
	 *
	 */
	private class DbMetaData {
		private DatabaseMetaData metaData;
		private ResultSet rs;
		
		/**
		 * Creates a new DbMetaData instance. If an exception occurs, close will be called
		 * implicitly.
		 * @throws JuDbException If the MetaData cannot be accessed
		 */
		public DbMetaData() throws JuDbException {
			try {
				this.metaData = DbConnectionImpl.this.getConnection().getMetaData();
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't access DatabaseMetaData", ex);
			}
		}
		
		public List<String> getTableNames() throws JuDbException {
			try {
				this.rs = this.metaData.getTables(getSchemaName(), null, null, new String[]{"TABLE"});
				
				List<String> tableNames = new ArrayList<>();
				while (rs.next()) {
					String tableName = rs.getString("TABLE_NAME");
					tableNames.add(tableName.toUpperCase());
				}
				
				Collections.sort(tableNames);
				
				return tableNames;
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate table names", ex);
			} finally {
				JuDbUtils.closeQuietly(this.rs);
				this.rs = null;
			}
		}
		
		/**
		 * Gets the primary column name for the specified table.
		 * @param tableName Table name
		 * @return Primary column name
		 * @throws JuDbException If the primary column name cannot be evaluated
		 */
		public String getPrimaryColumnName(String tableName) throws JuDbException {
			try {
				this.rs = this.metaData.getPrimaryKeys(null, null, tableName.toUpperCase());
				
				String columnName = null;
				if (rs.next()) columnName = rs.getString("COLUMN_NAME");
				else throw new JuDbException("Couldn't evaluate primary key for table " + tableName);
				
				if (rs.next()) {
					throw new JuDbException("Driver returned multiple primary keys for table " + tableName);
				}
				
				return columnName.toUpperCase();
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate primary key for table " + tableName, ex);
			} finally {
				JuDbUtils.closeQuietly(this.rs);
				this.rs = null;
			}
		}
		
		/**
		 * Gets all column names of the specified table in the order they are defined, or rather in the
		 * order the driver returns them.
		 * @param tableName Table name
		 * @return Column names of the table
		 * @throws JuDbException If the column names cannot be evaluated
		 */
		public String[] getColumnNames(String tableName) throws JuDbException {
			try {
				this.rs = this.metaData.getColumns(null, null, tableName.toUpperCase(), null);			
				
				ArrayList<String> columnNames = new ArrayList<String>();
				
				while (rs.next()) {
					String columnName = rs.getString("COLUMN_NAME");
					columnNames.add(columnName.toUpperCase());
				}
				
				if (columnNames.size() == 0) {
					throw new JuDbException("Couldn't evaluate column names for table " + tableName + ": Driver returned empty ResultSet.");
				}
				
				return (String[])columnNames.toArray(new String[0]);
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate primary key for table " + tableName, ex);
			} finally {
				JuDbUtils.closeQuietly(this.rs);
				this.rs = null;
			}
		}
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
}
