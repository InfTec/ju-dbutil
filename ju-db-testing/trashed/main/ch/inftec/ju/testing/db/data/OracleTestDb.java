package ch.inftec.ju.testing.db.data;

import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.testing.db.data.TestDbUtils.AbstractTestDb;

/**
 * Test database using an in-memory derby DB.
 * @author tgdmemae
 *
 */
class OracleTestDb extends AbstractTestDb {
	private static OracleTestDb instance;
	
	public static OracleTestDb getInstance() throws JuDbException {
		if (OracleTestDb.instance == null) {
			OracleTestDb.instance = new OracleTestDb();
		}
		return OracleTestDb.instance;
	}

	@Override
	protected void createTables() throws JuDbException {
		this.dropTable("TEST_A");		
		this.jdbcTemplate.update("CREATE TABLE TEST_A ("
				+ "  Aid NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64),"
				+ "  b_fk NUMBER(19)"
				+ ")");
		
		this.dropTable("TEST_B");
		this.jdbcTemplate.update("CREATE TABLE TEST_B ("
				+ "  Bid NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64)"
				+ ")");
		
		this.dropTable("TEST_C");
		this.jdbcTemplate.update("CREATE TABLE TEST_C ("
				+ "  Cid NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64),"
				+ "  a_fk NUMBER(19),"
				+ "  d_fk NUMBER(19)"
				+ ")");
		
		this.dropTable("TEST_D");
		this.jdbcTemplate.update("CREATE TABLE TEST_D ("
				+ "  Did NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64)"
				+ ")");
		
		this.dropTable("TEST_DATATYPES");
		this.jdbcTemplate.update("CREATE TABLE TEST_DATATYPES ("
				+ "  id NUMBER(19) not null primary key,"
				+ "  integerNumber NUMBER(19),"
				+ "  varcharText VARCHAR(64),"
				+ "  clobText CLOB,"
				+ "  dateField DATE"
				+ ")");
	}
	
	private void dropTable(String tableName) {
		int res = this.jdbcTemplate.queryForInt("select count(*) from user_tables where table_name=?", tableName);
		if (res > 0) {
			this.jdbcTemplate.update("DROP TABLE " + tableName);
		}
	}
	
	@Override
	protected void cleanup() throws JuDbException {
		this.jdbcTemplate.update("DROP TABLE TEST_DATATYPES");
		
		this.jdbcTemplate.update("DROP TABLE TEST_A");
	}
	
	@Override
	protected void resetPlatformSpecificData() throws JuDbException {
		this.juDbUtils.oracleSequenceSetNextVal("hibernate_sequence", 10);
	}
}
