package ch.inftec.ju.db;

import java.io.BufferedReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.ManagedType;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuObjectUtils;

/**
 * DB utility class. Mainly contains wrapper method for Apache commons DbUtils
 * to encapsulate the SQLExceptions and throw JuDbExceptions or to provide
 * log information on failed methods.
 * <p>
 * To use certain features, an EntityManagerFactory instance needs to be injected
 * using the provided setEntityManagerFactory method:
 * <ul>
 *   <li>createDefaultTables: Create tables on Schema based on JPA entities of the factories Entities</li>
 * </ul>
 * 
 * @author tgdmemae
 * 
 * @deprecated Use JuEmUtil instead
 *
 */
@Deprecated
public class JuDbUtils {
	// TODO: Refactor, remove Apache DbUtils...
	static Logger log = LoggerFactory.getLogger(JuDbUtils.class);

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private ConnectionInfo connectionInfo;
	
	private EntityManagerFactory emf;
	
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}
	
	/**
	 * Creates a new JuDbUtils instance for the specified Persistence Unit.
	 * <p>
	 * Note that this creates a new EntityManagerFactory every time the method is called
	 * @param persistenceUnitName Name of the Persistence Unit
	 * @return JuDbUtils instance
	 */
	public static JuDbUtils createByPersistenceUnitName(String persistenceUnitName) {
		JuDbUtils utils = new JuDbUtils();
		utils.setEntityManagerFactory(Persistence.createEntityManagerFactory(persistenceUnitName));
		return utils;
	}
	
	/**
	 * Gets whether a Spring transaction is active in our current context.
	 * @return True if we are within a Spring transaction, false if we're not.
	 */
	public static boolean isSpringTransactionActive() {
		return TransactionSynchronizationManager.isActualTransactionActive();
	}
	
	/**
	 * Gets the specified JpaRepository interface.
	 * @param em Backing EntityManager
	 * @param repositoryClass Repository interface class
	 * @return JPA repository interface
	 */
	public static <T> T getJpaRepository(EntityManager em, Class<T> repositoryClass) {
		JpaRepositoryFactory repositoryFactory = new JpaRepositoryFactory(em);
		return repositoryFactory.getRepository(repositoryClass);
	}

	/**
	 * Creates JPA default tables for the current EntityManager.
	 * <p>
	 * Only works with EclipseLink currently.
	 */
	@Transactional
	public void createDefaultTables() {
		AssertUtil.assertNotNull("EntityManagerFactory needs to be injected to create default tables", this.emf);
		
		EntityManager em = this.emf.createEntityManager();
		
		final Configuration conf = new Configuration();
		for (Class<?> clazz : JuDbUtils.getManagedTypesAsClass(em)) {
			conf.addAnnotatedClass(clazz);
		}
		
		conf.getProperties().put("hibernate.dialect", this.emf.getProperties().get("hibernate.dialect"));
		
		Session session = (Session)em.getDelegate();
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				SchemaExport export = new SchemaExport(conf, connection);
				export.create(true, true);
			}
		});
		
		em.close();
	}
	
	/**
	 * Executes some DB work using a raw JDBC connection.
	 * <p>
	 * Makes use of the Hibernate Work facility.
	 * @param em EntityManager that will be used to unwrap the raw connection. We'll also be joining
	 * the transaction (if any) of the EntityManager.
	 * @param work Work callback interface
	 */
	public static void doWork(EntityManager em, Work work) {
		Session session = em.unwrap(Session.class);
		session.doWork(work);
	}
	
	/**
	 * Executes some DB work using a raw JDBC connection.
	 * <p>
	 * Makes use of the Hibernate Work facility.
	 * @param work Work callback interface
	 */
	public void doWork(final Work work) {
		this.doWork(new EmWork() {
			@Override
			public void execute(EntityManager em) {
				JuDbUtils.doWork(em, work);
			}
		});
	}
	
	/**
	 * Executes some DB work using an EntityManager.
	 * @param work to be executed 
	 */
	public void doWork(EmWork work) {
		EntityManager em = null;
		try {
			em = this.emf.createEntityManager();
			
			em.getTransaction().begin();
			try {
				work.execute(em);
				em.getTransaction().commit();
			} catch (Exception ex) {
				em.getTransaction().rollback();
				throw ex;
			}
		} finally {
			em.close();
		}
	}
	
	/**
	 * Helper function that returns all managed types (i.e. entities) of the specified EntityManager
	 * as a list of Java Class objects.
	 * @param em
	 * @return List of Class<?> objects of the DB entity classes (annotated with @Entity)
	 */
	public static List<Class<?>> getManagedTypesAsClass(EntityManager em) {
		List<Class<?>> classes = new ArrayList<>();
		
		for (ManagedType<?> t : em.getMetamodel().getManagedTypes()) {
			Class<?> clazz = t.getJavaType();
			classes.add(clazz);
		}
		
		return classes;
	}
	
	/**
	 * Commits and closes a connection.
	 * @param conn Connection
	 * @throws JuDbException If the commit and/or close fails
	 */
	public static void commitAndClose(Connection conn) throws JuDbException {
		try {
			log.debug("Commiting and closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
			DbUtils.commitAndClose(conn);
		} catch (SQLException ex) {
			throw new JuDbException("Couldn't commit and close connection", ex);
		}
	}
	
	/**
	 * Rolls back and closes a connection.
	 * @param conn Connection
	 * @throws JuDbException If the rollback and/or close fails
	 */
	public static void rollbackAndClose(Connection conn) throws JuDbException {
		try {
			log.debug("Rolling back and closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
			DbUtils.rollbackAndClose(conn);
		} catch (SQLException ex) {
			throw new JuDbException("Couldn't roll back and close connection", ex);
		}
	}
	
	/**
	 * Closes the specified connection, catching any SQLException that might be thrown.
	 * @param conn Connection to be closed
	 */
	public static void closeQuietly(Connection conn) {
		try {
			log.debug("Closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
			DbUtils.close(conn);
		} catch (SQLException ex) {
			log.error("Couldn't close connection", ex);
		}
	}
	
	/**
	 * Closes the specified result set, catching any SQLException that might be thrown.
	 * @param rs ResultSet to be closed
	 */
	public static void closeQuietly(ResultSet rs) {
		try {
			DbUtils.close(rs);
		} catch (SQLException ex) {
			log.error("Couldn't close connection", ex);
		}
	}
	
	/**
	 * Converts the specified Clob into a String
	 * @param clob Database Clob
	 * @return String
	 * @throws JuDbException If the conversion fails
	 */
	public static String getClobString(Clob clob) throws JuDbException {
		if (clob == null) return null;
		
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(clob.getCharacterStream());
			String line;
			while ((line = reader.readLine()) != null) sb.append(line);
		} catch (Exception ex) {
			throw new JuDbException("Couldn't convert CLOB to String", ex);
		} finally {
			IOUtil.close(reader);
		}
		
		return sb.toString();
	}

	/**
	 * Gets a list of all table names of the DB. Table names are all upper case.
	 * @return List of Table names
	 * @throws JuDbException If the list cannot be evaluated
	 */
	public List<String> getTableNames() throws JuDbException {
		try {
			@SuppressWarnings("unchecked")
			List<String> tableNames = (List<String>) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
				@Override
				public Object processMetaData(DatabaseMetaData dbmd)
						throws SQLException, MetaDataAccessException {
					
					ResultSet rs = dbmd.getTables(connectionInfo.getSchema(), null, null, new String[]{"TABLE"});
					
					List<String> tableNames = new ArrayList<>();
					while (rs.next()) {
						String tableName = rs.getString("TABLE_NAME").toUpperCase();
						// We check if the TableName already exists in the list as
						// Oracle seems to return the same table names multiple times on some
						// Schemas...
						if (!tableNames.contains(tableName)) {
							tableNames.add(tableName);
						}
					}
					rs.close();
					
					Collections.sort(tableNames);
					
					return tableNames;
				}
			});
			
			return tableNames;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't evaluate table names", ex);
		}
	}
	
	/**
	 * Gets the name of the table's primary key column. Multi-Column primary keys are not supported.
	 * Column names are upper case.
	 * @param tableName Table name
	 * @return Name of the table's primary key
	 * @throws JuDbException If the primary key cannot be evaluated
	 */
	public String getPrimaryColumnName(final String tableName) throws JuDbException {
		try {
			String columnName = (String) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
				@Override
				public Object processMetaData(DatabaseMetaData dbmd)
						throws SQLException, MetaDataAccessException {
					
					ResultSet rs = dbmd.getPrimaryKeys(null, null, tableName.toUpperCase());
					
					String columnName = null;
					if (rs.next()) columnName = rs.getString("COLUMN_NAME");
					else throw new JuDbException("Couldn't evaluate primary key for table " + tableName);
					
					if (rs.next()) {
						throw new JuDbException("Driver returned multiple primary keys for table " + tableName);
					}
					rs.close();
					
					return columnName.toUpperCase();
				}
			});
			
			return columnName;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't evaluate primary column name", ex);
		}
	}
	
	/**
	 * Gets all column names of the specified table in the order they are defined, or rather in the
	 * order the driver returns them. Column names are upper case.
	 * @param tableName Table name
	 * @return Column names of the table
	 * @throws JuDbException If the column names cannot be evaluated
	 */
	public List<String> getColumnNames(final String tableName) throws JuDbException {
		try {
			@SuppressWarnings("unchecked")
			List<String> columnNames = (List<String>) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
				@Override
				public Object processMetaData(DatabaseMetaData dbmd)
						throws SQLException, MetaDataAccessException {
					
					ResultSet rs = dbmd.getColumns(null, null, tableName.toUpperCase(), null);
					
					List<String> columnNames = new ArrayList<>();
					while (rs.next()) {
						String columnName = rs.getString("COLUMN_NAME");
						columnNames.add(columnName.toUpperCase());
					}
					rs.close();
					
					if (columnNames.size() == 0) {
						throw new JuDbException("Couldn't evaluate column names for table " + tableName + ": Driver returned empty ResultSet.");
					}
					
					return columnNames;
				}
			});
			
			return columnNames;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't evaluate primary column name", ex);
		}
	}

	/**
	 * Sets the nextVal of an Oracle sequence.
	 * <p>
	 * Only works with sequences that have an increment of +1.
	 * @param sequenceName Sequence name
	 * @param nextVal Value that should be yielded by next NEXVAL call
	 */
	public void oracleSequenceSetNextVal(String sequenceName, long nextVal) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
		
		Long currentValue = jdbcTemplate.queryForLong(String.format("SELECT %s.NEXTVAL from dual", sequenceName));
		Long increment = nextVal - currentValue - 1;
		jdbcTemplate.execute(String.format("ALTER SEQUENCE %s INCREMENT BY %d", sequenceName, increment));
		jdbcTemplate.execute(String.format("SELECT %s.NEXTVAL from dual", sequenceName));
		jdbcTemplate.execute(String.format("ALTER SEQUENCE %s INCREMENT BY 1", sequenceName));
	}
}
