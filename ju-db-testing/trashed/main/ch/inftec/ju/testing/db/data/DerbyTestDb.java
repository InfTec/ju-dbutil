package ch.inftec.ju.testing.db.data;

import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.testing.db.data.TestDbUtils.AbstractTestDb;

/**
 * Test database using an in-memory derby DB.
 * @author tgdmemae
 *
 */
class DerbyTestDb extends AbstractTestDb {
	@Override
	protected void createTables() throws JuDbException {
		this.jdbcTemplate.update("CREATE TABLE TEST_A ("
				+ "  Aid INTEGER not null primary key,"
				+ "  text VARCHAR(64),"
				+ "  b_fk INTEGER"
				+ ")");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_B ("
				+ "  Bid INTEGER not null primary key,"
				+ "  text VARCHAR(64)"
				+ ")");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_C ("
				+ "  Cid INTEGER not null primary key,"
				+ "  text VARCHAR(64),"
				+ "  a_fk INTEGER,"
				+ "  d_fk INTEGER"
				+ ")");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_D ("
				+ "  Did INTEGER not null primary key,"
				+ "  text VARCHAR(64)"
				+ ")");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_DATATYPES ("
				+ "  id INTEGER not null primary key,"
				+ "  integerNumber INTEGER,"
				+ "  varcharText VARCHAR(64),"
				+ "  clobText CLOB,"
				+ "  dateField DATE"
				+ ")");
	}

	@Override
	protected void cleanup() throws JuDbException {
		this.jdbcTemplate.update("DROP TABLE TEST_DATATYPES");
		
		this.jdbcTemplate.update("DROP TABLE TEST_A");
	}
	
	@Override
	protected void resetPlatformSpecificData() throws JuDbException {
		for (Class<?> clazz : JuDbUtils.getManagedTypesAsClass(this.em)) {
			// Note: This works as long as the table name is not changed by an annotation
			// or a configuration file...
			// TODO: Get TableName by EntityManager
			String tableName = clazz.getSimpleName().toUpperCase();
			
			// TODO: Works as long as the table has an auto incremented ID column... Make more robust!
			this.jdbcTemplate.update(String.format("ALTER TABLE %s ALTER COLUMN ID RESTART WITH 10", tableName));
		}
	}
}
