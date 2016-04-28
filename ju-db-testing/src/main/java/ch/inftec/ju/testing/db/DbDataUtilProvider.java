package ch.inftec.ju.testing.db;

/**
 * Helper interface to provide a DbDataUtil instance to be used to perform
 * DbUnit data import and export for container tests.
 * <p>
 * If not specified, the DB tests will create a new DbDataUtil instance for every cycle
 * which may result in repetitive queries to the DB
 * @author Martin Meyer <martin.meyer@inftec.ch>
 *
 */
public interface DbDataUtilProvider {
	/**
	 * Gets a DbDataUtil instance to be used to perform data imports and exports.
	 * @return DbDataUtil instance
	 */
	public DbDataUtil getDbDataUtil();
}
