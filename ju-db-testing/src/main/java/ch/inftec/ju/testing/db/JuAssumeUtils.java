package ch.inftec.ju.testing.db;

import org.junit.Assume;

import ch.inftec.ju.db.JuConnUtil;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.util.JuUtils;

/**
 * Util class providing JUnit assume functionality.
 * @author Martin
 *
 */
public class JuAssumeUtils {
	/**
	 * Assumes that the DB represented by JuEmUtil is none of the
	 * DbTypes specified.
	 * @param emUtil JuEmUtil
	 * @param types Types we assume the DB is not
	 * @deprecated Use dbIsNot(JuConnUtil, DbType...) instead
	 */
	@Deprecated
	public static void dbIsNot(JuEmUtil emUtil, DbType... types) {
		DbType actualType = emUtil.getDbType();
		
		for (DbType type : types) {
			Assume.assumeFalse("Assumed DB was not " + type, type == actualType);
		}
	}
	
	/**
	 * Assumes that the DB represented by JuConnUtil is none of the
	 * DbTypes specified.
	 * @param connUtil JuConnUtil
	 * @param types Types we assume the DB is not
	 */
	public static void dbIsNot(JuConnUtil connUtil, DbType... types) {
		DbType actualType = connUtil.getDbType();
		
		for (DbType type : types) {
			Assume.assumeFalse("Assumed DB was not " + type, type == actualType);
		}
	}
	
	/**
	 * Assumes the Chrome browser is available.
	 */
	public static void chromeIsAvailable() {
		boolean chromeIsAvailable = JuUtils.getJuPropertyChain().get("ju-testing-ee.selenium.chrome.isAvailable", Boolean.class, true);
		Assume.assumeTrue("Chrome is not available", chromeIsAvailable);
	}
	
	/**
	 * Assumes Internet access is available.
	 */
	public static void internetIsAvailable() {
		boolean chromeIsAvailable = JuUtils.getJuPropertyChain().get("ju-testing-ee.internet.isAvailable", Boolean.class, true);
		Assume.assumeTrue("Internet is not available", chromeIsAvailable);
	}
	
	/**
	 * Assumes that the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files are installed.
	 * <p>
	 * These are necessary to use strong encryption algorithms.
	 */
	public static void javaCryptographyExtensionInstalled() {
		boolean javaCryptographyExtensionInstalled = JuUtils.getJuPropertyChain().get(
				"ju-testing.javaCryptographyExtension.isInstalled", Boolean.class, true);
		Assume.assumeTrue(javaCryptographyExtensionInstalled);
	}
}
