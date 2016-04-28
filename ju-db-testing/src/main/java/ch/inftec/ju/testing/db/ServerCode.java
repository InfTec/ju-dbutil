package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import ch.inftec.ju.db.JuEmUtil;

/**
 * Base class for server code execution, i.e. a container for code that will run
 * in the server VM in the container. The Server code will be initialized by the container
 * with an EntityManager to allow DB access.
 * <p>
 * Extending classes must provide a public default constructor.
 * @author Martin
 *
 */
public abstract class ServerCode {
	protected EntityManager em;
	protected JuEmUtil emUtil;
	
	/**
	 * Initializes the DataVerifier. Needs to be called from the testing
	 * framework before the initialize method is invoked.
	 * @param em EntityManager instance of the current persistence context
	 */
	public final void init(EntityManager em) {
		this.em = em;
		this.emUtil = new JuEmUtil(em);
	}
	
	/**
	 * Method that will be called by the testing framework when the
	 * server code should be executed.
	 * @throws Exception If execution fails. This will make the current test case fail.
	 */
	public abstract void execute() throws Exception;
}