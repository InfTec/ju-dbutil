package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to execute code on the server after the main test
 * has been run (but before the optional DataSet export and
 * data verification have taken place). 
 * <p>
 * The execution will run in a separate transaction.
 * <p>
 * When methods are overridden, we will execute all server code that are defined
 * on all methods (current and overridden).
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PostServerCode {
	/**
	 * Helper class to set as default value (we cannot set null nor void.class
	 * as default.
	 * @author Martin
	 *
	 */
	public static final class DEFAULT_SERVER_CODE extends ServerCode {
		@Override
		public void execute() {
			// Do nothing
		}
	}
	
	/**
	 * Sub type of ServerCode that will be used to perform the code execution.
	 * <p>
	 * Default value is DEFAULT_SERVER_CODE. In this case, we will look for
	 * a static inner class of the test class that has the same name as the test method
	 * annotated with ServerCode, but starts with a capital letter and has the suffix
	 * _code.
	 * @return Sub type of ServerCode
	 */
	Class<? extends ServerCode> value() default DEFAULT_SERVER_CODE.class;
}