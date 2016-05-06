package ch.inftec.ju.testing.db;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.testing.db.DataSetExport.ExportType;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuObjectUtils;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.JuUrl;
import ch.inftec.ju.util.JuUtils;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.ReflectUtils.AnnotationInfo;
import ch.inftec.ju.util.SystemPropertyTempSetter;
import ch.inftec.ju.util.XString;
import ch.inftec.ju.util.xml.XmlOutputConverter;
import ch.inftec.ju.util.xml.XmlUtils;

/**
 * Helper class to handle test annotations like @DataSet and @DataVerify.
 * <p>
 * When calling the execute... methods, the client is responsible that a valid transaction is present.
 * @author Martin
 *
 */
public class DbTestAnnotationHandler implements Serializable {
	private final static Logger logger = LoggerFactory.getLogger(DbTestAnnotationHandler.class);
	
	private final DbTestAnnotationInfo annoInfo;
	private final DataSetConfigInfo dataSetConfigInfo;

	protected final String testClassName;
	protected final String testMethodName;
	
	/**
	 * Readable name of the test method, may be testMethod[0] for parameterized tests.
	 */
	private final String testMethodReadableName;
	
	/**
	 * Helper class so we can use the annotation info in related classes like DataSetExportSuite.
	 * 
	 * @author martin.meyer@inftec.ch
	 * 
	 */
	static class DbTestAnnotationInfo implements Serializable {
		private final List<AnnotationInfo<DbDataUtilConfig>> dbDataUtilConfigAnnos;
		private final List<AnnotationInfo<JuTestEnv>> testEnvAnnos;
		private final List<AnnotationInfo<DataSet>> dataSetAnnos;
		private final List<AnnotationInfo<DataSetExport>> dataSetExportAnnos;
		private final List<AnnotationInfo<PostServerCode>> postServerCodeAnnos;
		private final List<AnnotationInfo<DataVerify>> dataVerifyAnnos;
		private final List<AnnotationInfo<DataSetConfig>> dataSetConfigAnnos;

		private DbTestAnnotationInfo(Method method) {
			this.dbDataUtilConfigAnnos = ReflectUtils.getAnnotationsWithInfo(method.getDeclaringClass(), DbDataUtilConfig.class, true);

			// Get all annotations for the method and declaring class (exlucing overridden methods)
			// in reverse order, i.e. starting from class to method
			this.testEnvAnnos = ReflectUtils.getAnnotationsWithInfo(method, JuTestEnv.class, false, true, true);
			Collections.reverse(this.testEnvAnnos);

			// Get all annotations for the method and the declaring class (including super classes, but
			// excluding overridden methods)
			this.dataSetAnnos = ReflectUtils.getAnnotationsWithInfo(method, DataSet.class, false, true, true);
			// Reverse the list as we want to start with the base class, then class and method last
			Collections.reverse(this.dataSetAnnos);

			this.dataSetExportAnnos = ReflectUtils.getAnnotationsWithInfo(method, DataSetExport.class, true, true, true);
			this.postServerCodeAnnos = ReflectUtils.getAnnotationsWithInfo(method, PostServerCode.class, true, false, false);
			this.dataVerifyAnnos = ReflectUtils.getAnnotationsWithInfo(method, DataVerify.class, true, false, false);

			this.dataSetConfigAnnos = ReflectUtils.getAnnotationsWithInfo(method, DataSetConfig.class, true, true, true);
		}

		public List<AnnotationInfo<DataSet>> getDataSetAnnos() {
			return dataSetAnnos;
		}

		public List<AnnotationInfo<DataSetExport>> getDataSetExportAnnos() {
			return dataSetExportAnnos;
		}

		@DataSetConfig
		private static class DataSetConfigAnnotationUtil implements Serializable {
		}
	}

	static class DataSetConfigInfo implements Serializable {
		private String resourceDir;
		private String resourcePrefix;
		private boolean loadImportResourcesFromFileSystem;

