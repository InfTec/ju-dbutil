package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.testing.db.DbTestAnnotationHandler.DataSetConfigInfo;
import ch.inftec.ju.testing.db.DbTestAnnotationHandler.DbTestAnnotationInfo;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.XString;

/**
 * Suite class that can be used to execute tests with DataSetExport in the correct order.
 * 
 * @author martin.meyer@inftec.ch
 * 
 */
public class DataSetExportSuite extends ParentRunner<Runner> {
	private static Logger logger = LoggerFactory.getLogger(DataSetExportSuite.class);

	private DataSetAwareParentRunner<Runner> parentRunner;

	// private List<Runner> runners;

	public DataSetExportSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
		super(klass);

		logger.debug("Initializing DataSetExportSuite");

		// Check if we have a RootSuite set
		logger.debug("Looking for RootSuite");
		List<RootSuite> rootSuiteAnnos = ReflectUtils.getAnnotations(klass, RootSuite.class, true);
		if (rootSuiteAnnos.size() > 0) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Runner> parentRunnerClass = (Class<? extends Runner>) rootSuiteAnnos.get(0).value();
				logger.debug("Found RootSuite: " + parentRunnerClass);

				@SuppressWarnings("unchecked")
				ParentRunner<Runner> suiteRunner = (ParentRunner<Runner>) new AnnotatedBuilder(builder).runnerForClass(parentRunnerClass);

				this.parentRunner = new DataSetAwareParentRunner<>(suiteRunner);

				// Print dependency report
				XString xs = new XString();
				this.parentRunner.printDependencyReport(xs);
				logger.info("DataSetExport Suite dependency information: \n\n" + xs.toString());

