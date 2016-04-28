package ch.inftec.ju.util.change;


/**
 * ChangeItem sub type that allows to execute an action on a DB.
 * <p>
 * The setValue method may have different effects on different implementations.
 * <p>
 * An action must be executable by a single SQL statement and must
 * be undoable.
 * 
 * @author tgdmemae
 *
 */
public interface DbAction extends PersistableChangeItem {
	/**
	 * Sets the value of the specified column.
	 * @param columName Column name
	 * @param value Value of the column
	 * @return An instance to itself to enable cascading of setValue
	 */
	public DbAction setValue(String columName, Object value);
	
	/**
	 * Gets the DbActionValue information for the specified column. This info contains the
	 * original and (if set) the new value for a column.
	 * @param columnName Column name
	 * @return DbActionValue instance for the specified column
	 */
	public DbActionValue getVal(String columnName);
	
	/**
	 * Creates an action that undos all changes performed by this action.
	 * @return Undo action
	 */
	public DbAction createUndoAction();
	
	/**
	 * Executes the DB action using the specified connection.
	 * The action does not perform a commit itself.
	 * <p>
	 * If the action does not actually change data on the database, the execute method
	 * should return false without performing any DB actions.
	 * @param conn Connection to execute the DB update
	 * @param dbConn DbConnection instance to get database meta data. DON'T use the DbConnection to execute
	 * the update, though!
	 * @return True if data was changed on the DB, false otherwise
	 * @throws JuDbException If the execute cannot be performed
	 */
	//public boolean execute(Connection conn, DbConnection dbConn) throws JuDbException;
	
	/**
	 * Gets whether the is actually changing data on the database.
	 * @param dbConn DbConnection instance to get database meta data.
	 * @return True if the action would change data on the database, false otherwise
	 * @throws JuDbException If the evaluation fails
	 */
	//public boolean isChanging(DbConnection dbConn) throws JuDbException;
}
