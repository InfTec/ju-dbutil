package ch.inftec.ju.testing.db;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;

public class DbUnitTest extends DefaultContextAbstractBaseDbTest {
	@Autowired
	private TestingEntityRepo testingEntityRepo;
		
	@Test
	public void databaseSetup() {
		this.createDbDataUtil().cleanImport("DbUnitTest-singleTestingEntityData.xml");
		Assert.assertEquals("SpringDbUnitTest", this.testingEntityRepo.findOne(1L).getName());
	}
}
