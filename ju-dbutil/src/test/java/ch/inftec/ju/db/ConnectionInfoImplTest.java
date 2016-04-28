package ch.inftec.ju.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.inftec.ju.util.JuUtils;
import ch.inftec.ju.util.SystemPropertyTempSetter;

public class ConnectionInfoImplTest {
	@Before
	public void clearPropertyChain_beforeTests() {
		JuUtils.clearPropertyChain();
	}
	
	@Test
	public void supports_unencryptedPassword() {
		try (SystemPropertyTempSetter ts = new SystemPropertyTempSetter()) {
			ts.setProperty("ju-util.propertyChain.encryption.password", "secretPassword");
			
			ConnectionInfoImpl ci = new ConnectionInfoImpl();
			ci.setPassword("cCULeUjKiLfBwEgCOKC1g3BasxVDF85c");
			
			Assert.assertEquals("cCULeUjKiLfBwEgCOKC1g3BasxVDF85c", ci.getPassword());
		}
	}
	
	@Test
	public void supports_encryptedPassword() {
		try (SystemPropertyTempSetter ts = new SystemPropertyTempSetter()) {
			ts.setProperty("ju-util.propertyChain.encryption.password", "secretPassword");
			
			ConnectionInfoImpl ci = new ConnectionInfoImpl();
			ci.setPassword("ENC(cCULeUjKiLfBwEgCOKC1g3BasxVDF85c)");
			
			Assert.assertEquals("secretVal", ci.getPassword());
		}
	}
}
