package ch.inftec.ju.db;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import ch.inftec.ju.db.DbRowUtils.DbRowBuilder;
import ch.inftec.ju.db.DbRowUtils.DbRowsImpl;

/**
 * Implementation of the ResultSetHandler interface that yields a DbRows instance containing
 * all the rows of the ResultSet.
 * @author Martin
 *
 */
class DbRowResultSetHandler implements ResultSetHandler<DbRowsImpl> {

	@Override
	public DbRowsImpl handle(ResultSet rs) throws SQLException {
		DbRowsImpl dbRows = new DbRowsImpl();
		
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			if (rsmd == null) rsmd = rs.getMetaData();
			
			DbRowBuilder rowBuilder = DbRowUtils.newDbRow();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				rowBuilder.addValue(rsmd.getColumnName(i), rsmd.getColumnType(i), this.processValue(rs.getObject(i)));
			}
			dbRows.addRow(rowBuilder.getRow());
		}
		
		// Set base row if query yielded no rows
		if (dbRows.getRowCount() == 0) {
			DbRowBuilder rowBuilder = DbRowUtils.newDbRow();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				rowBuilder.addValue(rsmd.getColumnName(i), rsmd.getColumnType(i), null);
			}
			dbRows.setBaseRow(rowBuilder.getRow());
		}
		
		return dbRows;
	}
	
	/**
	 * Processes the value returned by the DB (if necessary) so it matches a
	 * Java base type.
	 * <p>
	 * For instance, a Clob object will be converted to a String.
	 * @param obj Object to be processed
	 * @return Processes object as a Java base type
	 */
	private Object processValue(Object obj) {
		if (obj instanceof Clob) {
			Clob clob = (Clob)obj;
			return JuDbUtils.getClobString(clob);			
		} else {
			return obj;
		}
	}

}
