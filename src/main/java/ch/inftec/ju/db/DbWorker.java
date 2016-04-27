package ch.inftec.ju.db;

/**
 * Interface for classes that are capable of executing DbWork.
 * @author Martin
 *
 */
public interface DbWorker {
	/**
	 * Performs DB Work that requires a JDBC Connection.
	 * @param work DbWork
	 */
	void doWork(DbWork work);
}
