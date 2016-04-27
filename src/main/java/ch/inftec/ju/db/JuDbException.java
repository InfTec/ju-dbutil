package ch.inftec.ju.db;

import ch.inftec.ju.util.JuRuntimeException;

/**
 * DB relation exception. The exception is a runtime exception so it doesn't
 * need to be declared all the time.
 * @author Martin
 *
 */
public class JuDbException extends JuRuntimeException {

	public JuDbException() {
	}

	public JuDbException(String message) {
		super(message);
	}

	public JuDbException(Throwable cause) {
		super(cause);
	}

	public JuDbException(String message, Throwable cause) {
		super(message, cause);
	}
}
