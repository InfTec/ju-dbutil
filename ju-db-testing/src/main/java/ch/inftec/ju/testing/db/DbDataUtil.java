package ch.inftec.ju.testing.db;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.db.DbWork;
import ch.inftec.ju.db.JuConnUtil;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuConnUtils;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.DataHolder;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.JuUrl;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.XString;
import ch.inftec.ju.util.xml.XPathGetter;
import ch.inftec.ju.util.xml.XmlOutputConverter;
import ch.inftec.ju.util.xml.XmlUtils;

/**
 * Utility class containing methods to import and export data from a DB.
 * <p>
 * The class needs a JuConnUtil Connection wrapper to access the DB.
 * @author Martin
 *
 */
public class DbDataUtil {
	private final static Logger logger = LoggerFactory.getLogger(DbDataUtil.class);
	
	private final JuConnUtil connUtil;
	
	private String schemaName = null;
	
	private Map<String, Object> configProperties = new HashMap<>();

	private IDatabaseConnection dbConn;

	/**
	 * Cache for PrimaryKeyColumns. These are evaluated through MetaData and this results in a
	 * DatabaseMetaData query every time.
	 */
	private final Map<String, String[]> primaryKeyColumns = new HashMap<>();
	
	/**
	 * Creates a DbDataUtil that will use the internal "raw" DB connection
	 * of the EntityManager to perform data export and import.
	 * @param emUtil Wrapper around an EntityManager instance
	 */
	public DbDataUtil(JuEmUtil emUtil) {
		this(emUtil.asConnUtil());
	}
	
	/**
	 * Creates a DbDataUtil that will use the specified "raw" JDBC connection
	 * to perform data export and import.
	 * @param conn JDBC connection
	 */
	public DbDataUtil(Connection conn) {
		this(JuConnUtils.createByConnection(conn));
	}
	
	/**
	 * Creates a new DbDataUtil instance using the specifiec JuConnUtil.
	 * <p>
	 * If you need to specify a DB Schema, use the DbDataUtil(JuConnUtil, String) constructor.
	 * @param connUtil DB Connection instance
	 */
	public DbDataUtil(JuConnUtil connUtil) {
		this(connUtil, (String)null);
	}
	
	/**
	 * Creates a new DbDataUtil instance using the specified Connection and the Schema
	 * from the ConnectionInfo
	 * @param connection Connection instance
	 * @param ConnectionInfo to get the Schema to use
	 * @Deprecated Use constructor with EntityManager
	 */
	public DbDataUtil(JuConnUtil connUtil, ConnectionInfo connectionInfo) {
		this(connUtil, connectionInfo.getSchema());
	}
	
	/**
	 * Executes a DbUnit dataset import through a JDBC connection.
	 * @param conn JDBC connection
	 * @param dataSetUrl DataSet URL
	 * @param cleanInsert If true, a clean insert is performed. Otherwise, an insert is performed.
	 */
	public static void executeInsert(Connection conn, URL dataSetUrl, boolean cleanInsert) {
		ImportBuilder ib = new DbDataUtil(conn).buildImport().from(dataSetUrl);
		if (cleanInsert) {
			ib.executeCleanInsert();
		} else {
			ib.executeInsert();
		}
	}
	
	/**
	 * Create a new DbDataUtil that will use the specified EntityManager to get
	 * a raw connection to the DB and execute SQL queries.
	 * @param em EntityManager instance to execute SQL in a JDBC connection
	 */
	public DbDataUtil(EntityManager em) {
		this(new JuEmUtil(em));
	}
	
	/**
	 * Create a DbDataUtil using the specified connection and Schema name.
	 * @param connUtil DB connection instance
	 * @param schema Explicit Schema name
	 */
	public DbDataUtil(JuConnUtil connUtil, String schema) {
		this.connUtil = connUtil;
		this.schemaName = schema;

		// Initialize
		DefaultDataTypeFactory dataTypeFactory = null;
		IMetadataHandler metadataHandler = new DefaultMetadataHandler();
		switch (this.connUtil.getDbType()) {
		case DERBY:
			dataTypeFactory = new DefaultDataTypeFactory();
			break;
		case H2:
			dataTypeFactory = new H2DataTypeFactory();
			break;
		case HSQL:
			dataTypeFactory = new HsqldbDataTypeFactory();
			break;
		case MYSQL:
			dataTypeFactory = new MySqlDataTypeFactory();
			metadataHandler = new MySqlMetadataHandler();
			break;
		case ORACLE:
			// XXX: Enable for other DBs?
			this.setConfigProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, true);
			
			if (StringUtils.isEmpty(this.schemaName)) {
				this.schemaName = this.connUtil.getMetaDataInfo().getUserName();
			}
			dataTypeFactory = new Oracle10DataTypeFactory();
			break;
		default:
			throw new JuDbException("Unsupported DB: " + this.connUtil.getDbType());
		}
		
