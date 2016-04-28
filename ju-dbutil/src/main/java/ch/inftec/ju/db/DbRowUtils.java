package ch.inftec.ju.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.JuStringUtils;

/**
 * Helper class for DbRow related objects.
 * @author tgdmemae
 *
 */
public final class DbRowUtils {
	/**
	 * Builder to create DbRow instances.
	 * @return DbRowBuilder
	 */
	public static DbRowBuilder newDbRow() {
		return new DbRowBuilder();
	}
	
	/**
	 * Builder to create intances of DbRow.
	 * @author tgdmemae
	 *
	 */
	public static final class DbRowBuilder {
		private DbRowImpl dbRow = new DbRowImpl();
		
		/**
		 * Adds a value to the DbRow.
		 * @param columnName Column name
		 * @param columnType Column type
		 * @param value Column value
		 * @return This builder to allow for chaining
		 */
		public DbRowBuilder addValue(String columnName, int columnType, Object value) {
			this.dbRow.addValue(columnName, columnType, value);
			return this;
		}
		
		/**
		 * Gets the row that was built using this builder.
		 * @return DbRow
		 */
		public DbRow getRow() {
			return this.dbRow;
		}
	}
	
	/**
	 * Implementation of the DbRow interface. Note that DbRow instances are
	 * to be immutable.
	 * @author Martin
	 *
	 */
	private static class DbRowImpl implements DbRow {
		/**
		 * Contains the column names in order of adding, converted to all upper case.
		 */
		private ArrayList<String> columnNames = new ArrayList<String>();
		
		/**
		 * Contains the corresponding type of the columns in the same order as the names.
		 */
		private ArrayList<Integer> columnTypes = new ArrayList<Integer>();
		
		/**
		 * Contains the corresponding values of the columns.
		 */
		private HashMap<String, Object> values = new HashMap<String, Object>();
		
		/**
		 * Cache of the rows hashCode.
		 */
		private Integer hashCode = null;
		
		/**
		 * Adds a new value to the row.
		 * @param columnName Column name. Will be converted to all upper case
		 * @param columnType Column type as returned by the ResultSetMetaData
		 * @param value Value of the column
		 */
		void addValue(String columnName, int columnType, Object value) {
			// We will convert the column to upper case here.
			columnName = columnName.toUpperCase();
			
			// Make sure we have no duplicate columns
			if (this.values.containsKey(columnName)) {
				throw new IllegalArgumentException("Duplicate column name: " + columnName);
			}
			
			this.columnNames.add(columnName);
			this.columnTypes.add(columnType);
			this.values.put(columnName, value);
			
			// Reset the HashCode (if any)
			this.hashCode = null;
		}
		
		@Override
		public Object getValue(String columnName) {
			if (columnName == null) return null;
			return this.values.get(columnName.toUpperCase());
		}

		@Override
		public int getColumnCount() {
			return this.columnNames.size();
		}

		@Override
		public String getColumnName(int index) {
			return this.columnNames.get(index);
		}

		@Override
		public int getColumnType(int index) {
			return this.columnTypes.get(index);
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "values", this.values);
		}
		
		@Override
		public int hashCode() {
			if (this.hashCode == null) {
				HashCodeBuilder h = new HashCodeBuilder();
				for (int i = 0; i < this.getColumnCount(); i++) {
					h.append(this.getColumnName(i));
					h.append(this.getColumnType(i));
					h.append(this.getValue(this.getColumnName(i)));
				}
				
				this.hashCode = h.toHashCode();
			}
			
			return this.hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || !(obj instanceof DbRowImpl)) return false;
			
			DbRowImpl row = (DbRowImpl)obj;
			if (this.getColumnCount() == row.getColumnCount() 
					&& this.hashCode() == row.hashCode()
					&& JuCollectionUtils.mapEquals(this.values, row.values)) {
				return JuCollectionUtils.collectionEquals(this.columnTypes, row.columnTypes)
						&& JuCollectionUtils.collectionEquals(this.columnNames, row.columnNames)
						&& JuCollectionUtils.mapEquals(this.values, row.values);
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Implementation of the DbRows interface. Note that DbRow instances are
	 * to be immutable.
	 * @author Martin
	 *
	 */
	static class DbRowsImpl implements DbRows {
		/**
		 * The row that defines the columns of the DbRows implementation. This can either be the first row
		 * of the result or a DbRow with empty values if the result is empty.
		 */
		private DbRow baseRow;
		
		/**
		 * Rows of the DbRows instance.
		 */
		private ArrayList<DbRow> rows = new ArrayList<DbRow>();
		
		/**
		 * Cache of the DbRows hashCode.
		 */
		private Integer hashCode = null;
		
		/**
		 * Adds a row to this DbRows instance. If it's the first row, it will also be used
		 * as the base row to get column name and type information.
		 * @param row DbRow instance
		 */
		void addRow(DbRow row) {
			this.rows.add(row);
			
			if (this.baseRow == null) this.baseRow = row;
			
			this.hashCode = null;
		}
		
		/**
		 * Explicitly sets the base row. This is only necessary if the DbRows instance contains no rows.
		 * In this case, a DbRow instance with null values, but with column name and type infos has
		 * to be set as the base row.
		 * @param row DbRow instance, values can be null
		 */
		void setBaseRow(DbRow row) {
			this.baseRow = row;
			this.hashCode = null;
		}
		
		/**
		 * Gets the base row of this DbRows.
		 * @return DbRow instance, values will be null if the DbRows contain no rows
		 */
		DbRow getBaseRow() {
			return this.baseRow;
		}
		
		@Override
		public int getColumnCount() {
			return this.baseRow.getColumnCount();
		}

		@Override
		public String getColumnName(int index) {
			return this.baseRow.getColumnName(index);
		}

		@Override
		public int getColumnType(int index) {
			return this.baseRow.getColumnType(index);
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "rowsCount", this.rows.size(), "baseRow", this.baseRow);
		}
		
		@Override
		public int hashCode() {
			if (this.hashCode == null) {
				HashCodeBuilder h = new HashCodeBuilder();
				for (int i = 0; i < this.getColumnCount(); i++) {
					h.append(this.getColumnName(i));
					h.append(this.getColumnType(i));				
				}
				for (DbRow row : this) {
					h.append(row);
				}
				
				this.hashCode = h.toHashCode();
			}
			
			return this.hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || !(obj instanceof DbRowsImpl)) return false;
			
			DbRowsImpl rows = (DbRowsImpl)obj;
			return ObjectUtils.equals(this.baseRow, rows.baseRow) 
					&& this.hashCode() == rows.hashCode()
					&& JuCollectionUtils.collectionEquals(this.rows, rows.rows);		
		}

		@Override
		public int getRowCount() {
			return this.rows.size();
		}

		@Override
		public DbRow getRow(int i) {
			return this.rows.get(i);
		}

		@Override
		public Iterator<DbRow> iterator() {
			return this.rows.iterator();
		}
	}
}
