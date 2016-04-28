package ch.inftec.ju.db;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.testing.db.DefaultContextAbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;
import ch.inftec.ju.util.JuCollectionUtils;


/**
 * Test basic database interactions. The default implementation uses a Derby DB.
 * <p>
 * Note that some tests might run succesfully, but output ERROR or WARN debug messages
 * that should be examined.
 * @author tgdmemae
 *
 */
public class BasicDbTest extends DefaultContextAbstractBaseDbTest {
	@Autowired
	private JuDbUtils juDbUtils;
	
//	@Test
//	public void dbRowResultSetHandler() throws Exception {
//		DbQueryRunner qr = this.dbConn.getQueryRunner();
//		
//		// Single row result
//		DbRows dbRows = qr.query("SELECT * FROM TEST_A WHERE AID=1");
//		
//		Assert.assertEquals(dbRows.getRowCount(), 1);		
//		this.assertRowEquals(dbRows.getRow(0), "AID", 1, "TEXT", "A1", "B_FK", 1);
//		
//		// DbRowsImpl.equals and hashcode
//		
//		DbRows dbRows2 = qr.query("SELECT * FROM TEST_A WHERE AID=1");
//		Assert.assertEquals(dbRows.hashCode(), dbRows2.hashCode());
//		Assert.assertEquals(dbRows, dbRows2);
//		
//		// Empty result
//		dbRows = qr.query("SELECT * FROM TEST_A WHERE 1=0");
//		Assert.assertEquals(dbRows.getRowCount(), 0);
//		Assert.assertEquals(dbRows.getColumnCount(), 3);
//		Assert.assertEquals(dbRows.getColumnName(0), "AID");
//	}
	
	@Test
	public void getTableNames() throws Exception {
		List<String> tableNames = this.juDbUtils.getTableNames();
		
		Assert.assertTrue(tableNames.contains("PLAYER"));
		Assert.assertTrue(tableNames.contains("TESTINGENTITY"));
	}
	
	@Test
	public void getPrimaryColumnName() throws Exception {
		Assert.assertEquals(this.juDbUtils.getPrimaryColumnName("TEST_A"), "AID");
		Assert.assertEquals(this.juDbUtils.getPrimaryColumnName("test_a"), "AID");
	}
	
	@Test
	public void getColumnNames() throws Exception {
		 Assert.assertTrue(JuCollectionUtils.collectionEquals(this.juDbUtils.getColumnNames("TEST_A"), JuCollectionUtils.arrayList("AID", "TEXT", "B_FK")));
	}
	
	@Test
	public void testDbSequence_isResetTo10_test1() {
		this.entityIdTest();
	}
	
	@Test
	public void testDbSequence_isResetTo10_test2() {
		this.entityIdTest();
	}
	