		/**
		 * Gets the resource directory without any resource prefix, e.g. <code>src/main/resource<code>
		 * 
		 * @return
		 */
		public String getResourceDirectory() {
			return this.resourceDir;
		}

		/**
		 * Gets the resource directory with the resource prefix appended, e.g. <code>src/main/resource/dataSetExport</code>
		 * 
		 * @return
		 */
		public String getResourceDirectoryFull() {
			return Paths.get(this.resourceDir, this.resourcePrefix).toString();
		}

		/**
		 * Gets the resource prefix, e.g. <code>dataSetExport</code>
		 * 
		 * @return
		 */
		public String getResourcePrefix() {
			return resourcePrefix;
		}

		/**
		 * If true, we support loading of import resources from file system. If false, we only load from the classpath.
		 * 
		 * @return
		 */
		public boolean isLoadImportResourcesFromFileSystem() {
			return loadImportResourcesFromFileSystem;
		}
	}

	/**
	 * Gets a DbTestAnnotationInfo object for the specified test method.
	 * 
	 * @param testMethod
	 *            Test method
	 * @return DbTestAnnotationInfo object
	 */
	static DbTestAnnotationInfo getDbTestAnnotationInfo(Method testMethod) {
		return new DbTestAnnotationInfo(testMethod);
	}

	static DataSetConfigInfo getDataSetConfigInfo(DbTestAnnotationInfo annoInfo) {
		DataSetConfigInfo info = new DataSetConfigInfo();

		// Check if we have a DataSetConfig annotation
		if (annoInfo.dataSetConfigAnnos.size() > 0) {
			DataSetConfig configAnno = annoInfo.dataSetConfigAnnos.get(0).getAnnotation();
			info.resourceDir = configAnno.resourceDir();
			info.resourcePrefix = configAnno.resourcePrefix();
			info.loadImportResourcesFromFileSystem = configAnno.loadImportResourcesFromFileSystem();
		} else {
			// No DataSetConfig annotation, so use default values
			try {
				info.resourceDir = (String) DataSetConfig.class.getMethod("resourceDir").getDefaultValue();
				info.resourcePrefix = (String) DataSetConfig.class.getMethod("resourcePrefix").getDefaultValue();
				info.loadImportResourcesFromFileSystem = (Boolean) DataSetConfig.class.getMethod("loadImportResourcesFromFileSystem")
						.getDefaultValue();
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't get default values for DataSetConfig annotation", ex);
			}
		}

		return info;
	}

	public DbTestAnnotationHandler(Method method, Description description) {
		this.annoInfo = getDbTestAnnotationInfo(method);
		this.dataSetConfigInfo = getDataSetConfigInfo(this.annoInfo);
		
		this.testClassName = method.getDeclaringClass().getName();
		this.testMethodName = method.getName();
		this.testMethodReadableName = description.getMethodName();
	}
	
	private Class<?> getTestClass() {
		try {
			return Class.forName(this.testClassName);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't get test class. Make sure it's on the classpath: " + this.testClassName);
		}
	}
	
	
//	private Method getTestMethod() {
//		return ReflectUtils.getMethod(this.getTestClass(), this.testMethodName, new Class<?>[0]);
//	}
	
	/**
	 * Initializes the environment for the test case.
	 * @return A SystemPropertyTempSetter that can be used to restore the original system property state
	 */
	public SystemPropertyTempSetter initContainerTestEnv() {
		return DbTestAnnotationHandler.setTestEnvProperties(this.annoInfo.testEnvAnnos);
	}
	
