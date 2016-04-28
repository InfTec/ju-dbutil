package ch.inftec.ju.testing.db;

import org.junit.Test;

public class DefaultDataTest extends AbstractDbTest {
	@Test
	@JuDbTest(profile="derby-testing", persistenceUnit="ju-testing pu-default-test-data")
	public void canLoadDefaultData_inAbstractDbTest() {
		new DbSchemaUtil(this.connUtil).prepareDefaultSchemaAndTestData();
	}
}