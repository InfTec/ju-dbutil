package ch.inftec.ju.db.change;

import org.junit.Test;

import ch.inftec.ju.testing.db.DefaultContextAbstractBaseDbTest;

public class DbActionTest extends DefaultContextAbstractBaseDbTest {
	// TODO: Refactor or remove
	@Test
	public void refactor() {
	}
//	@Test
//	public void updateAction() throws Exception {
//		DbQueryRunner qr = this.dbConn.getQueryRunner();
//		
//		DbAction updateAction = qr.getUpdateAction("TEST_A", 1);
//
//		// Check toString and getDescriptor
//		// toString: UpdateDbRowDbAction[tableName=TEST_A,primaryKeyValue=AID(1),dbConnection=DbConnectionImpl[name=Derby InMemory-DB]]
//		Assert.assertTrue(updateAction.toString().contains("[tableName=TEST_A,primaryKeyValue=AID(1),dbConnection="));
//		
//		DbActionValue val = updateAction.getVal("TEXT");
//		Assert.assertEquals(val.getOriginalValue(), "A1");
//		Assert.assertEquals(val.getChangedValue(), null);
//		Assert.assertEquals(val.getValue(), "A1");
//		Assert.assertFalse(val.hasChanged());
//		
//		updateAction.setValue("TEXT", "NewA1");
//		Assert.assertEquals(val.getChangedValue(), "NewA1");
//		Assert.assertTrue(val.hasChanged());
//		
//		updateAction.getHandler().execute();
//		
//		// Reload update action
//		DbAction updateActionNew = qr.getUpdateAction("TEST_A", 1);
//		DbActionValue valNew = updateActionNew.getVal("TEXT");
//		Assert.assertEquals(valNew.getOriginalValue(), "NewA1");
//		Assert.assertEquals(valNew.getChangedValue(), null);
//		Assert.assertEquals(valNew.getValue(), "NewA1");
//		Assert.assertFalse(valNew.hasChanged());
//		
//		// Make sure we get null if the row doesn't exist
//		DbAction updateActionNoRow = qr.getUpdateAction("TEST_A", 999);
//		Assert.assertNull(updateActionNoRow);
//		
//		// Undo update
//		
//		ChangeItem undoItem = updateAction.getHandler().createUndoItem();
//		undoItem.getHandler().execute();
//		
//		DbRow row = qr.primaryKeyQuery("TEST_A", 1);
//		Assert.assertEquals("A1", row.getValue("TEXT"));
//	}
//	
//	@Test
//	public void updateDescriptor() throws Exception {
//		DbQueryRunner qr = this.dbConn.getQueryRunner();
//		
//		DbAction updateAction = qr.getUpdateAction("TEST_A", 1);
//		Assert.assertEquals("Update(TEST_A.AID(1))", updateAction.getDescriptor().getName());
//		
////		updateAction.setValue("TEXT", "NewA1");
////		Assert.assertEquals("UPDATE TEST_A SET TEXT='NewA1' WHERE AID=1", updateAction.getDescriptor().getDescription());
//	}
//	
//	/**
//	 * Make sure the getter and setter values for actions are not
//	 * case sensitive.
//	 */
//	@Test
//	public void upperLowerCase() {
//		// Check upper/lower case
//		DbAction action = qr.getUpdateAction("TEST_A", 2);
//		Assert.assertEquals("A2", action.getVal("text").getValue());
//		Assert.assertEquals("A2", action.getVal("Text").getValue());
//		
//		action.setValue("text", "A2.a");
//		Assert.assertEquals("A2.a", action.getVal("TEXT").getValue());
//		
//		action.setValue("Text", "A2.b");
//		Assert.assertEquals("A2.b", action.getVal("TEXT").getValue());
//	}
//	
//	@Test
//	public void insertAction() throws Exception {
//		DbQueryRunner qr = this.dbConn.getQueryRunner();
//		
//		DbAction insertAction = qr.getInsertAction("TEST_A");
//		// toString: InsertDbRowDbAction[tableName=TEST_A,primaryKeyValue=AID(null),dbConnection=DbConnectionImpl[name=Derby InMemory-DB]]
//		Assert.assertTrue(insertAction.toString().contains("[tableName=TEST_A,primaryKeyValue=AID(null),dbConnection="));
//		
//		insertAction.setValue("AID", 99);
//		insertAction.setValue("TEXT", "A99");
//		insertAction.setValue("B_FK", 1);
//		
//		DbActionValue val = insertAction.getVal("TEXT");
//		Assert.assertNull(val.getOriginalValue());
//		Assert.assertEquals(val.getChangedValue(), "A99");
//		Assert.assertEquals(val.getValue(), val.getChangedValue());
//		Assert.assertTrue(val.hasChanged());
//		
//		insertAction.getHandler().execute();
//		
//		// Load inserted row
//		DbRow insertedRow = qr.primaryKeyQuery("TEST_A", 99);
//		Assert.assertEquals(insertedRow.getValue("AID"), 99);
//		Assert.assertEquals(insertedRow.getValue("TEXT"), "A99");
//		Assert.assertEquals(insertedRow.getValue("B_FK"), 1);
//		
//		// Undo insert
//		
//		ChangeItem undoItem = insertAction.getHandler().createUndoItem();
//		undoItem.getHandler().execute();
//				
//		DbRow row = qr.primaryKeyQuery("TEST_A", 99);
//		Assert.assertNull(row);
//	}
//	
//	@Test
//	public void deleteAction() throws Exception {
//		DbQueryRunner qr = this.dbConn.getQueryRunner();
//		
//		DbAction deleteAction = qr.getDeleteAction("TEST_A", 1);
//		deleteAction.getHandler().execute();
//				
//		// Try to load deleted row
//		DbRow deletedRow = qr.primaryKeyQuery("TEST_A", 1);
//		Assert.assertNull(deletedRow);
//		
//		// Undo delete
//		
//		ChangeItem undoItem = deleteAction.getHandler().createUndoItem();
//		undoItem.getHandler().execute();
//						
//		DbRow row = qr.primaryKeyQuery("TEST_A", 1);
//		Assert.assertEquals("A1", row.getValue("TEXT"));
//		Assert.assertEquals(1, row.getValue("B_FK"));
//	}
//	
//	@Test
//	public void deleteMemento() {
//		DbAction d = this.qr.getDeleteAction("TEST_A", 1);
//		GenericMementoX m = GenericMementoUtils.asX(d.createMemento());
//		Assert.assertEquals("A1", m.getStringValue("TEXT.orig"));
//		Assert.assertNull(m.getStringValue("TEXT.new"));
//	}
//	
//	@Test
//	public void datatypes() throws Exception {
//		DbAction u = this.dbConn.getQueryRunner().getUpdateAction("TEST_DATATYPES", 1);
//		
//		u.setValue("INTEGERNUMBER", 2);
//		u.setValue("VARCHARTEXT", "two");
//		u.setValue("CLOBTEXT", "twoClob");
//		
//		Date newDate = JuStringUtils.toDate("31.12.1999", JuStringUtils.DATE_FORMAT_DAYS);
//		u.setValue("DATEFIELD", newDate);
//		
//		u.getHandler().execute();
//		
//		DbRow row = this.dbConn.getQueryRunner().primaryKeyQuery("TEST_DATATYPES", 1);
//		
//		Assert.assertEquals(2, row.getValue("INTEGERNUMBER"));		
//		Assert.assertEquals("two", row.getValue("VARCHARTEXT"));
//		Assert.assertEquals("twoClob", row.getValue("CLOBTEXT"));
//		
//		Assert.assertEquals(newDate, row.getValue("DATEFIELD"));
//	}
//	
//	@Test
//	public void datatypesNull() throws Exception {
//		DbAction u = this.qr.getUpdateAction("TEST_DATATYPES", 1);
//		
//		u.setValue("INTEGERNUMBER", null);
//		u.setValue("VARCHARTEXT", null);
//		u.setValue("CLOBTEXT", null);
//		u.setValue("DATEFIELD", null);
//		
//		u.getHandler().execute();
//		
//		DbRow row = this.dbConn.getQueryRunner().primaryKeyQuery("TEST_DATATYPES", 1);
//		
//		Assert.assertNull(row.getValue("INTEGERNUMBER"));
//		Assert.assertNull(row.getValue("VARCHARTEXT"));
//		Assert.assertNull(row.getValue("CLOBTEXT"));
//		Assert.assertNull(row.getValue("DATEFIELD"));
//	}
//	
//	/**
//	 * Tests the ActionBuilder used to construct update, insert and delete DbActions.
//	 */
//	@Test
//	public void actionBuilders() {
//		DbAction updateAction = DbActionUtils.newUpdateAction(this.dbConn, "TEST_A", 1)
//				.setValue("text", "NewA1")
//				.getAction();
//		updateAction.getHandler().execute();
//		Assert.assertEquals("NewA1", this.qr.primaryKeyQuery("TEST_A", 1).getValue("Text"));
//		
//		DbAction insertAction = DbActionUtils.newInsertAction(this.dbConn, "TEST_A")
//				.setValue("aid", 100)
//				.setValue("text", "A100")
//				.getAction();
//		insertAction.getHandler().execute();
//		Assert.assertEquals("A100", this.qr.primaryKeyQuery("TEST_A", 100).getValue("text"));
//		
//		DbAction deleteAction = DbActionUtils.newDeleteAction(this.dbConn, "TEST_A", 100);
//		deleteAction.getHandler().execute();
//		Assert.assertNull(this.qr.primaryKeyQuery("TEST_A", 100));
//		
//	}
}
