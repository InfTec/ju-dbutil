package ch.inftec.ju.db.specific;

import org.springframework.test.context.ContextConfiguration;

import ch.inftec.ju.db.change.DbActionTest;

@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/specific/DbTestsOracle-context.xml", inheritLocations=false)
public class OracleDbActionTest extends DbActionTest {
}
