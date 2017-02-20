package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to verify data after a test has completed.
 * <p>
 * The verifying will run in a separate transaction, so the verifier
 * can be used to test transactional behavior as well.
 * <p>
 * When methods are overridden, we will call all verifiers that are defined
 * on all methods (current and overridden).
 * <p>
 * If no value is specified, the default data verifier will be used that must be named
 * equal to the test method with a capital letter and extend DataVerifier. All non-alphabetical
 * characters will be stripped from the beginning of the method name to evalute the default verifier
 * class name. Examples:
 * <ul>
 *   <li>myTestMethod() -&gt; MyTestMethod</li>
 *   <li>_01_myTestMethod2() -&gt; MyTestMethod2</li>
 * </ul>
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataVerify {
	/**
	 * Helper class to set as default value (we cannot set null nor void.class
	 * as default.
	 * @author Martin
	 *
	 */
	public static final class DEFAULT_DATA_VERIFIER extends DataVerifier {
		@Override
		public void verify() {
			// Do nothing
		}
	}
	
	/**
	 * Sub type of DataVerifier that will be used to perform the data
	 * verifying.
	 * <p>
	 * Default value is DEFAULT_DATA_VERIFIER. In this case, we will look for
	 * a static inner class of the test class that has the same name as the test method
	 * annotated with DataVerify, but starts with a capital letter.
	 * <p>
	 * Note: Any non-alphabetically leading characters will be stripped, e.g. _01_testMethod -&gt; TestMethod 
	 * @return Sub type of DataVerifier
	 */
	Class<? extends DataVerifier> value() default DEFAULT_DATA_VERIFIER.class;
}