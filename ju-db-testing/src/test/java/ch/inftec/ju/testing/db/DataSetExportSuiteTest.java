package ch.inftec.ju.testing.db;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.rules.ExpectedException;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.inftec.ju.testing.db.DataSetExportSuite.RootSuite;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ClasspathSuite.MyTestClasspathSuite;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ClasspathSuite._cpSuite_TestClass;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite.MyTestExportsSuite;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite._exports_TestClass1;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite._exports_TestClass2;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite_Cyclic.MyTestExportsSuite_Cyclic;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite_Cyclic2.MyTestExportsSuite_Cyclic2;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite_Cyclic3.MyTestExportsSuite_Cyclic3;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite_Nested.MyTestExportsSuite_Nested;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite_Nested._nested_testClass1;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_ExportsSuite_Nested._nested_testClass2;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_Suite.MyTestSuite;
import ch.inftec.ju.testing.db.DataSetExportSuiteTest.DataSetExport_Suite._suite_TestClass;
import ch.inftec.ju.util.TestUtils;

/**
 * Test case for the DataSetExportSuite.
 * 
 * @author Martin Meyer <martin.meyer@inftec.ch>
 * 
 */
public class DataSetExportSuiteTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Runs DataSetExportSuite with a default JUnit Suite class to define
	 * the involved classes.
	 */
	@Test
	public void canRun_usingSuite() {
		_suite_TestClass.runCount = 0;

		Result res = TestUtils.runJUnitTests(DataSetExport_Suite.class);

		Assert.assertEquals(1, res.getRunCount());
		Assert.assertEquals(0, res.getFailureCount());

		Assert.assertEquals(1, _suite_TestClass.runCount);
	}
	
	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestSuite.class)
	public static class DataSetExport_Suite {
		@RunWith(Suite.class)
		@SuiteClasses({ _suite_TestClass.class })
		public static class MyTestSuite {
		}

		public static class _suite_TestClass {
			private static int runCount = 0;

			@Test
			public void test() {
				runCount++;
			}
		}
	}
	
	@Test
	public void canRun_usingClasspathSuite() {
		_cpSuite_TestClass.runCount = 0;

		Result res = TestUtils.runJUnitTests(DataSetExport_ClasspathSuite.class);

		Assert.assertEquals(1, res.getRunCount());
		Assert.assertEquals(0, res.getFailureCount());

		Assert.assertEquals(1, _cpSuite_TestClass.runCount);
	}

	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestClasspathSuite.class)
	public static class DataSetExport_ClasspathSuite {
		@RunWith(ClasspathSuite.class)
		@ClassnameFilters({ ".*_cpSuite.*" })
		public static class MyTestClasspathSuite {
		}

		public static class _cpSuite_TestClass {
			private static int runCount = 0;

			@Test
			public void test() {
				runCount++;
			}
		}
	}

	@Test
	public void canReorganize_dataSetExportOrder() {
		// First, we'll check the order if we're running the Suite class
		// without reordering
		MyTestExportsSuite.runCount = 0;
		Result res1 = TestUtils.runJUnitTests(MyTestExportsSuite.class);

		// Should have normal order (note that we use FixMethodOrder
		this.checkTestOrder(res1, 0, 1, 2, 3);

		// Now, make sure the reordering based on the DataSetExports works as expected
		MyTestExportsSuite.runCount = 0;
		Result res2 = TestUtils.runJUnitTests(DataSetExport_ExportsSuite.class);

		// First, we'll expect the noImport case. Then the other cases in reverse
		// order as we added dependencies on them
		this.checkTestOrder(res2, 1, 0, 3, 2);
	}

	private void checkTestOrder(Result res, int... orderIndexes) {
		Assert.assertEquals(4, res.getRunCount());
		Assert.assertEquals(0, res.getFailureCount());

		Assert.assertEquals(orderIndexes[0], _exports_TestClass1.runIndex1);
		Assert.assertEquals(orderIndexes[1], _exports_TestClass1.runIndex2);
		Assert.assertEquals(orderIndexes[2], _exports_TestClass2.runIndex);
		Assert.assertEquals(orderIndexes[3], _exports_TestClass2.runIndexNoImport);
	}

	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestExportsSuite.class)
	public static class DataSetExport_ExportsSuite {
		@RunWith(Suite.class)
		@SuiteClasses({ _exports_TestClass1.class, _exports_TestClass2.class })
		public static class MyTestExportsSuite {
			private static int runCount = 0;
		}

		@FixMethodOrder(MethodSorters.NAME_ASCENDING)
		public static class _exports_TestClass1 {
			private static int runIndex1 = 0;
			private static int runIndex2 = 0;

			@Test
			@DataSet("dataSetExport/exportForTest1")
			public void test1() {
				runIndex1 = MyTestExportsSuite.runCount++;
			}

			@Test
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest1")
			public void test2() {
				runIndex2 = MyTestExportsSuite.runCount++;
			}
		}

		@FixMethodOrder(MethodSorters.NAME_ASCENDING)
		public static class _exports_TestClass2 {
			private static int runIndex = 0;
			private static int runIndexNoImport = 0;

			@Test
			@DataSet("someUnrelatedDataSet")
			@DataSetExport(tablesDataSet = "ds")
			public void test() {
				runIndex = MyTestExportsSuite.runCount++;
			}

			@Test
			public void testNoImport() {
				runIndexNoImport = MyTestExportsSuite.runCount++;
			}
		}
	}

	@Test
	public void canDetect_cyclicDependency_inSameClass() {
		thrown.expectMessage("Cyclic dependencies detected");

		TestUtils.runJUnitTests(DataSetExport_ExportsSuite_Cyclic.class);
	}

	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestExportsSuite_Cyclic.class)
	public static class DataSetExport_ExportsSuite_Cyclic {
		@RunWith(Suite.class)
		@SuiteClasses({ _exports_TestClass1_Cyclic.class })
		public static class MyTestExportsSuite_Cyclic {
		}

		@FixMethodOrder(MethodSorters.NAME_ASCENDING)
		public static class _exports_TestClass1_Cyclic {
			@Test
			@DataSet("dataSetExport/exportForTest1")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest2")
			public void test1() {
			}

			@Test
			@DataSet("dataSetExport/exportForTest2")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest1")
			public void test2() {
			}
		}
	}

	@Test
	public void canDetect_cyclicDependency_inDifferentClass() {
		thrown.expectMessage("Cyclic dependencies detected");

		TestUtils.runJUnitTests(DataSetExport_ExportsSuite_Cyclic2.class);
	}

	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestExportsSuite_Cyclic2.class)
	public static class DataSetExport_ExportsSuite_Cyclic2 {
		@RunWith(Suite.class)
		@SuiteClasses({ _exports_TestClass1_Cyclic2.class, _exports_TestClass2_Cyclic2.class })
		public static class MyTestExportsSuite_Cyclic2 {
		}

		public static class _exports_TestClass1_Cyclic2 {
			@Test
			@DataSet("dataSetExport/exportForTest1")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest2")
			public void test1() {
			}
		}

		public static class _exports_TestClass2_Cyclic2 {
			@Test
			@DataSet("dataSetExport/exportForTest2")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest1")
			public void test2() {
			}
		}
	}

	/**
	 * Checks if we detect complex cyclic dependencies of the form A->B, B->C, C->A
	 */
	@Test
	public void canDetect_complexCyclicDependencies() {
		thrown.expectMessage("Cyclic dependencies detected");

		TestUtils.runJUnitTests(DataSetExport_ExportsSuite_Cyclic3.class);
	}

	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestExportsSuite_Cyclic3.class)
	public static class DataSetExport_ExportsSuite_Cyclic3 {
		@RunWith(Suite.class)
		@SuiteClasses({ _exports_TestClass1_Cyclic3.class })
		public static class MyTestExportsSuite_Cyclic3 {
		}

		@FixMethodOrder(MethodSorters.NAME_ASCENDING)
		public static class _exports_TestClass1_Cyclic3 {
			@Test
			@DataSet("dataSetExport/exportForTest1")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest2")
			public void test1() {
			}

			@Test
			@DataSet("dataSetExport/exportForTest2")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest3")
			public void test2() {
			}

			@Test
			@DataSet("dataSetExport/exportForTest3")
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest1")
			public void test3() {
			}
		}
	}

	@Test
	public void supports_nestedParentRunners() {
		DataSetExport_ExportsSuite_Nested.runCount = 0;

		Result res = TestUtils.runJUnitTests(DataSetExport_ExportsSuite_Nested.class);
		Assert.assertEquals(2, res.getRunCount());
		Assert.assertEquals(0, res.getFailureCount());

		Assert.assertEquals(0, _nested_testClass2.runIndex);
		Assert.assertEquals(1, _nested_testClass1.runIndex);
	}

	@RunWith(DataSetExportSuite.class)
	@RootSuite(MyTestExportsSuite_Nested.class)
	public static class DataSetExport_ExportsSuite_Nested {
		private static int runCount;

		@RunWith(Suite.class)
		@SuiteClasses({ _nested_testClass1.class, _nested_testSuite.class })
		public static class MyTestExportsSuite_Nested {
		}

		public static class _nested_testClass1 {
			private static int runIndex;

			@Test
			@DataSet("dataSetExport/exportForTest1")
			public void test1() {
				runIndex = DataSetExport_ExportsSuite_Nested.runCount++;
			}
		}

		@RunWith(Suite.class)
		@SuiteClasses(_nested_testClass2.class)
		public static class _nested_testSuite {
		}

		public static class _nested_testClass2 {
			private static int runIndex;

			@Test
			@DataSetExport(tablesDataSet = "ds", exportName = "exportForTest1")
			public void test1() {
				runIndex = DataSetExport_ExportsSuite_Nested.runCount++;
			}
		}
	}

}