package ch.inftec.ju.db;

public interface DbQueryRunner {
	/**
	 * Executes a select query and returns the result as an array of DbRow instances.
	 * @param query Select query to be executed
	 * @param params Parameters that will substitute ? place holders in the query
	 * @return Array of DbRow instances
	 * @throws JuDbException If the query fails
	 */
	public DbRows query(String query, Object... params) throws JuDbException;
	
	/**
	 * Executes a DB update and returns the result value.
	 * @param query Update query to be executed
	 * @param params Parameters that will substitute ? place holders in the query
	 * @return Value as returned by the database
	 * @throws JuDbException If the update fails
	 */
	public int update(String query, Object... params) throws JuDbException;
	
	/**
	 * Executes a primary key query on the specified row.
	 * @param tableName Table name
	 * @param primaryKeyValue Primary key value
	 * @return DbRow instance of the column with the specified key or null if no such column can be found
	 * @throws JuDbException If the query cannot be performed or more than one row is returned
	 */
	public DbRow primaryKeyQuery(String tableName, Object primaryKeyValue) throws JuDbException;
	
	/**
	 * Executes an empty rows query on the specified row, i.e. a query that returns no row, but initializes
	 * the DbRow result instance with the appropriate columns.
	 * @param tableName TableName
	 * @return DbRow of the specified table containing no rows
	 * @throws JuDbException If the query cannot be performed
	 */
	public DbRow emptyRowQuery(String tableName) throws JuDbException;
	
	/**
	 * Gets an instance of a DbAction that performs an update. If the row with the specified
	 * primary key cannot be found, null is returned.
	 * @param tableName Table name
	 * @param primaryKeyValue Primary key value
	 * @return DbAction instance to update the specified row or null if the row doesn't exist
	 * @throws JuDbException If the update action cannot be prepared
	 */
	public DbAction getUpdateAction(String tableName, Object primaryKeyValue) throws JuDbException;
	
	/**
	 * Gets an instance of a DbAction that performs an insert.
	 * @param tableName Table name
	 * @return DbAction instance to insert a row into the specified table
	 * @throws JuDbException If the insert action cannot be created
	 */
	public DbAction getInsertAction(String tableName) throws JuDbException;
	
	/**
	 * Gets an instance of a DbAction that performs a delete. If the row with the specified
	 * primary key cannot be found, null is returned.
	 * @param tableName Table name
	 * @param primaryKeyValue Primary key value
	 * @return DbAction instance to delete the specified row or null if the row doesn't exist
	 * @throws JuDbException If the delete action cannot be prepared
	 */
	public DbAction getDeleteAction(String tableName, Object primaryKeyValue) throws JuDbException;
}