				if (this.parentRunner.hasCyclicDependencies()) {
					throw new JuRuntimeException("Cyclic dependencies detected. Check log for details.");
				}
			} catch (JuRuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't initialize RootSuite", ex);
			}
		} else {
			logger.debug("No RootSuite specified");

			throw new JuRuntimeException("DataSetExportSuite without RootSuite annotation not supported yet");
		}
	}

	private static class DataSetAwareParentRunner<T> extends ParentRunner<T> {
		private final ParentRunner<T> originalRunner;
		private List<T> originalChildren;
		private List<DataSetObject> reorderedChildren;

		public DataSetAwareParentRunner(ParentRunner<T> originalRunner) throws InitializationError {
			super(originalRunner.getTestClass().getJavaClass());

			this.originalRunner = originalRunner;

			this.reorganizeRunner();
		}

		public boolean hasCyclicDependencies() {
			for (DataSetObject o : this.reorderedChildren) {
				if (o.hasCyclicDependency)
					return true;

				if (o.relatedRunner != null) {
					return o.relatedRunner.hasCyclicDependencies();
				}
			}

			return false;
		}

		private void reorganizeRunner() {
			try {
				Method m = ReflectUtils.getDeclaredMethodInherited(this.originalRunner.getClass(), "getChildren", null);
				m.setAccessible(true);
				@SuppressWarnings("unchecked")
				List<T> children = (List<T>) m.invoke(this.originalRunner, (Object[]) null);
				this.originalChildren = children;

				// Warp all original children in DataSetAwareParentRunners and DataSetObjects
				this.reorderedChildren = new ArrayList<>();
				for (T originalChild : this.originalChildren) {
					// Check if we have a parent runner
					if (originalChild instanceof ParentRunner) {
						DataSetAwareParentRunner<?> actualChild = new DataSetAwareParentRunner<>((ParentRunner<?>) originalChild);
						DataSetObject dataSetObject = new DataSetObject(actualChild, null);
						dataSetObject.gatherDataSetInfos(actualChild.reorderedChildren);
						this.reorderedChildren.add(dataSetObject);
					} else if (originalChild instanceof FrameworkMethod) {
						// Framework Method
						FrameworkMethod frameworkMethod = (FrameworkMethod) originalChild;
						Method testMethod = frameworkMethod.getMethod();
						System.err.println(testMethod);

						DataSetObject dataSetObject = new DataSetObject(null, frameworkMethod);
						dataSetObject.gatherDataSetInfos(testMethod, this.describeChild(originalChild));
						this.reorderedChildren.add(dataSetObject);
					}
				}

				// Now, we can sort the list
				logger.debug("Sorting runner " + this.originalRunner.getDescription().getDisplayName());
				this.sort(this.reorderedChildren);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't reorganize children for " + this.originalRunner.getDescription().getDisplayName(),
						ex);
			}
		}

		private void printDependencyReport(XString xs) {
			xs.addLine("Dependency info for " + this.originalRunner.getDescription().getDisplayName());
			xs.increaseIndent();

			for (DataSetObject o : this.reorderedChildren) {
				// Check if we have dependencies. If so, list them...
				// TODO
				// xs.newLine();
				
				if (o.relatedRunner != null && o.relatedRunner instanceof DataSetAwareParentRunner) {
					xs.newLine();
					((DataSetAwareParentRunner<?>) o.relatedRunner).printDependencyReport(xs);
				} else {
					xs.addLine(o);

					if (o.hasCyclicDependency) {
						xs.addText(" -> ! CYCLIC DEPENDENCIES !");
					}

					xs.increaseIndent();
					// Print explicit dependencies
					if (!o.dataSetImports.isEmpty()) {
						xs.addLine("Imports: ");
						for (String imports : o.dataSetImports) {
							xs.addText(imports);
						}
					}
					if (!o.dataSetExports.isEmpty()) {
						xs.addLine("Exports: ");
						for (String exports : o.dataSetExports) {
							xs.addText(exports);
						}
					}
					xs.decreaseIndent();
				}
			}

			xs.decreaseIndent();
		}

		@Override
		protected List<T> getChildren() {
			List<T> orderedChildrenList = new ArrayList<>();
			for (DataSetObject dataSetObject : this.reorderedChildren) {
				@SuppressWarnings("unchecked")
				T relatedObject = (T) dataSetObject.getRelatedObject();
				orderedChildrenList.add(relatedObject);
			}

			return orderedChildrenList;
		}

		@Override
		protected Description describeChild(T child) {
			try {
				Method m = ReflectUtils.getDeclaredMethodInherited(this.originalRunner.getClass(), "describeChild",
						new Class<?>[] { Object.class });
				m.setAccessible(true);
				return (Description) m.invoke(this.originalRunner, child);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't invoke describeChild of base parent runner", ex);
			}
		}

		@Override
		protected void runChild(T child, RunNotifier notifier) {
			try {
				Method m = ReflectUtils.getDeclaredMethodInherited(this.originalRunner.getClass(), "runChild", new Class<?>[] {
						Object.class, RunNotifier.class });
				m.setAccessible(true);
				m.invoke(this.originalRunner, child, notifier);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't invoke runChild of base parent runner", ex);
			}
		}

		private void sort(List<DataSetObject> dataSetObjects) {
			List<DataSetObject> sortedObjects = new ArrayList<>();

			// First, add all objects that contain no DataSet annotations at all
			for (DataSetObject o : dataSetObjects) {
				if (o.dataSetExports.isEmpty() && o.dataSetImports.isEmpty()) {
					sortedObjects.add(o);
				}
			}
			dataSetObjects.removeAll(sortedObjects);

			// Now, iterate through the list and add objects without dependencies on others as long as the list is not empty
			while (!dataSetObjects.isEmpty()) {
				for (DataSetObject o : dataSetObjects) {
					// Check if the object has no imports from other pending objects
					boolean hasDependentImports = false;
					for (DataSetObject otherObject : dataSetObjects) {
						if (otherObject != o) {
							for (String imports : o.dataSetImports) {
								if (otherObject.dataSetExports.contains(imports)) {
									hasDependentImports = true;
									break;
								}
							}
						}
					}

					if (!hasDependentImports) {
						sortedObjects.add(o);
					}
				}

				if (!dataSetObjects.removeAll(sortedObjects)) {
					break;
				}
			}

			// Add cyclic dependency information (if any)
			if (!dataSetObjects.isEmpty()) {
				for (DataSetObject o : dataSetObjects) {
					// For now, we'll just flag objects that suffer from cyclic dependencies. Would be nicer to
					// actually see what exactly they are...
					o.hasCyclicDependency = true;
				}
				sortedObjects.addAll(dataSetObjects);
				dataSetObjects.clear();
			}

			// Re-add the objects in the right order
			dataSetObjects.addAll(sortedObjects);
		}
	}

	private static class DataSetObject {
		private final Set<String> dataSetImports = new LinkedHashSet<>();
		private final Set<String> dataSetExports = new LinkedHashSet<>();
		private final DataSetAwareParentRunner<?> relatedRunner;
		private final FrameworkMethod relatedMethod;

		private boolean hasCyclicDependency = false;

		public DataSetObject(DataSetAwareParentRunner<?> relatedRunner, FrameworkMethod relatedMethod) {
			this.relatedRunner = relatedRunner;
			this.relatedMethod = relatedMethod;
		}

		public Object getRelatedObject() {
			return this.relatedMethod != null ? this.relatedMethod : this.relatedRunner;
		}

		public void gatherDataSetInfos(Method m, Description desc) {
			logger.debug("Gathering DataSetInfos for method " + m);

			DbTestAnnotationInfo annoInfo = DbTestAnnotationHandler.getDbTestAnnotationInfo(m);
			DataSetConfigInfo configInfo = DbTestAnnotationHandler.getDataSetConfigInfo(annoInfo);

			// Imports
			for (ReflectUtils.AnnotationInfo<DataSet> dsImport : annoInfo.getDataSetAnnos()) {
				// Clean insert
				this.dataSetImports.add(this.getResourceString("", dsImport.getAnnotation().value()));

				// Inserts
				for (String insert : dsImport.getAnnotation().inserts()) {
					this.dataSetImports.add(this.getResourceString(configInfo.getResourcePrefix(), insert));
				}
			}

			// Export
			for (ReflectUtils.AnnotationInfo<DataSetExport> dsExport : annoInfo.getDataSetExportAnnos()) {
				// Export
				this.dataSetExports.add(this.getResourceString(configInfo.getResourcePrefix()
						, new DbTestAnnotationHandler(m, desc).getExportFileName(dsExport.getAnnotation())));
			}
		}

		private String getResourceString(String resourcePrefix, String fileName) {
			return Paths.get(resourcePrefix, fileName).toString();
		}

		public void gatherDataSetInfos(List<DataSetObject> childObjects) {
			logger.debug("Gathering DataSetInfos for " + this);

			for (DataSetObject dsObject : childObjects) {
				this.dataSetImports.addAll(dsObject.dataSetImports);
				this.dataSetExports.addAll(dsObject.dataSetExports);
			}
		}

		@Override
		public String toString() {
			if (this.relatedRunner != null)
				return this.relatedRunner.getDescription().getDisplayName();
			else if (this.relatedMethod != null)
				return this.relatedMethod.getName();
			else
				return super.toString();
		}
	}

	@Override
	protected List<Runner> getChildren() {
		return this.parentRunner.getChildren();
	}

	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(Runner child, RunNotifier notifier) {
		child.run(notifier);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface RootSuite {
		public Class<?> value();
	}

	public static class TestClass {
		@Test
		public void test() {
		}
	}
}
