package org.junit.extensions.cpsuite;

import org.junit.Test;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
/*
 * By default, ClasspathSuite would execute all tests it finds.
 * Note that we need to match the full class name, including the package - thus the
 * .* at the beginning of the regex.
 */
@ClassnameFilters({ ".*ClasspathSuiteTest_Test" })
public class ClasspathSuiteTest {
	public static class ClasspathSuiteTest_Test {
		@Test
		public void test() {
		}
	}
}