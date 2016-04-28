package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helper annotation to define environmental variables for tests.
 * @author martin.meyer@inftec.ch
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface JuTestEnv {
	/**
	 * Sets system properties for the test run context.
	 * <p>
	 * System properties must be defined using Strings of the form <code>key=value</code>. They will
	 * override any system properties for the test context and then be reset to their original value at
	 * the end of the test.
	 * @return Array of system property settings
	 */
	public String[] systemProperties();
}
