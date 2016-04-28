package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to export (and verify) test data after the test has succeeded. When verifying, the resource
 * must be present on the classpath for comparison.
 * <p>
 * The data is exported as soon as the test succeeds, before any verifiers are run.
 * <p>
 * By default, the data set is exported to <code>src/main/resources/dataSetExport</code> to a name
 * like <code>TestClassName_testMethodName.xml</code>. Any leading non-alphabetically characters
 * will be stripped, e.g. _01_test -> test
 * <p>
 * The target location can be modified using the <code>targetDir</code> attribute.
 * <p>
 * No export will be performed if the property <code>ju-testing.export.compareToResource</code> is true.
 * In this case, the export will be done in-memory and compared to a resource on the classpath, using
 * the <code>resourcePrefix</code> attribute to resolve it.
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataSetExport {
	/**
	 * Name of the data set that contains the tables we should export.
	 * @return Path to the export data set XML file resource 
	 */
	String tablesDataSet();

	/**
	 * Name of the dataset export.
	 * <p>
	 * Defaults to empty string which means that the name is automatically computed by the class and
	 * method name.
	 * @return export name
	 */
	String exportName() default "";
	
	/**
	 * Type of the export. Defaults to PHYSICAL.
	 * <ul>
	 *   <li>PHYSICAL: The DataSet is exported to a file</li>
	 *   <li>MEMORY: The DataSet is logged and kept in memory so it can be used as an XmlDocument in a DataVerifier.</li>
	 *   <li>NONE: The DataSet is not exported at all. Can be used to override an intherited DataSetExport annotation</li>
	 * </ul>
	 * If physical export is disabled, there will be done no resource verification either.
	 * @return Type of DataSet export to be performed
	 */
	ExportType exportType() default ExportType.PHYSICAL;
	
	/**
	 * Type of DataSet export.
	 * @author Martin Meyer <martin.meyer@inftec.ch>
	 *
	 */
	public enum ExportType {
		/**
		 * Exports the DataSet to a file.
		 */
		PHYSICAL,
		
		/**
		 * Only exports the DataSet to the memory so it can be used as an XmlDocument in a DataVerifier.
		 */
		MEMORY,
		
		/**
		 * Doesn't perform any export. Can be used to override an inherited DataSetExport annotation.
		 */
		NONE;
	}
}