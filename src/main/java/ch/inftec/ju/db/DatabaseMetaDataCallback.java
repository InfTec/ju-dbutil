package ch.inftec.ju.db;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Callback interface to retrieve DatabaseMetaData. Used by JuEmUtil.
 * @author Martin
 *
 * @param <T> Return type of the meta data extracted by the callback method
 */
public interface DatabaseMetaDataCallback<T> {
	/**
	 * Processes the DatabaseMetaData and returns the extracted data.
	 * @param dbmd DatabaseMetaData
	 * @return Data
	 * @throws SQLException that will be wrapped into a Runtime exception by JuEmUtil
	 */
	T processMetaData(DatabaseMetaData dbmd) throws SQLException;
}