	/**
	 * Sets the system properties as configured in the JuTestEnv annotations provided and returns
	 * a SystemPropertyTempSetter that can be used to reset the properties.
	 * @param testEnvAnnos List of JuTestEnv annotations containing systemProperties settings
	 * @return SystemPropertyTempSetter that must be closed as soon as the test context is left
	 */
	public static SystemPropertyTempSetter setTestEnvProperties(List<AnnotationInfo<JuTestEnv>> testEnvAnnos) {
		@SuppressWarnings("resource") // We'll close it in another method...
		SystemPropertyTempSetter tempSetter = new SystemPropertyTempSetter();
		
		try {
			for (AnnotationInfo<JuTestEnv> testEnvInfo : testEnvAnnos) {
				logger.debug("Processing Annotation (Setting system property): {}", testEnvInfo);
				
				for (String keyValStr : testEnvInfo.getAnnotation().systemProperties()) {
					if (!StringUtils.isEmpty(keyValStr) && keyValStr.contains("=")) {
						int ind = keyValStr.indexOf("=");
						String key = keyValStr.substring(0, ind);
						String val = keyValStr.length() > ind - 1
								? keyValStr.substring(ind + 1)
								: "";
						
						logger.debug("Setting system property for test context: {}={}", key, val);
						tempSetter.setProperty(key, val);
					} else {
						throw new JuRuntimeException("SystemProperty String must be of type key=val, but was: %s", keyValStr);
					}
				}
			}
		} catch (Exception ex) {
			// When an exception occurrs, make sure we reset the properties to their original values
			tempSetter.close();
			throw ex;
		}
		
		return tempSetter;
	}
	
	private DbDataUtil getDbDataUtil(JuEmUtil emUtil) {
		if (this.annoInfo.dbDataUtilConfigAnnos.size() > 0) {
			AnnotationInfo<DbDataUtilConfig> annoInfo = this.annoInfo.dbDataUtilConfigAnnos.get(0);
			Class<? extends DbDataUtilProvider> providerClass = annoInfo.getAnnotation().value();
			
			logger.debug("Retrieving DbDataUtil from provider {} (defined in Annotation {}", providerClass, annoInfo);
			
			DbDataUtilProvider provider = ReflectUtils.newInstance(providerClass, false);
			DbDataUtil dbDataUtil = provider.getDbDataUtil();
			if (dbDataUtil == null) {
				logger.warn("DbDataUtilProvider.getDbDataUtil() returned null. Creating DbDataUtil using JuEmUtil.");
			} else {
				return provider.getDbDataUtil();
			}
		}
		
		return new DbDataUtil(emUtil);
	}
	
	public final void executePreTestAnnotations(JuEmUtil emUtil) throws Exception {
		// Load test data as defined by annotations

		DbDataUtil du = this.getDbDataUtil(emUtil);
		Integer sequenceValue = null;
		for (AnnotationInfo<DataSet> dataSetInfo : this.annoInfo.dataSetAnnos) {
			logger.debug("Processing Annotation (Loading Data Sets): {}", dataSetInfo);
			
			// Run pre initializer
			this.runInitializer(dataSetInfo.getAnnotation().preInitializer(), emUtil.getEm());
			
			if (DataSet.NO_CLEAN_INSERT.equals(dataSetInfo.getAnnotation().value())) {
				// Skip clean-insert				
			} else {
				// Perform clean-insert of value resource
				URL resourceUrl = this
						.resourceToUrl(dataSetInfo.getAnnotation().value(), this.dataSetConfigInfo.getResourceDirectory());
				du.buildImport()
					.from(resourceUrl)
					.executeCleanInsert();
			}
			
			// Perform inserts for inserts resources
			for (String insertResource : dataSetInfo.getAnnotation().inserts()) {
				URL resourceUrl = this.resourceToUrl(insertResource, this.dataSetConfigInfo.getResourceDirectory());
				du.buildImport()
					.from(resourceUrl)
					.executeInsert();
			}
			
			sequenceValue = dataSetInfo.getAnnotation().sequenceValue();
			
			// Run post initializer
			this.runInitializer(dataSetInfo.getAnnotation().postInitializer(), emUtil.getEm());
		}

		// Reset the sequences
		if (sequenceValue != null) {
			emUtil.resetIdentityGenerationOrSequences(sequenceValue);
		}
	}
	
	private void runInitializer(Class<? extends ServerCode> clazz, EntityManager em) throws Exception {
		ServerCode initializer = ReflectUtils.newInstance(clazz, false);
		initializer.init(em);
		initializer.execute();		
	}

