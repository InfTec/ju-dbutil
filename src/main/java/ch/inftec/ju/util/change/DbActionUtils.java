package ch.inftec.ju.util.change;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.DbRows;
import ch.inftec.ju.db.JuDbException;

/**
 * Helper class to build and handle DbActions.
 * @author tgdmemae
 *
 */
public final class DbActionUtils {
	/**
	 * Gets a new UpdateActionBuilder for the specified row, using a primaryKeyValue.
	 * @param dbConn DbConnection
	 * @param tableName Table name
	 * @param primaryKeyValue Primary key value
	 * @return AbstractActionBuilder instance to build the action
	 * @throws IllegalArgumentException If the specified row cannot be found
	 */
	public static AbstractActionBuilder newUpdateAction(DbConnection dbConn, String tableName, Object primaryKeyValue) {
		DbRow row = dbConn.getQueryRunner().primaryKeyQuery(tableName, primaryKeyValue);
		if (row == null) throw new IllegalArgumentException("No row found on table " + tableName + " with ID " + primaryKeyValue);
		
		return new UpdateActionBuilder(dbConn, row, tableName);
	}
	
	/**
	 * Creates a new UpdateActionBuilder for the specified row.
	 * @param dbConn DbConnection
	 * @param row Row to be updated
	 * @param tableName Table name of the row
	 * @return AbstractActionBuilder instance to build the action
	 */
	public static AbstractActionBuilder newUpdateAction(DbConnection dbConn, DbRow row, String tableName) {
		return new UpdateActionBuilder(dbConn, row, tableName);
	}
	
	/**
	 * Gets a new InsertActionBuilder for the specified row.
	 * @param dbConn DbConnection
	 * @param tableName Table name
	 * @param primaryKeyValue Primary key value
	 * @return AbstractActionBuilder instance to build the action
	 * @throws IllegalArgumentException If the specified row cannot be found
	 */
	public static AbstractActionBuilder newInsertAction(DbConnection dbConn, String tableName) {
		return new InsertActionBuilder(dbConn, tableName);
	}
	
	/**
	 * Gets a new Delete Action for the specified row, using a primaryKeyValue.
	 * @param dbConn DbConnection
	 * @param tableName Table name
	 * @param primaryKeyValue Primary key value
	 * @return DbAction instance to delete the row
	 * @throws IllegalArgumentException If the specified row cannot be found
	 */
	public static DbAction newDeleteAction(DbConnection dbConn, String tableName, Object primaryKeyValue) {
		DbRow row = dbConn.getQueryRunner().primaryKeyQuery(tableName, primaryKeyValue);
		if (row == null) throw new IllegalArgumentException("No row found on table " + tableName + " with ID " + primaryKeyValue);
		
		return DbActionUtils.newDeleteAction(dbConn, row, tableName);
	}
	
	
	/**
	 * Creates a new Delete Action for the specified row.
	 * @param dbConn DbConnection
	 * @param row Row to be deleted
	 * @param tableName Table name of the row
	 * @return DbAction instance to delete the row
	 */
	public static DbAction newDeleteAction(DbConnection dbConn, DbRow row, String tableName) {
		return new DeleteDbRowDbAction(dbConn, row, tableName);
	}
	
	/**
	 * Builder to create DbActions.
	 * @author tgdmemae
	 *
	 */
	public static abstract class AbstractActionBuilder {
		private final DbAction action;
		
		private AbstractActionBuilder(DbAction action) {
			this.action = action;
		}
		
		/**
		 * Sets the value of the specified column.
		 * @param columnName Column name
		 * @param value Value of the column
		 * @return This builder to allow for chaining
		 */
		public AbstractActionBuilder setValue(String columnName, Object value) {
			this.action.setValue(columnName, value);
			return this;
		}
		
		/**
		 * Gets the action that was built using this builder.
		 * @return
		 */
		public final DbAction getAction() {
			return this.action;
		}
		
		/**
		 * Executes a select * query on the specified table that returns no rows. Can be used
		 * to obtain an empty DbRows instance.
		 * @param tableName Table name
		 * @return DbRows instance with no rows
		 * @throws JuDbException If the query fails
		 */
		protected static DbRows emptyRowsQuery(DbConnection dbConn, String tableName) throws JuDbException {
			String selectQry = "SELECT * FROM " + tableName + " WHERE 1=0";
			return dbConn.getQueryRunner(). query(selectQry);
		}
	}
	
	/**
	 * Builder to create update DbActions.
	 * @author tgdmemae
	 *
	 */
	private static final class UpdateActionBuilder extends AbstractActionBuilder {
		private UpdateActionBuilder(DbConnection dbConn, DbRow row, String tableName) {
			super(new UpdateDbRowDbAction(dbConn, row, tableName));
		}
	}
	
	/**
	 * Builder to create insert DbActions.
	 * @author tgdmemae
	 *
	 */
	private static final class InsertActionBuilder extends AbstractActionBuilder {
		private InsertActionBuilder(DbConnection dbConn, String tableName) {
			super(new InsertDbRowDbAction(dbConn, dbConn.getQueryRunner().emptyRowQuery(tableName), tableName));
		}
	}
}
