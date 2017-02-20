package ch.inftec.ju.db;

import java.sql.Connection;

/**
 * Interface that can be used to execute work requiring a Connection instance.
 * @author Martin
 *
 */
public interface DbWork {
	/**
	 * Callback function providing a Connection.
	 * @param conn Connection instance
	 */
	void execute(Connection conn);
}
