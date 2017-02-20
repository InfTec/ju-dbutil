package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The list of acceptable selenium drivers. If the currently set driver (ju-testing-ee.selenium.driver) does not match any of the given
 * entries, then the first entry in the list will be used as default selenium driver.
 * 
 * @author dalay.mabboux@inftec.ch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface SeleniumDriverPolicy {

	public SeleniumDriver[] value();

	public enum SeleniumDriver {
		Chrome, PhantomJS, HtmlUnit
	}
}
