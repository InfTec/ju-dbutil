package ch.inftec.ju.util.change;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.XString;

/**
 * DbRow based implementation of a DbAction that performs a delete of a row.
 * @author tgdmemae
 *
 */
class DeleteDbRowDbAction extends AbstractDbRowDbAction {
	/**
	 * Type name for TypeHandler when creating Memento.
	 */
	@SuppressWarnings("unused")
	private static final String MEMENTO_TYPE_NAME = "Delete";
	
	/**
	 * Creates a new DeleteDbRowDbAction based on the specified DbRow.
	 * @param dbConn DbConnection instance
	 * @param row DbRow instance
	 * @param tableName Name of the row's table
	 */
	DeleteDbRowDbAction(DbConnection dbConn, DbRow row, String tableName) {
		super(dbConn, row, tableName);
		
		// Set all values to null. This will make working with the delete action easier, for instance
		// when trying to access changed values
		for (int i = 0; i < this.getRow().getColumnCount(); i++) {
			String columnName = this.getRow().getColumnName(i);
			this.setValue(columnName, null);
		}
	}
	
	@Override
	public void execute() {
		XString deleteQry = null;
		try {
			deleteQry = new XString("DELETE FROM " + this.getTableName() + " WHERE " + this.getPrimaryKeyValue().getColumnName() + "=?");

			int res = this.getDbConnection().getQueryRunner().update(deleteQry.toString(), this.getPrimaryKeyValue().getOriginalValue());
			if (res != 1) throw new JuDbException("Execution of query returned " + res + ", expected 1: " + deleteQry);
		} catch (Exception ex) {
			throw new JuRuntimeException("Failed to execute delete: " + deleteQry, ex);
		}
	}

	@Override
	public DbAction createUndoAction() {
		DbAction undoAction = new InsertDbRowDbAction(this.getDbConnection(), this.getRow(), this.getTableName());
		return undoAction;
	}
}
