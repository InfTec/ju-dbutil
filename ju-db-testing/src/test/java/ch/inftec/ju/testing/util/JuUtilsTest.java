package ch.inftec.ju.testing.util;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.util.JuUtils;

/**
 * Additional test to ch.inftec.ju.util.JuUtilsTest to test with additional property files.
 * @author Martin
 *
 */
public class JuUtilsTest {
	@Test
	public void multiplePropertyFiles_areFound() {
		Assert.assertEquals("testing", JuUtils.getJuPropertyChain().get("ju-testing.property"));
	}
}
