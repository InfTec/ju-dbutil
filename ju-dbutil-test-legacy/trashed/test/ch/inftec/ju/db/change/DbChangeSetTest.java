package ch.inftec.ju.db.change;

import org.junit.Test;

import ch.inftec.ju.testing.db.DefaultContextAbstractBaseDbTest;

/**
 * Test class to test DbChangeSets.
 * @author tgdmemae
 *
 */
public class DbChangeSetTest extends DefaultContextAbstractBaseDbTest {
	// TODO: Refactor or remove
	@Test
	public void refactor() {
	}
//	@Test
//	public void emptySet() {
//		// Empty set
//		ChangeItem dbChangeSet = DbChangeUtils.buildChangeSet(this.dbConn)
//			.name("TestSet")
//			.description("TestSet description")
//			.build();
//
//		Assert.assertEquals(0, dbChangeSet.getChildItems().size());
//		
//		Assert.assertEquals("TestSet", dbChangeSet.getDescriptor().getName());
//		Assert.assertEquals("TestSet description", dbChangeSet.getDescriptor().getDescription());
//		
//		// Test executing
//		dbChangeSet.getHandler().execute();
//		
//		// Create undo set
//		ChangeItem undoSet = dbChangeSet.getHandler().createUndoItem();
//		Assert.assertEquals(0, undoSet.getChildItems().size());
//		Assert.assertEquals("TestSet.undo", undoSet.getDescriptor().getName());
//		Assert.assertEquals("TestSet description.undo", undoSet.getDescriptor().getDescription());
//	}
//	
//	@Test
//	public void simpleSet() {
//		DbQueryRunner qr = this.dbConn.getQueryRunner();
//		
//		DbAction u1 = qr.getUpdateAction("TEST_A", 1);
//		u1.setValue("TEXT", "NewA1");
//		
//		DbAction u2 = qr.getUpdateAction("TEST_A", 2);
//		u2.setValue("TEXT", "NewA2");
//		u2.setValue("B_FK", 1);
//		
//		ChangeItem dbChangeSet = DbChangeUtils.buildChangeSet(this.dbConn)
//			.name("SimpleSet")
//			.description("SimpleSet description")
//				.newGroup("UpdateA", "Update TEST_A tables")
//					.addAction(u1)
//					.addAction(u2)
//				.endGroup()
//			.build();
//		
//		dbChangeSet.getHandler().execute();
//
//		DbRow a1 = qr.primaryKeyQuery("TEST_A", 1);
//		this.assertRowEquals(a1, "AID", 1, "TEXT", "NewA1", "B_FK", 1);
//		DbRow a2 = qr.primaryKeyQuery("TEST_A", 2);
//		this.assertRowEquals(a2, "AID", 2, "TEXT", "NewA2", "B_FK", 1);
//	}
//	
//	/**
//	 * Tests the creation of the memento.
//	 */
//	@Test
//	public void createMemento() {
//		DbAction updateAction1 = dbConn.getQueryRunner().getUpdateAction("TEST_A", 1);
//		
//		updateAction1.setValue("TEXT", "A1New");
//		
//		PersistableChangeItem changeItem = DbChangeUtils.buildChangeSet(dbConn)
//			.name("Memento Test")
//			.description("Memento Test")
//			.newGroup("G1", "Updates 1")
//				.addAction(updateAction1)
//				.newUpdateAction("TEST_A", 2)
//					.setValue("text", "A2New")
//					.endAction()
//				.endGroup()
//			.newGroup("G2", "Updates 2")
//				.newUpdateAction("TEST_A", 3)
//					.setValue("b_fk", 1)
//					.endAction()
//				.endGroup()
//			.newGroup("G3", "Inserts")
//				.newInsertAction("test_a")
//					.setValue("aid", 100)
//					.setValue("text", "A100")
//					.endAction()
//				.endGroup()				
//			.newGroup("G4", "Deletes")
//				.newDeleteAction("TEST_A", 1)
//				.endGroup()
//			.build();
//		
//		GenericMemento memento = changeItem.createMemento();
//		
//		MementoStorage stringStorage = GenericMementoUtils.newStringMementoStorage();
//		stringStorage.persistMemento(memento, "DbChangeSet");
//				
//		TestUtils.assertEqualsResource("DbChangeSetTest.createMemento.txt", stringStorage.toString());
//	}
}
