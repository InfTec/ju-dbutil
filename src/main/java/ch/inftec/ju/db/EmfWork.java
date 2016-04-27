package ch.inftec.ju.db;

import javax.persistence.EntityManager;

/**
 * Helper class that is used by JuEmfUtil to perform DB work in a transaction.
 * @author Martin
 *
 */
public interface EmfWork extends AutoCloseable {
	/**
	 * Gets an EntityManager instance that can be used to interact with the DB.
	 * @return EntityManager instance
	 */
	EntityManager getEm();
	
	/**
	 * Gets a JuEmUtil wrapper around this works EntityManager
	 * @return JuEmUtil instance
	 */
	JuEmUtil getEmUtil();
	
	/**
	 * Marks the transaction for rollback. Rollback will be executed when close is executed.
	 */
	void setRollbackOnly();
	
	@Override
	public void close();
}
