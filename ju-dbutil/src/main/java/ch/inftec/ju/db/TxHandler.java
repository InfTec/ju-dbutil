package ch.inftec.ju.db;

import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 * Helper wrapper around an EntityManager (local transaction) or a UserTransaction (JTA) 
 * to help coping with optional transaction object (null) and exceptions.
 * TODO: Refactor (EntityManager vs. UserTransaction code) or drop resource local transaction support and require JTA
 * @author Martin
 *
 */
public final class TxHandler implements AutoCloseable {
	private final EntityManager em;
	private final UserTransaction tx;
	private boolean committed = true;
	
	/**
	 * Creates a TxHandler wrapper, starting a new transaction (if no active transaction is present).
	 * @param tx Underlying user transaction
	 */
	public TxHandler(UserTransaction tx) {
		this(tx, true);
	}
	
	/**
	 * Creates a TxHandler around the EntityTransaction of the EntityManager and
	 * automatically begins the transaction.
	 * @param em EntityManager with local resource transaction handling
	 */
	public TxHandler(EntityManager em) {
		this(em, null, true);
	}
	
	/**
	 * Creates a new TxHandler wrapper and begins a new transaction
	 * if begin is true.
	 * @param tx UserTransaction
	 * @param begin If true, calls begin on the transaction
	 */
	public TxHandler(UserTransaction tx, boolean begin) {
		this(null, tx, begin);
	}

	private TxHandler(EntityManager em, UserTransaction tx, boolean begin) {
		this.em = em;
		this.tx = tx;
		
		if (begin) this.begin();
	}
	
	/**
	 * Begins a new transaction (unless we already have an active tranaction).
	 * @throws JuDbException If we cannot begin a transaction
	 */
	public void begin() {
		if (this.em != null) {
			if (!this.em.getTransaction().isActive()) {
				this.em.getTransaction().begin();
			}
		} else {
			try {
				if (this.tx != null && tx.getStatus() != Status.STATUS_ACTIVE) tx.begin();
				this.committed = false;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't begin JTA transaction", ex);
			}
		}
	}
	
	/**
	 * Commits the transaction without beginning a new one.
	 */
	public void commit() {
		this.commit(false);
	}
	
	/**
	 * Commits the transaction
	 * @param beginNew If true, begins a new transaction
	 */
	public void commit(boolean beginNew) {
		if (this.em != null) {
			if (!this.committed) {
				this.em.getTransaction().commit();
				this.committed = true;
			}
			
			if (beginNew) this.em.getTransaction().begin();
		} else {
			try {
				if (!this.committed) {
					if (this.tx != null) tx.commit();
					this.committed = true;
				}
			} catch (Exception ex) {
				throw new JuDbException("Couldn't commit JTA transaction", ex);
			}
			
			if (beginNew) this.begin();
		}
	}
	
	/**
	 * Rolls back the transaction if it hasn't been
	 * committed yet.
	 */
	public void rollbackIfNotCommitted() {
		if (!this.committed) {
			if (this.em != null) {
				this.em.getTransaction().rollback();
			} else if (this.tx != null) {
				try {
					this.tx.rollback();
				} catch (Exception ex) {
					throw new JuDbException("Couldn't rollback transaction", ex);
				}
			}
			this.committed = true;
		}
	}
	
	@Override
	public void close() {
		this.rollbackIfNotCommitted();
	}
}
