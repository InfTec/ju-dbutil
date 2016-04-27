package ch.inftec.ju.util.persistable;


/**
 * Tests for the DbPersistenceStorage implementation of a MementoStorage.
 * @author Martin
 *
 */
public class DbPersistenceStorageTest {
	// TODO: Refactor or remove
//	private static DbConnectionFactory dcf = DbConnectionFactoryLoader.createInstance();
//	
//	@Test
//	public void mementoStorage() {
//		try (DbConnection dbConn = dcf.openDbConnection("PU_mementoObject")) {
//			TestObject o1 = new TestObject("o1", 1L);
//			o1.setId(1L); // Makes assertion comparison easier
//			
//			EntityManager em = dbConn.getEntityManager();
//			em.createQuery("delete from TestObject").executeUpdate(); // Clear from previous/unrelated runs
//			em.persist(o1);
//			em.flush();
//			
//			DbAction updateAction1 = dbConn.getQueryRunner().getUpdateAction("TestObject", o1.getId());
//			DbAction updateAction2 = dbConn.getQueryRunner().getUpdateAction("TestObject", o1.getId());
//			DbAction updateAction3 = dbConn.getQueryRunner().getUpdateAction("TestObject", o1.getId());
//			
//			updateAction1.setValue("text", "o1_new");
//			updateAction2.setValue("value", -1L);
//			updateAction3.setValue("value", 2);
//			
//			PersistableChangeItem changeItem = DbChangeUtils.buildChangeSet(dbConn)
//				.name("Memento Test")
//				.description("Memento Test")
//				.newGroup("G1", "Group1")
//					.addAction(updateAction1)
//					.addAction(updateAction2)
//					.endGroup()
//				.newGroup("G2", "Group2")
//					.addAction(updateAction3)
//					.endGroup()
//				.build();
//			
//			GenericMemento memento = changeItem.createMemento();
//			
//			// Store to StringStorage to check contents
//			TestUtils.assertEqualsResource("DbPersistenceStorageTest_mementoStorage.txt", GenericMementoUtils.persistToString(memento, "DbChangeSet"));
//			
//			// Store to DbPersistenceStorage
//			MementoStorage dbStorage = new DbPersistenceStorage(dbConn);
//			
//			Long id = dbStorage.persistMemento(memento, "ChangeSet");
//			
//			// Load from DbPersistenceStorage
//			GenericMementoItem loadedMementoItem = dbStorage.loadMemento(id);
//			
//			Assert.assertEquals(id, loadedMementoItem.getId());
//			
//			TestUtils.assertEqualsResource("DbPersistenceStorageTest_mementoStorage.txt", 
//					GenericMementoUtils.persistToString(loadedMementoItem.getMemento(), "ChangeSet"));
//		}
//	}
}
