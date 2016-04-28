package ch.inftec.ju.db.specific;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.inftec.ju.db.BasicDbTest;
import ch.inftec.ju.db.JpaTest;
import ch.inftec.ju.db.JuDbUtilsTest;
import ch.inftec.ju.db.MultiplePersistenceUnitsTest;
import ch.inftec.ju.db.auth.AuthenticationTest;
import ch.inftec.ju.db.change.DbActionTest;
import ch.inftec.ju.db.change.DbChangeSetTest;

@RunWith(Suite.class)
@SuiteClasses({ BasicDbTest.class, JpaTest.class, JuDbUtilsTest.class, MultiplePersistenceUnitsTest.class,
	AuthenticationTest.class, DbActionTest.class, DbChangeSetTest.class})
public class AllTestsDerby {

}
