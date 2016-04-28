package ch.inftec.ju.testing.db;


import org.junit.Assert;
import org.junit.Test;

/**
 * Test to test the JuDbTest annotation.
 * @author Martin
 *
 */
@JuDbTest(profile="derby-testing-ann1", persistenceUnit="ju-testing-pu-ann")
public class JuDbTestTest extends AbstractDbTest {
	@Test
	public void usesClassAnnotation_ifNoneSpecified() {
		Assert.assertEquals("jdbc:derby:memory:ju-testing-pu-ann1", this.emUtil.getMetaDataUrl());
	}
	
	@Test
	@JuDbTest(profile="derby-testing-ann2", persistenceUnit="ju-testing-pu-ann")
	public void methodAnnotation_overrides_classAnnotation() {
		Assert.assertEquals("jdbc:derby:memory:ju-testing-pu-ann2", this.emUtil.getMetaDataUrl());
	}
}
