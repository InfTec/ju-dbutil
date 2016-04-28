package ch.inftec.ju.testing.db.data;

import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.testing.db.data.TestDbUtils.AbstractTestDb;

/**
 * Default implementation of a Derby Test DB.
 * <p>
 * Defers from the DerbyTestDb in the way it doesn't create any tables on
 * its own.
 * @author Martin
 *
 */
public class DefaultDerbyTestDb extends AbstractTestDb {
	protected void resetPlatformSpecificData() throws JuDbException {
		// XXX: Adapt for hibernate
		// Reset sequence to guarantee predictable primary key values
		//this.jdbcTemplate.update("UPDATE SEQUENCE SET SEQ_COUNT=? WHERE SEQ_NAME=?", 9, "SEQ_GEN");
	}
}
