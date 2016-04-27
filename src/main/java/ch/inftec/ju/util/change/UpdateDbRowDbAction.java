package ch.inftec.ju.util.change;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.DbRowUtils;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.DbRowUtils.DbRowBuilder;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.XString;

/**
 * DbRow based implementation of a DbAction that performs an update of a row.
 * @author tgdmemae
 *
 */
class UpdateDbRowDbAction extends AbstractDbRowDbAction {
	/**
	 * Type name for TypeHandler when creating Memento.
	 */
	@SuppressWarnings("unused")
	private static final String MEMENTO_TYPE_NAME = "Update";

	/**
	 * Creates a new UpdateDbRowDbAction based on the specified DbRow.
	 * @param dbConn DbConnection instance
	 * @param row DbRow instance
	 * @param tableName Name of the row's table
	 */
	UpdateDbRowDbAction(DbConnection dbConn, DbRow row, String tableName) {
		super(dbConn, row, tableName);
	}
	
	@Override
	public void execute() {
		XString updateQry = null;
		try {
			Val[] changedColumns = this.getChangedColumns();
			if (changedColumns.length < 1) return;
			
			Object[] changedVals = new Object[changedColumns.length + 1];
			
			updateQry = new XString("UPDATE " + this.getTableName() + " SET ");
			
			int i = 0;
			for (Val changedColumn : changedColumns) {
				updateQry.assertText("SET ", ", ");
				updateQry.addText(changedColumn.getColumnName() + "=?");
				changedVals[i] = changedColumns[i].getChangedValue();
				i++;
			}
			
			updateQry.addText(" WHERE " + this.getPrimaryKeyValue().getColumnName() + "=?");
			changedVals[i] = this.getPrimaryKeyValue().getOriginalValue();
			
			int res = this.getDbConnection().getQueryRunner().update(updateQry.toString(), changedVals);
			if (res != 1) throw new JuDbException("Execution of query returned " + res + ", expected 1: " + updateQry);
		} catch (Exception ex) {
			throw new JuRuntimeException("Failed to execute update: " + updateQry, ex);
		}
	}

	@Override
	public DbAction createUndoAction() {
		DbRowBuilder targetRowBuilder = DbRowUtils.newDbRow();
		for (int i = 0; i < this.getRow().getColumnCount(); i++) {
			String columnName = this.getRow().getColumnName(i);
			targetRowBuilder.addValue(columnName, this.getRow().getColumnType(i), this.getVal(columnName).getValue());
		}
		
		DbAction undoAction = new UpdateDbRowDbAction(this.getDbConnection(), targetRowBuilder.getRow(), this.getTableName());
		
		for (Val val : this.getChangedColumns()) {
			undoAction.setValue(val.getColumnName(), val.getOriginalValue());
		}
		
		return undoAction;
	}
}
