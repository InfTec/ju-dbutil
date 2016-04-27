package ch.inftec.ju.db;

import javax.persistence.EntityManager;

/**
 * Interface that can be used to execute work requiring an EntityManager instance.
 * @author Martin
 *
 */
public interface EmWork {
	/**
	 * Callback function providing an EntityManager. Implementations make
	 * sure that a transaction is active.
	 * <p>
	 * If an exception is thrown during execution, the transaction will be rolled back.
	 * @param em EntityManager instance
	 */
	void execute(EntityManager em);
}
