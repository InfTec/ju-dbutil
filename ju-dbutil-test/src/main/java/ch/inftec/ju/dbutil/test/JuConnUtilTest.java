package ch.inftec.ju.dbutil.test;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import ch.inftec.ju.db.DbWork;
import ch.inftec.ju.db.JuConnUtil;
import ch.inftec.ju.db.JuConnUtils;
import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.util.JuUtils;


public class JuConnUtilTest extends AbstractDbTest {
	@Test
	public void juConnUtil_isSet() {
		Assert.assertNotNull(this.connUtil);
	}
	
	@Test
	public void canGet_dbType() {
		Assert.assertNotNull(this.connUtil.getDbType());
	}
	
	@Test
	public void canCreateTable_usingJdbcTemplate() {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jt = JuConnUtils.asJdbcTemplate(conn);
				
				// Drop table in case the test wasn't succesful last time
				try {
					jt.execute("drop table juConnUtilTestTable");
				} catch (Exception ex) {
					// Expected...
				}
				
				// Create new test table
				jt.execute("CREATE TABLE juConnUtilTestTable (id NUMERIC(19))");
				jt.execute("insert into juConnUtilTestTable (id) values (1)");
				
				// Query the table
				Long id = jt.queryForObject("select id from juConnUtilTestTable where id=1", Long.class);
				Assert.assertEquals(new Long(1), id);
				
				// Create a new connUtil to make sure we'll see the changes there...
				JuConnUtil connUtil2 = JuConnUtils.build()
					.profile(JuUtils.getJuPropertyChain().get("ju-dbutil-test.profile", true))
					.create();
				connUtil2.doWork(new DbWork() {
					@Override
					public void execute(Connection conn) {
						Long id2 = JuConnUtils.asJdbcTemplate(conn).queryForObject("select id from juConnUtilTestTable where id=1",
								Long.class);
						Assert.assertEquals(new Long(1), id2);
						
						// Drop table
						JuConnUtils.asJdbcTemplate(conn).execute("drop table juConnUtilTestTable");
					}
				});
			}
		});
	}
}
