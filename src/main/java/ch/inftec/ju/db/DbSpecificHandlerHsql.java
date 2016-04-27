package ch.inftec.ju.db;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.util.ConversionUtils;

/**
 * DbSpecificHandler implementations for HSQL.
 * 
 * @author Martin
 *
 */
public class DbSpecificHandlerHsql extends DbSpecificHandlerDefault {
	public DbSpecificHandlerHsql(JuConnUtil connUtil) {
		super(connUtil);
	}
	
	@Override
	public List<String> getSequenceNames() {
		return this.queryForList("select SEQUENCE_NAME name from INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_NAME not in ('LOB_ID')",
				String.class);
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(final int val) {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);

				// Reset autoincrement values
				// Reset autoincrement values
				List<Map<String, Object>> res = jt.queryForList("select c.TABLE_NAME, c.COLUMN_NAME " + // , c.* " +
						"from information_schema.columns c " +
						"where c.IDENTITY_GENERATION is not null");// c.TABLE_NAME='PLAYER'");

				for (Map<String, Object> row : res) {
					String tableName = row.get("TABLE_NAME").toString();
					String columnName = row.get("COLUMN_NAME").toString();

					logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));

					jt.execute(String.format("alter table %s alter column %s RESTART WITH %d"
							, tableName
							, columnName
							, val));
				}
		
				// Reset sequences. Auto increment columns are internally implemented by sequences, too (named SYSTEM_SEQUENCE_...),
				// so resetting all sequences will also cause the identity columns to be resetted
				for (final String sequenceName : getSequenceNames()) {
					logger.debug(String.format("Restarting sequence %s with %d", sequenceName, val));
					jt.execute(String.format("alter sequence %s restart with %d increment by %d"
						, sequenceName
						, val
						, 1));
				}
			}
		});
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		// We seem to get a BigInteger, so convert to a Long
		return ConversionUtils.toLong(this.queryForObject(String.format("call NEXT VALUE FOR %s", sequenceName), Object.class));
	}
}
