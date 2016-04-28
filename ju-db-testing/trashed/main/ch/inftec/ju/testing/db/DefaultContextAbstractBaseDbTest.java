package ch.inftec.ju.testing.db;

import org.springframework.test.context.ContextConfiguration;

/**
 * Extension of the AbstractBaseDbTest class associated with its default Spring context.
 * <p>
 * This class should only be used if the default context doesn't have to be adapted. Otherwise
 * it's rather confusing how Spring beans will be looked up.
 *
 * @author tgdmemae
 *
 */
@ContextConfiguration(locations="classpath:/ch/inftec/ju/testing/db/AbstractBaseDbTest-context.xml")
public abstract class DefaultContextAbstractBaseDbTest extends AbstractBaseDbTest {
}