	public void entityIdTest() {
		TestingEntity te = new TestingEntity();
		this.em.persist(te);
		
		Assert.assertEquals(new Long(10), te.getId());
	}
	
//	@Test
//	public void queryRunner() throws Exception {
//		DbRows dbRows = this.dbConn.getQueryRunner().query("SELECT * FROM TEST_A WHERE AID IN (?, ?) ORDER BY AID ASC", 1, 2);
//		Assert.assertEquals(2, dbRows.getRowCount());
//		this.assertRowEquals(dbRows.getRow(0), "AID", 1, "TEXT", "A1", "B_FK", 1);
//		this.assertRowEquals(dbRows.getRow(1), "AID", 2, "TEXT", "A2", "B_FK", 2);
//	}
	
//	/**
//	 * Test the retrieval of different data types.
//	 */
//	@Test
//	public void datatypes() throws Exception {
//		DbRow row = this.dbConn.getQueryRunner().primaryKeyQuery("TEST_DATATYPES", 1);
//		
//		Assert.assertEquals(1, row.getValue("INTEGERNUMBER"));		
//		Assert.assertEquals("one", row.getValue("VARCHARTEXT"));
//		Assert.assertEquals("oneClob", row.getValue("CLOBTEXT"));
//		
//		Date date = JuStringUtils.toDate("3.12.1980", JuStringUtils.DATE_FORMAT_DAYS);
//		Assert.assertEquals(date, row.getValue("DATEFIELD"));
//	}
//	
//	@Test
//	public void datatypesNull() throws Exception {
//		DbRow row = this.dbConn.getQueryRunner().primaryKeyQuery("TEST_DATATYPES", 2);
//		
//		Assert.assertNull(row.getValue("INTEGERNUMBER"));
//		Assert.assertNull(row.getValue("VARCHARTEXT"));
//		Assert.assertNull(row.getValue("CLOBTEXT"));
//		Assert.assertNull(row.getValue("DATEFIELD"));
//	}
	

	
//	@Test
//	public void close() throws Exception {
//		// Makes sure a close doesn't commit any changes made since the last commit
//		
//		SqlConnection newConn = this.createConnection();
//		MutableRowHandler handler = new MutableRowHandler();
//		
//		// Make a first change
//		
//		SqlMutableRow updateRow1 = handler.getRow("TEST_A", 1L, newConn);
//		updateRow1.setValue("TEXT", "A1New");
//		
//		UpdateAction update1 = new UpdateAction(updateRow1);
//		update1.execute(newConn);
//		
//		// Commit the first change
//		newConn.commit();
//		
//		// Make another change
//		
//		SqlMutableRow updateRow2 = handler.getRow("TEST_A", 2L, newConn);
//		updateRow2.setValue("TEXT", "A2New");
//		
//		UpdateAction update2 = new UpdateAction(updateRow2);
//		update2.execute(newConn);
//		
//		// Just close the connection without committing explicitly
//		newConn.close();
//		
//		// Make sure the second change hasn't been committed
//		SqlMutableRow row1 = handler.getRow("TEST_A", 1L, this.conn);
//		assertEquals(row1.getValue("TEXT"), "A1New");
//		SqlMutableRow row2 = handler.getRow("TEST_A", 2L, this.conn);
//		assertEquals(row2.getValue("TEXT"), "A2");
//	}
//	
//	@Test
//	public void testConnectionUncached() throws Exception {
//		SqlConnection newConn = this.createUncachedConnection();
//		newConn.testConnection();
//		
//		newConn.close();
//		
//		try {
//			newConn.testConnection();
//			Assert.fail("Test connection didn't return exception after close");
//		} catch (DrSqlException ex) {
//			log.info("Test connection failed as expected: " + ex);
//		}
//	}
//	
//	@Test
//	public void testConnection() throws Exception {
//		SqlConnection newConn = this.createConnection();
//		newConn.testConnection();
//		
//		newConn.close();
//		
//		try {
//			newConn.testConnection();
//			Assert.fail("Test connection didn't return exception after close");
//		} catch (DrSqlException ex) {
//			log.info("Test connection failed as expected: " + ex);
//		}
//	}
//	
//	@Test
//	public void select() throws Exception {
//		SqlResultSet res = this.conn.executeQuery("SELECT * FROM TEST_A");
//		
//		assertEquals(res.getRowCount(), 3);
//		assertEquals(res.getRowValues(0).length, 3);
//	}
//	
//	@Test
//	public void resultSet() throws Exception {
//		SqlResultSet res = this.conn.executeQuery("SELECT TEXT FROM TEST_A WHERE AID=1");
//		assertEquals(res.getRowCount(), 1);
//		assertEquals(res.getRowValues(0).length, 1);
//		assertEquals(res.getRowValues(0)[0].getValue(), "A1");
//		
//		//res.getRowValues(0)[0] = new SqlValueImpl("X1", res.getRowValues(0)[0].getColumnType(), false);
//		//assertEquals(res.getRowValues(0)[0].getValue(), "X1");
//	}
//	
//	@Test
//	public void primaryColumn() throws Exception {
//		assertEquals(this.conn.getPrimaryColumnName("TEST_A"), "AID");
//	}
//	
//	@Test
//	public void mutableRow() throws Exception {
//		MutableRowHandler handler = new MutableRowHandler();
//		
//		// Test existing row
//		SqlMutableRow row = handler.getRow("TEST_A", 1, this.conn);
//		
//		assertEquals(row.getValue("AID"), 1);
//		assertEquals(row.getValue("TEXT"), "A1");
//		
//		assertEquals(row.getValue("TEXT"), row.getOriginalValue("TEXT"));
//		row.setValue("TEXT", "NewA1");
//		assertEquals(row.getValue("TEXT"), "NewA1");
//		assertEquals(row.getOriginalValue("TEXT"), "A1");
//		
//		// Test empty row
//		SqlMutableRow emptyRow = handler.getRow("TEST_A", -1, this.conn);
//		
//		assertNull(emptyRow.getValue("AID"));
//		emptyRow.setValue("AID", 99);
//		assertEquals(emptyRow.getValue("AID"), 99);
//		assertNull(emptyRow.getOriginalValue("AID"));
//		
//		// Test equals
//		SqlMutableRow rowA1a = handler.getRow("TEST_A", 1L, this.conn);
//		SqlMutableRow rowA1b = handler.getRow("TEST_A", 1L, this.conn);
//		
//		assertEquals(rowA1a, rowA1b);
//		
//		rowA1b.setValue("TEXT", "bla");
//		assertFalse(rowA1a.equals(rowA1b));
//		
//		// Rows are considered equals if their current values match
//		SqlMutableRow rowA2 = handler.getRow("TEST_A", 2L, this.conn);
//		assertFalse(rowA2.equals(rowA1a));
//		for (int i = 0; i < rowA2.getColumnCount(); i++) {
//			rowA2.setValue(rowA2.getColumnName(i), rowA1a.getValue(rowA2.getColumnName(i)));
//		}
//		assertEquals(rowA2, rowA1a);
//	}
//	
//	@Test
//	public void getMutableRows() throws DrSqlException {
//		MutableRowHandler handler = new MutableRowHandler();
//		
//		SqlResultSet resultSet = this.conn.executeQuery("SELECT * FROM TEST_A WHERE AID IN (1, 2, 3) ORDER BY AID");
//		SqlMutableRow rows[] = handler.getMutableRows("TEST_A", resultSet, this.conn);
//		
//		assertEquals(rows.length, 3);
//		assertEquals(rows[0].getValue("TEXT"), "A1");
//		assertEquals(rows[1].getValue("TEXT"), "A2");
//		assertEquals(rows[2].getValue("TEXT"), "A3");
//	}
//	
//	
//	
//	
//	@Test
//	public void sqlValueFormatter() throws Exception {
//		SqlValueFormatter formatter = this.getConnectionInfo().getFormatter();
//		
//		MutableRowHandler handler = new MutableRowHandler();
//		SqlMutableRow row = handler.getRow("TEST_DATATYPES", 1, this.conn);
//		
//		// Test formatting
//		
//		assertEquals(formatter.toString(row.getValue("INTEGERNUMBER")), "1");
//		assertEquals(formatter.toString(row.getValue("VARCHARTEXT")), "one");
//		assertEquals(formatter.toString(row.getValue("CLOBTEXT")), "oneClob");
//		assertEquals(formatter.toString(row.getValue("DATEFIELD")), "03.12.1980 00:00:00");
//		
//		// Test parsing
//		
//		SqlValue val;
//		val = formatter.parse("1", row.getValue("INTEGERNUMBER").getColumnType());
//		assertEquals(val, row.getValue("INTEGERNUMBER"));
//		val = formatter.parse("one", row.getValue("VARCHARTEXT").getColumnType());
//		assertEquals(val, row.getValue("VARCHARTEXT"));
//		val = formatter.parse("oneClob", row.getValue("CLOBTEXT").getColumnType());
//		assertEquals(val, row.getValue("CLOBTEXT"));
//		val = formatter.parse("03.12.1980 00:00:00", row.getValue("DATEFIELD").getColumnType());
//		assertEquals(val, row.getValue("DATEFIELD"));
//		val = formatter.parse("03.12.1980 00:00", row.getValue("DATEFIELD").getColumnType());
//		assertEquals(val, row.getValue("DATEFIELD"));
//		val = formatter.parse("03.12.1980", row.getValue("DATEFIELD").getColumnType());
//		assertEquals(val, row.getValue("DATEFIELD"));
//		
//		// Test null formatting
//		
//		row.setValue("INTEGERNUMBER", null);
//		row.setValue("VARCHARTEXT", null);
//		row.setValue("CLOBTEXT", null);
//		row.setValue("DATEFIELD", null);
//		assertEquals(formatter.toString(row.getValue("INTEGERNUMBER")), "");
//		assertEquals(formatter.toString(row.getValue("VARCHARTEXT")), "");
//		assertEquals(formatter.toString(row.getValue("CLOBTEXT")), "");
//		assertEquals(formatter.toString(row.getValue("DATEFIELD")), "");
//	}
//	
//	private SqlActionSet createTestSet() throws DrSqlException {
//		ActionSetBuilder sb = new ActionSetBuilder();
//		
//		MutableRowHandler handler = new MutableRowHandler();
//		
//		// Prepare set
//		
//		sb.newSet("ActionSet Test");
//		
//		sb.newSet("Update");		
//		SqlMutableRow updateRow = handler.getRow("TEST_A", 1, this.conn);
//		updateRow.setValue("TEXT", "NewA1");
//		sb.addAction(new UpdateAction(updateRow));		
//		sb.endSet();
//		
//		sb.newSet("Delete");
//		SqlMutableRow deleteRow = handler.getRow("TEST_A", 2, this.conn);
//		sb.addAction(new DeleteAction(deleteRow));
//		sb.endSet();
//		
//		sb.newSet("Insert");
//		SqlMutableRow insertRow = handler.getRow("TEST_A", -1, this.conn);
//		insertRow.setValue("AID", 99L);
//		insertRow.setValue("TEXT", "A99");
//		insertRow.setValue("B_FK", 999L);
//		sb.addAction(new InsertAction(insertRow));
//		sb.endSet();
//		
//		return sb.getRootSet();
//	}
//	
//	@Test
//	public void actionSetExecutor() throws Exception {
//		MutableRowHandler handler = new MutableRowHandler();
//		
//		ActionSetExecutor executor = new ActionSetExecutor();
//		executor.setActionSet(this.createTestSet());
//		executor.setUserName("actionSetTest");
//		
//		// Add listeners
//		EventTester<ActionSetEventObject> eventTester = new EventTester<ActionSetEventObject>();
//		executor.addExecutionListener(eventTester);
//		
//		// Execute set
//		executor.execute(this.conn, this.getConnectionInfo());
//		
//		// Check if data was changed
//		
//		SqlMutableRow updatedRow = handler.getRow("TEST_A", 1, this.conn);
//		assertEquals(updatedRow.getValue("TEXT"), "NewA1");
//		
//		SqlMutableRow deletedRow = handler.getRow("TEST_A", 2, this.conn);
//		assertNull(deletedRow.getValue("AID"));
//		
//		SqlMutableRow insertedRow = handler.getRow("TEST_A", 99, this.conn);
//		assertEquals(insertedRow.getValue("AID"), 99);
//		assertEquals(insertedRow.getValue("TEXT"), "A99");
//		assertEquals(insertedRow.getValue("B_FK"), 999L);
//		
//		// Check tree export
//		
//		String setExport = ExportUtil.toXString(eventTester.getLastObject()).toString();
//		TestUtil.assertEqualsResource(
//				setExport, 
//				"actionSetExecutor_dbSet.txt", 
//				"userName", executor.getUserName(),
//				"date", StringUtil.DATE_FORMAT_SECONDS.format(executor.getExecutionDate()),
//				"database", this.getConnectionInfo().getName());
//		
//		// Undo changes
//		
//		ActionSetExecutor undoExecutor = new ActionSetExecutor();
//		undoExecutor.setActionSet(executor.getUndoActionSet());
//		undoExecutor.setUserName("actionSetTest undo");
//		undoExecutor.addExecutionListener(eventTester);
//		
//		// Execute undo set
//		undoExecutor.execute(this.conn, this.getConnectionInfo());
//		
//		// Check if data was reset
//		
//		SqlMutableRow origA1 = handler.getRow("TEST_A", 1, this.conn);
//		assertEquals(origA1.getValue("TEXT"), "A1");
//		
//		SqlMutableRow origA2 = handler.getRow("TEST_A", 2, this.conn);
//		assertEquals(origA2.getValue("AID"), 2);
//		assertEquals(origA2.getValue("TEXT"), "A2");
//		assertEquals(origA2.getValue("B_FK"), 2);
//		
//		SqlMutableRow origA99 = handler.getRow("TEST_A", 99, this.conn);
//		assertNull(origA99.getValue("AID"));
//		
//		// Check tree export
//		
//		String undoSetExport = ExportUtil.toXString(eventTester.getLastObject()).toString();
//		TestUtil.assertEqualsResource(
//				undoSetExport, 
//				"actionSetExecutor_dbSetUndo.txt", 
//				"userName", undoExecutor.getUserName(),
//				"date", StringUtil.DATE_FORMAT_SECONDS.format(undoExecutor.getExecutionDate()),
//				"database", this.getConnectionInfo().getName());
//	}
//	
//	@Test
//	public void cachedDataLoader() throws Exception {
//		CachedConnection cachedConn = new CachedConnection(this.conn);
//		
//		MutableRowHandler handler = new MutableRowHandler();
//
//		// Get value through cached connection
//		SqlMutableRow row = handler.getRow("TEST_A", 1L, cachedConn);
//		assertEquals(row.getValue("TEXT"), "A1");
//		
//		// Update value
//		row.setValue("TEXT", "NewA1");
//		SqlAction update = new UpdateAction(row);
//		update.execute(cachedConn);
//		
//		// Get value. Should be cached, i.e. not changed.
//		SqlMutableRow cachedRow = handler.getRow("TEST_A", 1L, cachedConn);
//		assertEquals(cachedRow.getValue("TEXT"), "A1");
//		
//		// Reset cached and reload value. Should be changed then.
//		cachedConn.clearCache();
//		
//		SqlMutableRow updatedRow = handler.getRow("TEST_A", 1L, cachedConn);
//		assertEquals(updatedRow.getValue("TEXT"), "NewA1");
//	}
//	
//	@Test
//	public void cloneRows() throws Exception {
//		MutableRowHandler handler = new MutableRowHandler();
//		
//		SqlMutableRow row1 = handler.getRow("TEST_A", 1L, this.conn);
//		SqlMutableRow row2 = handler.getRow("TEST_A", 2L, this.conn);
//		
//		SqlMutableRow rows[] = new SqlMutableRow[] {row1, row2};
//		SqlMutableRow clonedRows[] = handler.cloneRows(rows);
//		
//		assertArrayEquals(rows, clonedRows);
//	}
//	
//	@Test
//	public void preparedStatementStressTest() throws Exception {
//		for (int i = 0; i < 1000; i++) {
//			this.conn.executeQuery("SELECT * FROM TEST_A WHERE Aid=" + (-i));
//		}
//	}
//	
//	@Test
//	public void cachedConnection() throws Exception {
//		TestUtil.runInternalTests(CachedConnection.class, this.conn);
//	}
//	
//	@Test
//	public void columnNames() throws Exception {
//		String columnNames[] = this.conn.getColumnNames("TEST_A");
//		assertArrayEquals(columnNames, new String[] {"AID", "TEXT", "B_FK"});
//	}
}
