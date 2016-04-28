package ch.inftec.ju.db.auth;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import ch.inftec.ju.testing.db.AbstractBaseDbTest;

/**
 * Base class for DB tests using the auth entities (AuthUser and AuthRole)
 * @author Martin
 *
 */
@ContextConfiguration(classes={AbstractAuthBaseDbTest.Config.class})
public abstract class AbstractAuthBaseDbTest extends AbstractBaseDbTest {
	@Configuration
	@ImportResource("classpath:ch/inftec/ju/db/auth/AbstractAuthBaseDbTest-context.xml")
	static class Config {
		@Bean
		public AuthDao authDao() {
			return new AuthDao();
		}
		
		@Bean
		public RoleProvider roleProvider() {
			return new RoleProvider() {
				@Override
				public List<String> getAvailableRoles() {
					return null;
				}
			};
		}
	}
}