	/**
	 * Converts a resourceUrl string to an URL. This also performs paramterized placeholder replacement
	 * if necessary.
	 * @param resource Resource path
	 * @param resourceDir Resource directory in case we need to lookup the resource in the file system
	 * @return Actual resource URL
	 * @throws JuRuntimeException If the resource is not valid
	 */
	private URL resourceToUrl(String resource, String resourceDir) {
		String actualResource = resource;
		// Perform {param} placeholder replacement
		if (resource.indexOf(DataSet.PARAM_POSTFIX) > 0) {
			String parameterizedTestName = this.getParameterizedTestName();
			AssertUtil.assertNotNull("Doesn't seem to be parameterized test: " + this.testMethodReadableName, parameterizedTestName);
			
			actualResource = actualResource.replace(DataSet.PARAM_POSTFIX, "[" + parameterizedTestName + "]");
		}
		
		URL url = null;
		if (!JuUtils.getJuPropertyChain().get("ju-testing.export.compareToResource", Boolean.class)
				&& this.dataSetConfigInfo.isLoadImportResourcesFromFileSystem() && !StringUtils.isEmpty(resourceDir)) {
			// Lookup resource in file system
			Path p = Paths.get(this.getLocalRoot(), resourceDir, actualResource);
			url = JuUrl.toUrl(p);
		} else {
			// Lookup resource as (classpath) resource
			url = JuUrl.resource().relativeTo(this.getTestClass()).get(actualResource);
			if (url == null) url = JuUrl.resource(actualResource);
		} 
		
		if (url == null) {
			throw new JuRuntimeException(String.format("Couldn't find resource %s, relative to class %s"
					, actualResource
					, this.getTestClass()));
		}
		
		return url;
	}

	/**
	 * Gets the local root directory used to resolve resource locations.
	 * <p>
	 * Can be overridden by extending classes to provide a different root.
	 * @return Root location for resource lookup on the filesystem
	 */
	protected String getLocalRoot() {
		return ".";
	}
	
	/**
	 * Get the name of the parameterized test.
	 * @return Parameterized test name or null if the test is not parameterized.
	 */
	private String getParameterizedTestName() {
		if (this.testMethodReadableName.indexOf("[") < 0 || !this.testMethodReadableName.endsWith("]")) {
			return null;
		} else {
			return this.testMethodReadableName.substring(this.testMethodReadableName.indexOf("[") + 1
					, this.testMethodReadableName.length() - 1);
		}
	}
	
//	/**
//	 * Extending classes can override this method to perform initialization on the
//	 * test class before the test method is invoked.
//	 * @param instance
//	 */
//	protected void initTestClass(Object instance) {
//	}
	
	public final void executePostServerCode(JuEmUtil emUtil) throws Exception {
		// Execute post server code
		for (AnnotationInfo<PostServerCode> codeInfo : this.annoInfo.postServerCodeAnnos) {
			logger.debug("Processing Annotation (Executing Post Server Code): {}", codeInfo);
			
			Class<?> codeClass = null;
			if (codeInfo.getAnnotation().value() == PostServerCode.DEFAULT_SERVER_CODE.class) {
				String verifierName = StringUtils.capitalize(this.testMethodName + "_code");
				Class<?> defaultVerifier = ReflectUtils.getInnerClass(this.getTestClass(), verifierName);
				AssertUtil.assertNotNull(String.format("Couldn't find Verifier %s as inner class of %s. Make sure it exists and is public static."
						, verifierName, this.getTestClass())
						, defaultVerifier);

				codeClass = defaultVerifier;
			} else {
				codeClass = codeInfo.getAnnotation().value();
			}

			this.runServerCode(codeClass, emUtil.getEm());
		}
	}
	
