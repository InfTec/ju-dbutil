package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure DataSet exporting and importing.
 * <p>
 * This mainly includes the configuration of paths and prefixes to locate datasets.
 * <p>
 * The DataSetConfig is inherited by class extension and can be overridden on a method. Only the most relevant config will be used, starting
 * from method going up to class and super classes.
 * <p>
 * If no DataSetConfig is specified, no comparison to resources on the file system will be performed, even if the
 * <code>ju-testing.export.compareToResource</code> is false.
 * 
 * @author Martin
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataSetConfig {
	/**
	 * Target directory to copy resources to.
	 * <p>
	 * Defaults to <code>src/main/resources</code>
	 * <p>
	 * Note that the resourcePrefix will be added to this directory in order to perform dataSet exports, e.g. for prefix dataSetExport, the
	 * file will be saved to <code>src/main/resources/dataSetExport/exportName</code>
	 * <p>
	 * If resourceDir is empty, we will never try to load the resource from the file system.
	 * 
	 * @return Resource directory to export to and import from
	 */
	String resourceDir() default "src/main/resources";

	/**
	 * Prefix of the resources on the classpath, used to perform resource lookup when compareToResource is true.
	 * <p>
	 * Defaults to <code>dataSetExport</code>
	 * <p>
	 * This prefix will also be added to the resourceDir when performing physical exports.
	 * <p>
	 * Note that the prefix will <strong>NOT</strong> be automatically added to the name of the DataSet import, so we still need to supply
	 * it there, e.g. <code>dataSetExport/myExport.xml<code>
	 * 
	 * @return resource prefix
	 */
	String resourcePrefix() default "dataSetExport";

	/**
	 * Specifies if we should try to load the resource for a DataSet import from the file
	 * system provided the property <code>ju-testing.export.compareToResource</code> is set to false.
	 * This allows us to use the exports of tests directly as imports for dependent test cases.
	 * <p>
	 * Defaults to false, i.e. import resources are always loaded from the classpath exclusively.
	 * 
	 * @return True if we support loading resources from the file system, false if we only load from classpath.
	 */
	boolean loadImportResourcesFromFileSystem() default false;
}