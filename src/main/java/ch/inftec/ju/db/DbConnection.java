package ch.inftec.ju.db;

import java.sql.Connection;
import java.util.List;


/**
 * Wrapper class around a plain JDBC connection providing helper functionality
 * on SQL level.
 * @author Martin
 *
 */
@Deprecated
public interface DbConnection {
	/**
	 * Gets the unique name of the DB connection.
	 * @return Connection name
	 */
	public String getName();
	
	/**
	 * Gets the Schema name used by this connection.
	 * @return Schema name or null if no Schema name is specified
	 */
	public String getSchemaName();

	/**
	 * Gets a list of all table names of the DB. Table names are all upper case.
	 * @return List of Table names
	 * @throws JuDbException If the list cannot be evaluated
	 */
	public List<String> getTableNames() throws JuDbException;
	
	/**
	 * Gets the name of the table's primary key column. Multi-Column primary keys are not supported.
	 * Column names are upper case.
	 * @param tableName Table name
	 * @return Name of the table's primary key
	 * @throws JuDbException If the primary key cannot be evaluated
	 */
	public String getPrimaryColumnName(String tableName) throws JuDbException;
	
	/**
	 * Gets all column names of the specified table in the order they are defined, or rather in the
	 * order the driver returns them. Column names are upper case.
	 * @param tableName Table name
	 * @return Column names of the table
	 * @throws JuDbException If the column names cannot be evaluated
	 */
	public List<String> getColumnNames(String tableName) throws JuDbException;
	
	/**
	 * Gets a DbQueryRunner instance that is based on this DbConnection.
	 * @return DbQueryRunner instance
	 */
	public DbQueryRunner getQueryRunner();
	
	/**
	 * Gets the raw JDBC connection of the current Spring transaction scope.
	 * <p>
	 * Make sure you use it in a Spring transaction scope and don't call any
	 * close or transaction methods explicitly.
	 * @return Connection instance
	 */
	public Connection getConnection();
}	
