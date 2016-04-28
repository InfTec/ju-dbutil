package ch.inftec.ju.db;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.util.ConversionUtils;

/**
 * DbSpecificHandler implementations for Derby.
 * @author Martin
 *
 */
public class DbSpecificHandlerDerby extends DbSpecificHandlerDefault {
	public DbSpecificHandlerDerby(JuConnUtil connUtil) {
		super(connUtil);
	}
	
	@Override
	public List<String> getSequenceNames() {
		return this.queryForList("select SEQUENCENAME name from SYS.SYSSEQUENCES", String.class);
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(final int val) {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
				
				// Reset autoincrement values
				List<Map<String, Object>> res = jt.queryForList("select t.TABLENAME, c.COLUMNNAME " +
						"from sys.SYSCOLUMNS c " +
						"  inner join sys.SYSTABLES t on t.TABLEID = c.REFERENCEID " +
						"where c.AUTOINCREMENTVALUE is not null"); 
					
				for (Map<String, Object> row : res) {
					String tableName = row.get("TABLENAME").toString();
					String columnName = row.get("COLUMNNAME").toString();
					
					logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));
					
					jt.execute(String.format("alter table %s alter %s restart with %d"
							, tableName
							, columnName
							, val));
				}
				
				// Reset sequences
				// Derby doesn't seem to support altering sequences, so we'll use drop/create
				for (String sequenceName : getSequenceNames()) {
					logger.debug(String.format("Restarting (crop/create) sequence %s with %d", sequenceName, val));
					
					jt.execute(String.format("drop sequence %s restrict", sequenceName));
					jt.execute(String.format("create sequence %s start with %d", sequenceName, val));
				}
			}
		});
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		// We'll probably get an Integer
		return ConversionUtils.toLong(this.queryForObject(String.format("values next value for %s", sequenceName), Object.class));
	}
}
