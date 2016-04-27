package ch.inftec.ju.db;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.XString;

/**
 * DbSpecificHandler implementations for MySQL.
 * @author Martin
 *
 */
public class DbSpecificHandlerMySql extends DbSpecificHandlerDefault {
	public DbSpecificHandlerMySql(JuConnUtil connUtil) {
		super(connUtil);
	}
	
	@Override
	public String convertTableNameCasing(String tableName) {
		// MySQL is case sensitive, so look for the exact casing in the table list
		for (String actualTableName : this.connUtil.getMetaDataInfo().getTableNames()) {
			if (actualTableName.equalsIgnoreCase(tableName)) return actualTableName;
		}
		
		// If we were unlucky, just return the same tableName
		return tableName;
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(final int val) {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
				
				// Reset autoincrement values
				List<Map<String, Object>> res = jt.queryForList("select c.TABLE_NAME, c.COLUMN_NAME " +
					"from information_schema.columns c " +
					"where c.EXTRA='auto_increment'"); //c.TABLE_NAME='Player'" 
								
				for (Map<String, Object> row : res) {
					String tableName = row.get("TABLE_NAME").toString();
					String columnName = row.get("COLUMN_NAME").toString();
					
					logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));
					
					jt.execute(String.format("alter table %s auto_increment = %d"
							, tableName
							, val));
				}
			}
		});
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

				jt.execute(String.format("CREATE SCHEMA `%s`", builder.getName()));
				
				// Create a user as well if specified
				if (!StringUtils.isEmpty(builder.getUserName())) {
					jt.execute(String.format("CREATE USER `%s` identified by '%s'", builder.getUserName(), builder.getPassword()));
					
					// Grant all permissions for the created Schema to the user
					jt.execute(String.format("GRANT ALL ON `%s`.* TO `%s`", builder.getName(), builder.getUserName()));
				}
			}
		});
	}
	
	@Override
	public void dropSchema(final SchemaInfo schemaInfo, final String... users) {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);

				jt.execute(String.format("DROP SCHEMA `%s`", schemaInfo.getName()));
				
				for (String user : users) {
					int cnt = jt.queryForInt(String.format("select count(*) from mysql.user where user = '%s'", user));
					if (cnt > 0) {
						jt.execute(String.format("DROP USER `%s`", user));
					}
				}
			}
		});
	}
}
