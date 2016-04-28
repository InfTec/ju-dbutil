package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure JU db tests.
 * <p>
 * If not present, default values will be used or values will be loaded from
 * property files.
 * <p>
 * If present, these values override any other values that might be specified.
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface JuDbTest {
	/**
	 * Name of the profile.
	 * @return Profile name
	 */
	String profile();
	
	/**
	 * Name of the persistence unit to use.
	 * @return Persistence Unit name
	 */
	String persistenceUnit();
}