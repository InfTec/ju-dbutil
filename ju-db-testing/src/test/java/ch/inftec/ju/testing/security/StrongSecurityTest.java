package ch.inftec.ju.testing.security;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.security.JuSecurityUtils;
import ch.inftec.ju.security.JuTextEncryptor;
import ch.inftec.ju.testing.db.JuAssumeUtils;

/**
 * Test for ju-util JuSecurityUtils as we need the JuAssumeUtils here...
 * @author martin.meyer@inftec.ch
 *
 */
public class StrongSecurityTest {
	
	@Test
	public void canEncryptText_usingStrongEncryptor() {
		JuAssumeUtils.javaCryptographyExtensionInstalled();
		
		JuTextEncryptor encryptor = JuSecurityUtils.buildEncryptor()
				.strong()
				.password("secret")
				.createTextEncryptor();
		
		String encryptedString = encryptor.encrypt("String"); // The encrypted String will not be constant...
		Assert.assertNotNull(encryptor.encrypt("String"));
		Assert.assertEquals("String", encryptor.decrypt(encryptedString));
	}
}