	/**
	 * Gets the target file name for the specified DataSetExport.
	 * <p>
	 * This will either take the value of exportName specified in the annotation or compute a generic value based on the class and method
	 * name.
	 * 
	 * @param dataSetExport
	 * @return Export file name of the DataSetExport file
	 */
	String getExportFileName(DataSetExport dataSetExport) {
		String targetFileName = dataSetExport.exportName();
		if (StringUtils.isEmpty(targetFileName)) {
			// Construct name using class and method name
			targetFileName = String.format("%s_%s.xml"
					, this.getTestClass().getSimpleName()
					, JuStringUtils.removeNonAlphabeticalLeadingCharacters(this.testMethodReadableName));
		}

		return targetFileName;
	}

	public final void executePostTestAnnotations(JuEmUtil emUtil) throws Exception {
		// Process DataSetExport annotation. We'll just consider the first annotation.
		Document doc = null;
		if (this.annoInfo.dataSetExportAnnos.size() > 0) {
			logger.debug("Processing Annotation (Exporting Data Set): {}", this.annoInfo.dataSetExportAnnos.get(0));
			DataSetExport dataSetExport = this.annoInfo.dataSetExportAnnos.get(0).getAnnotation();
			
			if (dataSetExport.exportType() != ExportType.NONE) {
				// Get file name
				String targetFileName = getExportFileName(dataSetExport);
				
				URL tablesDataSestUrl = JuUrl.resource().relativeTo(this.getTestClass()).get(dataSetExport.tablesDataSet());
				if (tablesDataSestUrl == null) tablesDataSestUrl = JuUrl.resource(dataSetExport.tablesDataSet());
				
				XmlOutputConverter xmlOutput = this.getDbDataUtil(emUtil).buildExport()
						.addTablesByDataSet(tablesDataSestUrl, true)
						.writeToXml();
				
				doc = xmlOutput.getDocument();
				
				if (dataSetExport.exportType() == ExportType.PHYSICAL) {
					if (JuUtils.getJuPropertyChain().get("ju-testing.export.compareToResource", Boolean.class, true)) {
						// Perform export in-memory and compare to resource
						String resourcePrefix = this.dataSetConfigInfo.getResourcePrefix();
						String resourcePath = resourcePrefix + "/" + targetFileName;
						URL resourceUrl = JuUrl.singleResource(resourcePath);
						String resourceString = new IOUtil().loadTextFromUrl(resourceUrl);
						
						String xmlString = xmlOutput.getXmlString();
						
						logger.debug("Comparing DB export to resource {}", resourceUrl);
						Assert.assertEquals(resourceString, xmlString);
					} else {
						// Perform export to file
						String targetDirName = this.dataSetConfigInfo.getResourceDirectoryFull();
						// Create target directory
						Path targetDirPath = Paths.get(this.getLocalRoot(), targetDirName);
						Files.createDirectories(targetDirPath);
						
						// Build file path
						Path targetFilePath = targetDirPath.resolve(targetFileName);
						xmlOutput.writeToXmlFile(targetFilePath);
					}
				} else if (dataSetExport.exportType() == ExportType.MEMORY) {
					// Log XML
					if (logger.isInfoEnabled()) {
						XString xs = new XString(targetFileName);
						xs.newLine();
						xs.addLine(XmlUtils.toString(doc, true, true));
						logger.info(xs.toString());
					}
				} else {
					// Shouldn't happen
					throw new IllegalArgumentException("Unsupported export type: " + dataSetExport.exportType());
				}
			}
			
			if (this.annoInfo.dataSetExportAnnos.size() > 1) {
				logger.debug("Ignoring DataSetExport annotations as only first is processed:");
				for (int i = 1; i < this.annoInfo.dataSetExportAnnos.size(); i++) {
					logger.debug("Ignoring Annotation: {}", this.annoInfo.dataSetExportAnnos.get(i));
				}
			}
		}
		
		// Run data verifiers (provided the test method and data set export has succeeded)
		List<DataVerifier> verifiers = new ArrayList<DataVerifier>();
		
		// Check for programmatic verifiers
		for (AnnotationInfo<DataVerify> verifyInfo : this.annoInfo.dataVerifyAnnos) {
			logger.debug("Processing Annotation (Data Verifying): {}", verifyInfo);
			
			Class<?> verifierClass = null;
			if (verifyInfo.getAnnotation().value() == DataVerify.DEFAULT_DATA_VERIFIER.class) {
				String verifierName = StringUtils.capitalize(JuStringUtils.removeNonAlphabeticalLeadingCharacters(this.testMethodName));
				Class<?> defaultVerifier = ReflectUtils.getInnerClass(this.getTestClass(), verifierName);
				AssertUtil.assertNotNull(
						String.format("Couldn't find Verifier %s as inner class of %s. Make sure it exists and is public static."
						, verifierName, this.getTestClass())
						, defaultVerifier);
				
				verifierClass = defaultVerifier;
			} else {
				verifierClass = verifyInfo.getAnnotation().value();
			}
			
			verifiers.add(this.createVerifier(verifierClass, emUtil.getEm(), doc));
		}
		
		// Run verifiers
		for (DataVerifier verifier : verifiers) {
			verifier.verify();
		}
	}
	
