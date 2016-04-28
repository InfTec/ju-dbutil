package ch.inftec.ju.testing.db.data;

import ch.inftec.ju.db.JuDbException;

/**
 * Instance of a Test database.
 * @author tgdmemae
 *
 */
public interface TestDb {
	/**
	 * Initializes the DB.
	 * <p>
	 * This method will be called once before accessing the DB to initialize it, e.g.
	 * to create tables.
	 * @throws JuDbException If the Database cannot be initialized
	 */
	public void initDb() throws JuDbException;
	
	/**
	 * Resets the Database so subsequent calls will yield the same results, e.g.
	 * sequences should be reset so new objects always get the same IDs.
	 * <p>
	 * This method will be called multiple times to reset the DB to a known state.
	 * @throws JuDbException If the Database cannot be reset
	 */
	public void resetDatabase() throws JuDbException;
}