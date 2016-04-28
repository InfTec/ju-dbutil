package ch.inftec.ju.db;

import javax.sql.DataSource;

/**
 * Interface that can be used to execute work requiring an DataSource instance.
 * @author Martin
 *
 */
public interface DsWork {
	/**
	 * Callback function providing a DataSource.
	 * @param ds DataSource instance
	 */
	void execute(DataSource ds);
}
