package ch.inftec.ju.util.change;

import java.util.ArrayList;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.DbRowUtils;
import ch.inftec.ju.db.DbRowUtils.DbRowBuilder;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.XString;

/**
 * DbRow based implementation of a DbAction that performs an insert of a row.
 * @author tgdmemae
 *
 */
class InsertDbRowDbAction extends AbstractDbRowDbAction {
	/**
	 * Type name for TypeHandler when creating Memento.
	 */
	@SuppressWarnings("unused")
	private static final String MEMENTO_TYPE_NAME = "Insert";

	/**
	 * Creates a new InsertDbRowDbAction based on the specified DbRow.
	 * @param dbConn DbConnection instance
	 * @param row DbRow instance
	 * @param tableName Name of the row's table
	 */
	InsertDbRowDbAction(DbConnection dbConn, DbRow row, String tableName) {
		super(dbConn, row, tableName);
	}
	
	@Override
	public void execute() {
		XString insertQry = null;
		try {
			insertQry = new XString("INSERT INTO " + this.getTableName() + " (");
			
			ArrayList<Object> values = new ArrayList<Object>();
			XString valuesQry = new XString("VALUES (");
			for (int i = 0; i < this.getRow().getColumnCount(); i++) {
				String columnName = this.getRow().getColumnName(i);
				insertQry.assertText("(", ", ");
				insertQry.addText(columnName);
				valuesQry.assertText("(", ", ");
				valuesQry.addText("?");
				values.add(this.getVal(columnName).getValue());
			}
			insertQry.addText(") ");
			insertQry.addText(valuesQry);
			insertQry.addText(")");
						
			int res = this.getDbConnection().getQueryRunner().update(insertQry.toString(), (Object[])values.toArray(new Object[0]));
			if (res != 1) throw new JuDbException("Execution of query returned " + res + ", expected 1: " + insertQry);
		} catch (Exception ex) {
			throw new JuRuntimeException("Failed to execute insert: " + insertQry, ex);
		}
	}

	@Override
	public DbAction createUndoAction() {
		DbRowBuilder targetRowBuilder = DbRowUtils.newDbRow();
		for (int i = 0; i < this.getRow().getColumnCount(); i++) {
			String columnName = this.getRow().getColumnName(i);
			targetRowBuilder.addValue(columnName, this.getRow().getColumnType(i), this.getVal(columnName).getValue());
		}
		
		DbAction undoAction = new DeleteDbRowDbAction(this.getDbConnection(), targetRowBuilder.getRow(), this.getTableName());
		
		return undoAction;
	}
}
