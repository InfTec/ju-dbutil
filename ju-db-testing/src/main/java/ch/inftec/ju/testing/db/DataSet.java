package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to load test data sets before executing tests.
 * <p>
 * The annotation can be specified on both, class and method level. Processing of annotations is done
 * in the following order:
 * <ol>
 *   <li>Base class(es)</li>
 *   <li>Class</li>
 *   <li>Method (without overridden methods)</li>
 * </ol>
 * <p>
 * We'll first try to load the resource relative to the test class and then absolute using the classLoader.getResources method.
 * <p>
 * When test data is loaded, the sequences are automatically set back to 1 so we get predictable.
 * new IDs
 * <p>
 * In addition to loading data sets, we can also specify DataInitializers that can perform custom initialization
 * code before and after the data set is loaded.
 * <p>
 * DataSet supports parameterized test data loading in the following way: If a resource name has the postfix
 * <code>{param}</code>, it will be replaced by the name of the parameterized test before performing the
 * resource lookup.
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataSet {
	/**
	 * Postfix that can be used for parameterized test name replacement.
	 */
	public static final String PARAM_POSTFIX = "{param}";
	
	/**
	 * Helper class to set as default value (we cannot set null nor void.class
	 * as default.
	 * @author Martin
	 *
	 */
	public static final class DEFAULT_DATA_INITIALIZER extends ServerCode {
		@Override
		public void execute() {
			// Do nothing
		}
	}
	
	public static final String NO_CLEAN_INSERT = "";
	
	/**
	 * Data set that will be executed as a clean insert.
	 * <p>
	 * A clean insert data set must always be specified. If we don't want to perform a
	 * clean insert, we can set it to DataSet.NO_CLEAN_INSERT.
	 * @return Clean insert data set
	 */
	String value();
	
	/**
	 * Optional list of data sets that will be inserted using the insert method (without previous cleaning).
	 * @return Array of insert data sets
	 */
	String[] inserts() default{};
	
	/**
	 * Value to reset the sequences to. Default to 1.
	 * <p>
	 * If we find multiple data sets that define a sequenceValue, we'll use the one last in the evaluation
	 * chain (last being the current method).
	 * @return Value to reset sequences to
	 */
	int sequenceValue() default 1;
	
	/**
	 * Optional pre initializer that is called before the data set is loaded.
	 * @return DataInitializer class
	 */
	Class<? extends ServerCode> preInitializer() default DEFAULT_DATA_INITIALIZER.class;
	
	/**
	 * Optional post initializer that is called after the data set is loaded.
	 * <p>
	 * Post initializers will be called <strong>before</strong> the sequences are reset!
	 * @return DataInitializer class
	 */
	Class<? extends ServerCode> postInitializer() default DEFAULT_DATA_INITIALIZER.class;
}