	private void runServerCode(Class<?> codeClass, EntityManager em) throws Exception {
		AssertUtil.assertTrue("Code class must be of type ServerCode: " + codeClass.getName(), ServerCode.class.isAssignableFrom(codeClass));
		
		ServerCode code = (ServerCode) ReflectUtils.newInstance(codeClass, false);
		code.init(em);
		
		try {
			code.execute();
		} catch (Exception ex) {
			this.handleServerThrowable(ex);
		}
	}
	
	/**
	 * Handle Server throwables to make sure we can send them to the client.
	 * @param t Throwable
	 * @throws T Handled throwable (may be the same or a converted Throwable)
	 */
	protected final <T extends Throwable> void handleServerThrowable(T t) throws T {
		// Handle non-serializable exceptions
		if (!IOUtil.isSerializable(t)) {
			// If we have an assumption failure wrapped in an InvocationTargetException, rethrow a new
			// AssumptionViolatedException that just containing the message
			InvocationTargetException ite = JuObjectUtils.as(t, InvocationTargetException.class);
			if (ite != null && ite.getTargetException() instanceof AssumptionViolatedException) {
				throw new AssumptionViolatedException(ite.getTargetException().getMessage());
			}

			
			XString causes = new XString("%s (Original Exception %s not serializable. Resolving chain"
					, t.getMessage()
					, t.getClass().getName());
					
			Throwable tChain = t;
			// Use cause (if possible / serializable)
			if (ite != null) {
				causes.addLineFormatted("%s [Target: %s]", ite.getMessage(), ite.getTargetException());
				if (ite.getTargetException() != null) {
					tChain = ite.getTargetException();
				}
			}
			
			Throwable cause = tChain.getCause();
			while (cause != null) {
				if (!IOUtil.isSerializable(cause)) {
										
					causes.addLineFormatted("%s [Caused by: %s (non-serializable)", cause.getMessage(), cause.getClass().getName());
					cause = cause.getCause();
				} else {
					break;
				}
			}
			
			if (cause != null) {
				throw new JuRuntimeException(causes.toString(), cause);
			} else {
				causes.addLine("Check Server log for more details");
				throw new JuRuntimeException(causes.toString());
			}
		} else {
			throw t;
		}
	}
	
	private DataVerifier createVerifier(Class<?> verifierClass, EntityManager em, Document doc) {
		AssertUtil.assertTrue("Verifier must be of type DataVerifier: " + verifierClass.getName(), DataVerifier.class.isAssignableFrom(verifierClass));
		
		DataVerifier verifier = (DataVerifier) ReflectUtils.newInstance(verifierClass, false);
		verifier.init(em, doc);
		this.initVerifier(verifier);

		return verifier;
	}
	
	/**
	 * Extending classes can override this method to perform additional initialization on the DataVerifier.
	 * @param verifier DataVerifier
	 */
	protected void initVerifier(DataVerifier verifier) {
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s()", this.testClassName, this.testMethodReadableName);
	}
}
