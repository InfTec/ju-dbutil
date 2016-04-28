package ch.inftec.ju.dbutil.test;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DataSet;
import ch.inftec.ju.testing.db.DataSetExport;
import ch.inftec.ju.testing.db.DataSetExport.ExportType;
import ch.inftec.ju.testing.db.DataVerifier;
import ch.inftec.ju.testing.db.DataVerify;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.testing.db.JuAssumeUtils;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;

public class AbstractDbTestTransactionTest extends AbstractDbTest {
	@Override
	protected void runDbInitializationScripts(JuEmfUtil emfUtil) {
		try (EmfWork ew = emfUtil.startWork()) {
			new DbSchemaUtil(ew.getEm()).prepareDefaultSchemaAndTestData();;
		}
	}
	
	@Test
	@DataSet("AbstractDbTestTransactionTest_testingEntity.xml")
	@DataVerify
	@DataSetExport(tablesDataSet="AbstractDbTestTransactionTest_testingEntity.xml", exportType=ExportType.MEMORY)
	public void transaction_isCommitted() {
		this.em.find(TestingEntity.class, -1L).setName("DoCommit");
	}
	public static class Transaction_isCommitted extends DataVerifier {
		@Override
		public void verify() throws Exception {
			Assert.assertEquals("DoCommit", this.getXg().getSingle("//TestingEntity/@name"));
		}
	}
	
	@Test
	@DataSet("AbstractDbTestTransactionTest_testingEntity.xml")
	@DataVerify
	@DataSetExport(tablesDataSet="AbstractDbTestTransactionTest_testingEntity.xml", exportType=ExportType.MEMORY)
	public void transaction_isRolledBack() {
		this.em.find(TestingEntity.class, -1L).setName("DoCommit");
		this.em.flush();
		
		this.setRollbackOnly();
	}
	public static class Transaction_isRolledBack extends DataVerifier {
		@Override
		public void verify() throws Exception {
			Assert.assertEquals("TransactionTest", this.getXg().getSingle("//TestingEntity/@name"));
		}
	}
	
	@Test
	@DataSet("AbstractDbTestTransactionTest_testingEntity.xml")
	@DataVerify
	@DataSetExport(tablesDataSet="AbstractDbTestTransactionTest_testingEntity.xml", exportType=ExportType.MEMORY)
	public void multipleTransactions_areSupported() {
		// H2 and HSQL have a locking strategy that will cause a timeout in this scenario
		JuAssumeUtils.dbIsNot(this.emUtil, DbType.H2, DbType.HSQL);
		
		TestingEntity te1 = new TestingEntity("TE1");
		this.em.persist(te1);
		
		try (EmfWork ew2 = this.startNewWork()) {
			TestingEntity te2 = new TestingEntity("TE2");
			ew2.getEm().persist(te2);
			
			try (EmfWork ew3 = this.startNewWork()) {
				TestingEntity te3 = new TestingEntity("TE3");
				ew3.getEm().persist(te3);			
			}
			
			ew2.setRollbackOnly();
		}
	}
	public static class MultipleTransactions_areSupported extends DataVerifier {
		@Override
		public void verify() throws Exception {
			Assert.assertEquals(3, this.getXg().getCount("//TestingEntity"));
			Assert.assertEquals("TE1", this.getXg().getSingle("//TestingEntity[@id='1']/@name"));
			Assert.assertEquals("TE3", this.getXg().getSingle("//TestingEntity[@id='3']/@name"));
		}
	}
}
