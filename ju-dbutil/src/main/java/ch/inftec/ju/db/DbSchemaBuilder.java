package ch.inftec.ju.db;


/**
 * Helper class to create DB Schemas (or users).
 * 
 * @author martin.meyer@inftec.ch
 *
 */
public class DbSchemaBuilder {
	private final DbSpecificHandler dbSpecificHandler;
	private String name;
	private String password;
	private String userName;

	private boolean defaultGrants = true;
	private boolean jtaRecoveryGrants = false;

	DbSchemaBuilder(DbSpecificHandler dbSpecificHandler) {
		this.dbSpecificHandler = dbSpecificHandler;
	}

	/**
	 * Name of the Schema / User.
	 * 
	 * @param name
	 * @return
	 */
	public DbSchemaBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Password for the Schema.
	 * @param password
	 * @return
	 */
	public DbSchemaBuilder password(String password) {
		this.password = password;
		return this;
	}

	/**
	 * User to create. Only available for databases that distinguish between user and schema.
	 * @param userName
	 * @param password
	 * @return
	 */
	public DbSchemaBuilder user(String userName) {
		this.userName = userName;
		return this;
	}
	
	/**
	 * Sets whether JTA recovery grants should be granted to the user to be created.
	 * <p>
	 * Defaults to false
	 * <p>
	 * See http://docs.codehaus.org/display/BTM/FAQ#FAQ-WhyisOraclethrowingaXAExceptionduringinitializationofmydatasource? for details.
	 * 
	 * @param grantJtaRecovery
	 * @return
	 */
	public DbSchemaBuilder jtaRecoveryGrants(boolean jtaRecoveryGrants) {
		this.jtaRecoveryGrants = true;
		return this;
	}

	String getName() {
		return name;
	}

	String getPassword() {
		return password;
	}
	
	String getUserName() {
		return userName;
	}

	boolean isDefaultGrants() {
		return this.defaultGrants;
	}

	boolean isJtaRecoveryGrants() {
		return this.jtaRecoveryGrants;
	}

	/**
	 * Creates the schema as defined.
	 */
	public void create() {
		this.dbSpecificHandler.createSchema(this);
	}
}
