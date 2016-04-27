package ch.inftec.ju.db;

import java.sql.Connection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.util.ConversionUtils;

/**
 * DbSpecificHandler implementations for H2.
 * @author Martin
 *
 */
public class DbSpecificHandlerH2 extends DbSpecificHandlerDefault {
	public DbSpecificHandlerH2(JuConnUtil connUtil) {
		super(connUtil);
	}
	
	@Override
	public List<String> getSequenceNames() {
		return this.queryForList("select SEQUENCE_NAME name from INFORMATION_SCHEMA.SEQUENCES", String.class);
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(final int val) {
//		// Reset auto increment columns
//		List<?> res = this.em.createNativeQuery(
//				"select c.TABLE_NAME, c.COLUMN_NAME " +
//				"from information_schema.columns c " +
//				"where c.SEQUENCE_NAME is not null").getResultList();
//			
//		for (Object row : res) {
//			Object[] aRow = (Object[]) row;
//			String tableName = aRow[0].toString();
//			String columnName = aRow[1].toString();
//			
//			logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));
//			
//			this.em.createNativeQuery(String.format("alter table %s alter column %s restart with %d"
//					, tableName
//					, columnName
//					, val)).executeUpdate();
//		}
		
		// Reset sequences. Auto increment columns are internally implemented by sequences, too (named SYSTEM_SEQUENCE_...),
		// so resetting all sequences will also cause the identity columns to be resetted
		for (final String sequenceName : this.getSequenceNames()) {
			logger.debug(String.format("Restarting sequence %s with %d", sequenceName, val));
			
			this.connUtil.doWork(new DbWork() {
				@Override
				public void execute(Connection conn) {
					JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
					jt.execute(String.format("alter sequence %s restart with %d increment by %d"
						, sequenceName
						, val
						, 1));
				}
			});
		}
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		// We seem to get a BigInteger, so convert to a Long
		return ConversionUtils.toLong(this.queryForObject(String.format("select NEXTVAL('%s')", sequenceName), Object.class));
	}
}
