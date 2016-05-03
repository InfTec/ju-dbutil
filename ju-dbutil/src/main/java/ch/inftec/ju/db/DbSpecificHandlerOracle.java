package ch.inftec.ju.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.ConversionUtils;
import ch.inftec.ju.util.XString;

/**
 * DbSpecificHandler implementations for H2.
 * @author Martin
 *
 */
public class DbSpecificHandlerOracle extends DbSpecificHandlerDefault {
	public DbSpecificHandlerOracle(JuConnUtil connUtil) {
		super(connUtil);
	}
	
	@Override
	public List<String> getSequenceNames() {
		return this.queryForList("select SEQUENCE_NAME from USER_SEQUENCES", String.class);
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(final int val) {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
				
				for (String sequence : getSequenceNames()) {
				// We'll just drop and recreate the sequence.
					jt.execute("drop sequence " + sequence);
					jt.execute(String.format("create sequence %s start with %d", sequence, val));
				}
			}
		});
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		// We'll probably get a BigDecimal. With driver 11, we could query for an Object, with version 12, that doesn't work anymore...
		return ConversionUtils.toLong(this.queryForObject(String.format("select %s.nextVal from dual", sequenceName), BigDecimal.class));
	}

	@Override
	public void createSchema(final DbSchemaBuilder builder) {
		// Make sure we have all the infos we need from the builder
		AssertUtil.assertNotEmpty("Schema name is required", builder.getName());
		AssertUtil.assertNotEmpty("Schema password is required", builder.getPassword());

		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);

				// Create user

				String tableSpace = "USERS";

				XString xs = new XString();
				xs.addFormatted("CREATE USER %s IDENTIFIED BY %s", builder.getName(), builder.getPassword());
				xs.addFormatted(" DEFAULT TABLESPACE %s", tableSpace);
				xs.addFormatted(" QUOTA UNLIMITED ON %s", tableSpace);
				jt.execute(xs.toString());

				// Grants
				if (builder.isDefaultGrants()) {
					jt.execute(String.format("GRANT CREATE SESSION TO %s", builder.getName()));
					jt.execute(String.format("GRANT CREATE TABLE TO %s", builder.getName()));
					jt.execute(String.format("GRANT CREATE VIEW TO %s", builder.getName()));
					jt.execute(String.format("GRANT CREATE ANY TRIGGER TO %s", builder.getName()));
					jt.execute(String.format("GRANT CREATE ANY PROCEDURE TO %s", builder.getName()));
					jt.execute(String.format("GRANT CREATE SEQUENCE TO %s", builder.getName()));
					jt.execute(String.format("GRANT CREATE SYNONYM TO %s", builder.getName()));
				}

				// JTA recovery grants
				// (see http://docs.codehaus.org/display/BTM/FAQ#FAQ-WhyisOraclethrowingaXAExceptionduringinitializationofmydatasource?)
				if (builder.isJtaRecoveryGrants()) {
					jt.execute(String.format("GRANT SELECT ON SYS.DBA_PENDING_TRANSACTIONS TO %s", builder.getName()));
					jt.execute(String.format("GRANT SELECT ON SYS.PENDING_TRANS$ TO %s", builder.getName()));
					jt.execute(String.format("GRANT SELECT ON SYS.DBA_2PC_PENDING TO %s", builder.getName()));
					jt.execute(String.format("GRANT EXECUTE ON SYS.DBMS_SYSTEM TO %s", builder.getName()));
				}
			}
		});
	}

	@Override
	public void dropSchema(final SchemaInfo schemaInfo, String... users) {
		AssertUtil.assertTrue("Dropping of users not supported for Oracle", users.length == 0);
		
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);

				jt.execute(String.format("DROP USER %s CASCADE", schemaInfo.getName()));
			}
		});
	}
}