		this.setConfigProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory);
		this.setConfigProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, metadataHandler);
	}
	
	/**
	 * Sets the DB schema name to work with.
	 * <p>
	 * May be necessary for DBs like oracle to avoid duplicate name problems.
	 * @param schemaName DB schema name
	 * @return This util to allow for chaining
	 */
	public DbDataUtil setSchema(String schemaName) {
		this.schemaName = schemaName;
		return this;
	}
	
	/**
	 * Sets a config attribute of the underlying DbUnit IDatabaseConnection instance.
	 * @param name Name of the attribute
	 * @param value Value of the attribute
	 * @return This instance to allow for chaining
	 */
	public DbDataUtil setConfigProperty(String name, Object value) {
		this.configProperties.put(name, value);
		return this;
	}
	
	private void execute(final DbUnitWork work) {
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				doExecute(conn, work);
			}
		});
	}
	
	private void doExecute(Connection connection, DbUnitWork work) {
		/**
		 * Due to a JDBC 1.4 spec imcompatibility of the Oracle driver
		 * (doesn't return IS_AUTOINCREMENT in table meta data), we need
		 * to unwrap the actual JDBC connection in case this is a (Hibernate)
		 * proxy.
		 */
		Connection unwrappedConn = null;
		if (this.connUtil.getDbType() == DbType.ORACLE && connection instanceof Proxy) {
			try {
				unwrappedConn = connection.unwrap(Connection.class);
			} catch (Exception ex) {
				throw new JuDbException("Couldn't unwrap Connection", ex);
			}
		}
		final Connection realConn = unwrappedConn != null
				? unwrappedConn
				: connection;
		
		try {
			// Check if we get the same connection. If so, we'll recycle the DatabaseConnection to avoid
			// requerying of all the DB meta data
			if (this.dbConn == null || this.dbConn.getConnection() != connection) {
				this.dbConn = new DatabaseConnection(realConn, this.schemaName);
				
				for (String key : this.configProperties.keySet()) {
					this.dbConn.getConfig().setProperty(key, this.configProperties.get(key));
				}
				logger.debug("Created DatabaseConnection: {}", this.dbConn);
			} else {
				// Use existing DatabaseConnection
				logger.info("Reusing DatabaseConnection {}", this.dbConn.getConnection());
			}
			work.execute(this.dbConn);
		} catch (Exception ex) {
			throw new JuDbException("Couldn't execute DbUnitWork", ex);
		}
	}
	
	/**
	 * Shortcut to execute a clean import from a dataset resource file.
	 * @param resourcePath Path to dataset resource
	 */
	public void cleanImport(String resourcePath) {
		this.buildImport().from(resourcePath).executeCleanInsert();
	}
	
	/**
	 * Returns a new ExportBuilder to configure and execute DB data exports.
	 * @return ExportBuilder instance
	 */
	public ExportBuilder buildExport() {
		return new ExportBuilder(this);
	}
	
	/**
	 * Returns a new ImportBuilder to import data from XML resources into the DB.
	 * @return ImportBuilder instance
	 */
	public ImportBuilder buildImport() {
		return new ImportBuilder(this);
	}
	
	/**
	 * Returns a new AssertBuilder to assert that table data equals expected data
	 * specified in an XML file.
	 * @return AssertBuilder instance
	 */
	public AssertBuilder buildAssert() {
		return new AssertBuilder(this);
	}
	
	/**
	 * Helper callback interface to execute code that needs a IDatabaseConnection
	 * instance.
	 * @author tgdmemae
	 * <T> Return value
	 *
	 */
	private static interface DbUnitWork {
		public void execute(IDatabaseConnection conn);
	}
	
	/**
	 * Builder class to configure and execute DB data exports.
	 * @author Martin
	 *
	 */
	public static class ExportBuilder {
//		private Logger logger = LoggerFactory.getLogger(ExportBuilder.class);
		
		private final DbDataUtil dbDataUtil;
		
		private final ExportItems exportItems = new ExportItems();
		
		private ExportBuilder(DbDataUtil dbDataUtil) {
			this.dbDataUtil = dbDataUtil;	
		}
		
		/**
		 * Loads the table names from the specified dataset XML resource and uses it as a template
		 * of how to case any table name that will be exported.
		 * <p>
		 * Note that calling this method doesn't actually ADD a table.
		 * @param resourcePath Resource path to dataset XML
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder setTableNamesCasingByDataSet(String resourcePath) {
			try {
				this.exportItems.setCasedTableNames(new XPathGetter(XmlUtils.loadXml(
						JuUrl.resource().relativeTo(DbDataUtil.class).get(resourcePath))).getNodeNames("dataset/*"));
			} catch (Exception ex) {
				throw new JuDbException("Couldn't load table names data set " + resourcePath, ex);
			}
			return this;
		}
		
		/**
		 * Adds the specific table to the builder, exporting the table data.
		 * @param tableName Table name
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTable(String tableName) {
			return this.addTable(tableName, null);
		}
		
		private String[] getPrimaryKeyColumns(String tableName) {
			if (!dbDataUtil.primaryKeyColumns.containsKey(tableName)) {
				List<String> primaryKeyColumnsList = this.dbDataUtil.connUtil.getMetaDataInfo().getPrimaryKeyColumns(tableName);
				dbDataUtil.primaryKeyColumns.put(tableName, primaryKeyColumnsList.toArray(new String[0]));
			}
			
			return dbDataUtil.primaryKeyColumns.get(tableName);
		}
		
		/**
		 * Adds the specific table to the builder, exporting the table data.
		 * <p>
		 * The data will be sorted by the tables primary key column.
		 * <p>
		 * Note: This works only if the DbDataUtil was initialized with an emUtil instance.
		 * @param tableName Table name
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTableSorted(String tableName) {
			AssertUtil.assertNotNull(
					"Sorting by primary key only works with DbDataUtils that were initialized with an JuConnUtil instance"
					, this.dbDataUtil.connUtil);
			
			return this.addTableSorted(tableName, this.getPrimaryKeyColumns(tableName));
		}
		
		/**
		 * Adds the specified table to the builder, exporting the table data.
		 * <p>
		 * If no query is specified (null), all table data is exported. Otherwise, only
		 * the data returned by the query is exported.
		 * <p>
		 * The query has to be a full SQL query like <code>select * from table where id=7</code>
		 * @param tableName TableName
		 * @param query Optional query to select sub data
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTable(final String tableName, final String query) {
			this.exportItems.add(tableName, query);
			return this;
		}
		
		/**
		 * Adds the data of the specified table, ordering by the specified columns.
		 * @param tableName Table names
		 * @param orderColumns List of columns to order by
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTableSorted(String tableName, String... orderColumns) {
			if (orderColumns.length == 0) {
				return this.addTable(tableName);
			} else {
				XString xs = new XString();
				xs.addFormatted("SELECT * FROM %s ORDER BY ", tableName);
				for (String orderColumn : orderColumns) {
					xs.assertText("ORDER BY ", ", ");
					xs.addText(orderColumn);
				}
				
				return this.addTable(tableName, xs.toString());
			}
		}
		
		/**
		 * Adds the data of the tables contained in the specified data set.
		 * <p>
		 * It doesn't matter what kind of dataset we got, we're just extracting the table names.
		 * @param resourceUrl
		 * @param sortedByPrimaryKey If true, the entries will be sorted by primary key
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTablesByDataSet(URL resourceUrl, boolean sortedByPrimaryKey) {
			try {
				Set<String> tableNames = JuCollectionUtils.asSameOrderSet(new XPathGetter(XmlUtils.loadXml(resourceUrl)).getNodeNames("dataset/*"));
				for (String tableName : tableNames) {
					if (sortedByPrimaryKey) {
						this.addTableSorted(tableName);
					} else {
						this.addTable(tableName);
					}
				}
				
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't add tables by dataset " + resourceUrl, ex);
			}
		}
		
		private void doWork(final DataSetWork dataSetWork) {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					dataSetWork.execute(createDataSet(conn));
				};
			});
		}
		
		private IDataSet createDataSet(IDatabaseConnection conn) {
			IDataSet dataSet = exportItems.createDataSet(conn);
			return dataSet;
		}
		
		/**
		 * Writes the DB data to an XML Output Converter that can convert or write it to different
		 * output formats without having to re-execute the whole DbUnit export.
		 * @return
		 */
		public XmlOutputConverter writeToXml() {
			final DataHolder<XmlOutputConverter> doc = new DataHolder<>();
			
			this.doWork(new DataSetWork() {
				@Override
				public void execute(IDataSet dataSet) {
					try {
						XmlOutputConverter xmlConv = new XmlOutputConverter();
						ExportBuilder.writeToXml(dataSet, xmlConv.getOutputStream());
						doc.setValue(xmlConv);
					} catch (Exception ex) {
						throw new JuDbException("Couldn't write DB data to XML Output Converter", ex);
					}
				}
			});
			
			return doc.getValue();
		}
		
		/**
		 * Writes the DB data to an (in-memory) XML Document.
		 * @return Xml Document instance
		 */
		public Document writeToXmlDocument() {
			return this.writeToXml().getDocument();
		}
		
		/**
		 * Writes the DB data to a String.
		 * @return Xml Document instance
		 */
		public String writeToXmlString() {
			return this.writeToXml().getXmlString();
		}
		
		/**
		 * Write the DB data to an XML file.
		 * <p>
		 * File name will be resolved relatively to the current working directory.
		 * @param fileName File name
		 * @deprecated Use writeToXmlFile(Path) instead
		 */
		@Deprecated
		public void writeToXmlFile(String fileName) {
			this.writeToXmlFile(Paths.get(fileName));
		}
		
		/**
		 * Write the DB data to an XML file.
		 * @param file Path to the file
		 */
		public void writeToXmlFile(final Path file) {
			this.writeToXml().writeToXmlFile(file);
		}
		
		private interface DataSetWork {
			void execute(IDataSet dataSet);
		}
		
		/**
		 * Custom implementation of FlatXmlDataSet.write so we can enforce column casing
		 * @param dataSet
		 * @param out
		 * @throws IOException
		 * @throws DataSetException
		 */
		private static void writeToXml(IDataSet dataSet, OutputStream out) throws IOException, DataSetException {
			CaseAwareFlatXmlWriter datasetWriter = new CaseAwareFlatXmlWriter(out, "utf-8");
	        datasetWriter.setIncludeEmptyTable(true);
	        datasetWriter.write(dataSet);
		}
	}
	
	/**
	 * Builder class to configure and execute DB data imports.
	 * @author Martin
	 *
	 */
	public static class ImportBuilder {
		private Logger logger = LoggerFactory.getLogger(ImportBuilder.class);
		
		private final DbDataUtil dbDataUtil;
		private FlatXmlDataSet flatXmlDataSet;
		private URL dataSetUrl;
		private boolean automatedTableOrder = false;
		
		private ImportBuilder(DbDataUtil dbDataUtil) {
			this.dbDataUtil = dbDataUtil;	
		}
		
		/**
		 * If true, Import tries to order tables in change set automatically according to
		 * foreign key constraints.
		 * <p>
		 * Default value is false.
		 * @param automatedTableOrder
		 * @return
		 */
		public ImportBuilder automatedTableOrder(boolean automatedTableOrder) {
			this.automatedTableOrder = automatedTableOrder;
			return this;
		}
		
		/**
		 * Imports DB data from the specified XML.
		 * <p>
		 * Only one 'from' is possible per import.
		 * @param resourcePath Resource path, either absolute or relative to the current class
		 * @return ImportBuilder
		 */
		public ImportBuilder from(String resourcePath) {
			URL url = JuUrl.resource().relativeTo(ReflectUtils.getCallingClass()).get(resourcePath);
			return from(url);
		}
		
		/**
		 * Imports DB data from the specified XML
		 * @param xmlUrl URL to XML file location
		 */
		public ImportBuilder from(URL xmlUrl) {
			try {
				flatXmlDataSet = new FlatXmlDataSetBuilder()
					.setColumnSensing(true)
					.setCaseSensitiveTableNames(false)
					.build(xmlUrl);
				this.dataSetUrl = xmlUrl;
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: " + xmlUrl, ex);
			}
		}
		
		/**
		 * Performs a clean import of the data into the DB, i.e. cleans any existing
		 * data in affected tables and imports the rows specified in in this builder.
		 */
		public void executeCleanInsert() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						logger.debug("Executing Clean-Insert from: " + dataSetUrl);
						DatabaseOperation.CLEAN_INSERT.execute(conn, getDataSet(conn));
					} catch (Exception ex) {
						throw new JuDbException("Couldn't clean and insert data into DB", ex);
					}
				}
			});
		}
		
		private IDataSet getDataSet(IDatabaseConnection conn) throws Exception {
			IDataSet ds = flatXmlDataSet;
			
			// If automated table order is enabled, decorate the table set with a database sequence filter
			if (this.automatedTableOrder) {
				ds = new FilteredDataSet(new DatabaseSequenceFilter(conn), flatXmlDataSet);
			}
			
			return ds;
		}
		
		/**
		 * Truncates all tables included in the data set.
		 */
		public void executeDeleteAll() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						logger.debug("Executing Delete-All from: " + dataSetUrl);
						DatabaseOperation.DELETE_ALL.execute(conn, getDataSet(conn));
					} catch (Exception ex) {
						throw new JuDbException("Couldnt truncate data in DB", ex);
					}
				};
			});
		}
		
		/**
		 * Performs an import of the data into the DB, without cleaning any data
		 * previously.
		 */
		public void executeInsert() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						logger.debug("Executing Insert from: " + dataSetUrl);
						DatabaseOperation.INSERT.execute(conn, getDataSet(conn));
					} catch (Exception ex) {
						throw new JuDbException("Couldnt insert data into DB", ex);
					}
				};
			});
			
		}
		
		/**
		 * Performs an update of the existing data in the DB, without inserting new data.
		 */
		public void executeUpdate() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						logger.debug("Executing Update from: " + dataSetUrl);
						DatabaseOperation.UPDATE.execute(conn, getDataSet(conn));
					} catch (Exception ex) {
						throw new JuDbException("Couldnt update data in DB", ex);
					}
				};
			});
			
		}
	}	
	
	/**
	 * Builder class to configure and execute DB data asserts.
	 * @author Martin
	 *
	 */
	public static class AssertBuilder {
		private final DbDataUtil dbDataUtil;
		private FlatXmlDataSet flatXmlDataSet;
		private URL dataSetUrl;
		
		private AssertBuilder(DbDataUtil dbDataUtil) {
			this.dbDataUtil = dbDataUtil;	
		}
		
		/**
		 * Path to XML of expected data.
		 * @param resourcePath Resource path, either absolute or relative to the current class
		 * @return AssertBuilder
		 */
		public AssertBuilder expected(String resourcePath) {
			URL url = JuUrl.resource().relativeTo(ReflectUtils.getCallingClass()).get(resourcePath);
			return expected(url);
		}
		
		/**
		 * URL to XML of expected data.
		 * @param xmlUrl URL to XML file location
		 * @return This builder to allow for chaining
		 */
		public AssertBuilder expected(URL xmlUrl) {
			try {
				this.dataSetUrl = xmlUrl;
				flatXmlDataSet = new FlatXmlDataSetBuilder().build(xmlUrl);
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: xmlUrl", ex);
			}
		}
		
		/**
		 * Asserts that the data exported based on the result data set (i.e. all tables contained in the dataset, sorted
		 * by primary key) equals the result data set.
		 */
		public void assertEquals() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						IDataSet dbDataSet = dbDataUtil.buildExport()
							.addTablesByDataSet(dataSetUrl, true)
							.createDataSet(conn);
						
						Assertion.assertEquals(flatXmlDataSet, dbDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldn't assert DB data", ex);
					}
				}
				
			});
		}
		
		/**
		 * Asserts that the whole data set in the DB equals the expected data.
		 * TODO: Add functionality to exclude (system) tables
		 */
		public void assertEqualsAll() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						IDataSet  dbDataSet = conn.createDataSet();
						Assertion.assertEquals(flatXmlDataSet, dbDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldn't assert DB data", ex);
					}
				}
			});
		}
		
		/**
		 * Asserts that the export from the specified table equals the expected data.
		 * @param tableName Name of the table to assert
		 * @param orderColumnName Name of the column to order data by for the export
		 */
		public void assertEqualsTable(final String tableName, final String orderColumnName) {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						QueryDataSet tableDataSet = new QueryDataSet(conn);
						tableDataSet.addTable(tableName, String.format("select * from %s order by %s", tableName, orderColumnName));
						
						Assertion.assertEquals(flatXmlDataSet, tableDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldn't assert DB data", ex);
					}
				}
			});
		}
	}
}
