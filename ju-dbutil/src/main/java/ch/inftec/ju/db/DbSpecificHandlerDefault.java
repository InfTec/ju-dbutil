package ch.inftec.ju.db;

import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.db.JuConnUtil.DbHandler;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;
import ch.inftec.ju.util.DataHolder;

/**
 * Default implementation of DbSpecificHandler. DB specific handlers can extend this class
 * and only have to override a method if the DB doesn't comply with the default handling.
 * @author Martin
 *
 */
public abstract class DbSpecificHandlerDefault implements DbSpecificHandler, DbHandler {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected final JuConnUtil connUtil;
	
	protected DbSpecificHandlerDefault(JuConnUtil connUtil) {
		this.connUtil = connUtil;
	}
	
	protected <T> List<T> queryForList(final String sql, final Class<T> listType) {
		final DataHolder<List<T>> res = new DataHolder<>();
		
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
				res.setValue(jt.queryForList(sql, listType));
			}
		});
		
		return res.getValue();
	}
	
	protected <T> T queryForObject(final String sql, final Class<T> requiredType) {
		final DataHolder<T> res = new DataHolder<>();
		
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
				res.setValue(jt.queryForObject(sql, requiredType));
			}
		});
		
		return res.getValue();
	}
	
	@Override
	public void createSchema(DbSchemaBuilder builder) {
		throw new JuDbException("Schema Creation not yet implemented for " + this.connUtil.getDbType());
	}

	@Override
	public String convertTableNameCasing(String tableName) {
		return tableName.toUpperCase();
	}
	
	@Override
	public List<String> getSequenceNames() {
		throw new JuDbException("Sequences not supported by " + this.connUtil.getDbType());
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		throw new JuDbException("Sequences not supported by " + this.connUtil.getDbType());
	}
	
	@Override
	public String wrapInLowerString(String expression) {
		return String.format("lower(%s)", expression);
	}

	@Override
	public DbSchemaBuilder createSchema() {
		return new DbSchemaBuilder(this);
	}

	@Override
	public void dropSchema(SchemaInfo schemaInfo, String... users) {
		throw new JuDbException("Schema Dropping not yet implemented for " + this.connUtil.getDbType());
	}
}
