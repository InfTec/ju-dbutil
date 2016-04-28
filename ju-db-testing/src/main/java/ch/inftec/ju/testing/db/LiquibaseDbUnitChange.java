package ch.inftec.ju.testing.db;

import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import ch.inftec.ju.util.AssertUtil;

public class LiquibaseDbUnitChange implements CustomTaskChange {
	private String dataSet;
	private URL dataSetUrl;
	
	private boolean cleanInsert = true;
	
	private ResourceAccessor resourceAccessor;
	
	@Override
	public String getConfirmationMessage() {
		return String.format("Imported data from dataSet %s as %s"
				, dataSetUrl
				, (this.cleanInsert ? "CleanInsert" : "Insert"));
	}

	@Override
	public void setUp() throws SetupException {
		AssertUtil.assertNotEmpty("Parameter 'dataSet' must be set to the DbUnit dataSet to be executed", this.dataSet);
		
		// Try to lookup change set and make sure we find exactly one resource
		try {
			Enumeration<URL> urls = this.resourceAccessor.getResources(this.dataSet);
			AssertUtil.assertTrue("ResourceAccessor couldn't locate resource", urls.hasMoreElements());
			this.dataSetUrl = urls.nextElement();
			AssertUtil.assertFalse("ResourceAccessor found more than one resource", urls.hasMoreElements());
		} catch (Exception ex) {
			throw new SetupException("Couldn't find change set resource " + this.dataSet, ex);
		}
		
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		AssertUtil.assertTrue("Expected JdbcConnection to be able to extract raw JDBC connection", database.getConnection() instanceof JdbcConnection);
		JdbcConnection lbConn = (JdbcConnection) database.getConnection();
		Connection conn = lbConn.getWrappedConnection();
		DbDataUtil.executeInsert(conn, this.dataSetUrl, this.cleanInsert);
	}

	public void setDataSet(String dataSet) {
		this.dataSet = dataSet;
	}
	
	/**
	 * Sets if the dataSet should be inserted as a cleanInsert.
	 * <p>
	 * Default is true.
	 * @param cleanInsert If true, dataSet will be imported as a clean insert. If false, a normal insert will be performed.
	 */
	public void setCleanInsert(boolean cleanInsert) {
		this.cleanInsert = cleanInsert;
	}
}
