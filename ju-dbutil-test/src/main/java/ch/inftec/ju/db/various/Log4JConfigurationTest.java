package ch.inftec.ju.db.various;

import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.JuUrl;

public class Log4JConfigurationTest {
	private Logger logger = LoggerFactory.getLogger(Log4JConfigurationTest.class);

	/**
	 * Helper to display Log4J config files on the classpath.
	 * <p>
	 * Used to determine problems with logging on build servers and the like.
	 */
	@Test
	public void show_log4JConfigFields_onClasspath() {
		List<URL> urls = JuUrl.resource().getAll("log4j.xml");

		logger.info("Found {} log4j.xml files on classpath:", urls.size());

		for (URL url : urls) {
			logger.info("  {}", url);
		}
	}
}
