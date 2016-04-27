package ch.inftec.ju.db;


/**
 * Interface that represents a list of DbRow instances, e.g. as a result from a query.
 * <p>
 * A DbRows instance must implement the Iterable interface to be able to iterate over it's rows,
 * in addition to direct row access.
 * <p>
 * A DbRows instance is to be immutable.
 * @author Martin
 *
 */
public interface DbRows extends Iterable<DbRow>{
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
	 * Gets the number of rows.
	 * @return Number of rows
	 */
	public int getRowCount();
	
	/**
	 * Gets the specified row.
	 * @param i Row index, starting from 0
	 * @return DbRow instance for the specified row index
	 */
	public DbRow getRow(int i);
	
	/**
	 * Two DbRows instances are considered equal if they have the same columns (in the same order
	 * with the same types) and all their rows are equal, too.
	 * @param obj Object to compare to
	 * @return If the supplied object is a DbRows instance and equals this DbRows instance
	 */
	@Override
	public boolean equals(Object obj);
}
