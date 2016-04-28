package ch.inftec.ju.testing.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;

import ch.inftec.ju.db.JuDbException;

/**
 * Helper class used by {@link DbDataUtil} to construct data sets.
 * @author Martin
 *
 */
class ExportItems {
	private final List<ExportItem> items = new ArrayList<>();
	private List<String> casedTableNames = new ArrayList<>();
	
	/**
	 * Adds a table with an optional query
	 * @param tableName Table name
	 * @param query Optional query. May be null. If not, it has to be a complete query
	 * like <code>select * from table where id=?</code>
	 * @return This
	 */
	public ExportItems add(String tableName, String query) {
		this.items.add(new TableQueryExportItem(tableName, query));
		return this;
	}
	
	/**
	 * List that may contain table names the way we are supposed to write them (casing).
	 * @param casedTableNames
	 * @return This
	 */
	public ExportItems setCasedTableNames(List<String> casedTableNames) {
		this.casedTableNames = casedTableNames;
		return this;
	}
	
	/**
	 * Creates a data set based on the items added to this ExportItems instance.
	 * <p>
	 * If no items were added explicitly, we'll return a data set that
	 * comprises the whole DB.
	 * @param conn IDatabaseConnection instance
	 * @return IDataSet instance
	 */
	public IDataSet createDataSet(IDatabaseConnection conn) {
		if (this.items.size() > 0) {
			QueryDataSet dataSet = new QueryDataSet(conn, false);
			for (ExportItem item : this.items) {
				item.addToQueryDataSet(dataSet, casedTableNames);
			}
			return dataSet;
		} else {
			try {
				// Export whole DB
				return conn.createDataSet();
			} catch (Exception ex) {
				throw new JuDbException("Couldn't export whole DB");
			}
		}
	}
	
	interface ExportItem {
		void addToQueryDataSet(QueryDataSet dataSet, List<String> casedTableNames);
	}
	
	private static class TableQueryExportItem implements ExportItem {
		private final String tableName;
		private final String query;
		
		private TableQueryExportItem(String tableName, String query) {
			this.tableName = tableName;
			this.query = query;
		}
		
		@Override
		public void addToQueryDataSet(QueryDataSet dataSet, List<String> casedTableNames) {
			try {
				String actualTableName = this.tableName;
				for (String casedTableName : casedTableNames) {
					if (casedTableName.equalsIgnoreCase(this.tableName)) {
						actualTableName = casedTableName;
						break;
					}
				}
				
				dataSet.addTable(actualTableName, this.query);
			} catch (AmbiguousTableNameException ex) {
				throw new JuDbException(String.format("Couldn't add table %s to QueryDataSet: %s", this.tableName, this.query), ex);
			}
		}
	}
}
