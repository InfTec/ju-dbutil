package ch.inftec.ju.db;

/**
 * Represents a result row from a database. This can either be a row of a table
 * or the result row of a view/query. The values of the row are immutable.
 * <p>
 * DbRows have to implement the equals method to make sure two rows with the same columns
 * (including order and type) and values are considered equal.
 * <p>
 * Column names are always converted to upper case, but getValue must be case insensitive.
 * @author Martin
 *
 */
public interface DbRow {
	/**
	 * Gets the value of the specified column.
	 * @param columnName Column name
	 * @return Value or null if the columName is null or the specified column doesn't exist.
	 * The value of the column may be null by itself as well.
	 */
	public Object getValue(String columnName);
	
	/**
	 * Gets the number of columns in the row.
	 * @return Number of columns
	 */
	public int getColumnCount();
	
	/**
	 * Gets the name of the specified column. Column names are always
	 * converted to all upper case.
	 * @param index Column index, starting with 0
	 * @return Column name
	 */
	public String getColumnName(int index);
	
	/**
	 * Gets the type of the specified column as returned by the ResultSetMetaData object.
	 * @param index Column index, starting with 0
	 * @return Column type
	 */
	public int getColumnType(int index);
	
	/**
	 * Two DbRows are considered equal if they have the same columns (in the same order
	 * with the same types) and the same values.
	 * @param obj Object to compare to
	 * @return If the supplied object is a DbRow and equals this row
	 */
	@Override
	public boolean equals(Object obj);
}
