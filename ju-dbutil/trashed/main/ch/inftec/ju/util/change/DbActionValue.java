package ch.inftec.ju.util.change;


/**
 * Helper interface for the value of a DbAction, containing the column name, original and changed value.
 * @author Martin
 *
 */
public interface DbActionValue {
	/**
	 * Gets the column name of the value.
	 * @return Column name
	 */
	public String getColumnName();
	
	/**
	 * Gets the original value, i.e. the value before the action is executed.
	 * @return Original value
	 */
	public Object getOriginalValue();
	
	/**
	 * Gets the changed value, i.e. the value set by setValue. If no value was set,
	 * null is returned.
	 * @return Changed value
	 */
	public Object getChangedValue();
	
	/**
	 * Gets the actual value, i.e. the changed value if we have any or the original value
	 * if no value was set explicitly.
	 * @return Changed value if any or original value if not
	 */
	public Object getValue();
	
	/**
	 * Gets whether the value has changed, i.e. whether the original and the changed value
	 * are different.
	 * @return True if the value has changed, false otherwise
	 */
	public boolean hasChanged();
}
