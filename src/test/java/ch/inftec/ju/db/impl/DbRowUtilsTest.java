package ch.inftec.ju.db.impl;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.DbRowUtils;
import ch.inftec.ju.db.DbRowUtils.DbRowBuilder;

public class DbRowUtilsTest {
	@Test
	public void testEqualsAndHashCode() {
		DbRowBuilder r1Builder = DbRowUtils.newDbRow();
		DbRowBuilder r2Builder = DbRowUtils.newDbRow();
		
		r1Builder.addValue("col1", 1, 1);		
		Assert.assertFalse(r1Builder.getRow().equals(r2Builder.getRow()));
		Assert.assertFalse(r1Builder.getRow().hashCode() == r2Builder.getRow().hashCode());
		
		r2Builder.addValue("col2", 2, 2);		
		Assert.assertFalse(r1Builder.getRow().equals(r2Builder.getRow()));
		Assert.assertFalse(r1Builder.getRow().hashCode() == r2Builder.getRow().hashCode());
		
		r1Builder.addValue("col2", 2, 2);
		r2Builder.addValue("col1", 1, 1);
		
		// Still false, because order doesn't match
		Assert.assertFalse(r1Builder.getRow().equals(r2Builder.getRow()));
		Assert.assertFalse(r1Builder.getRow().hashCode() == r2Builder.getRow().hashCode());
		
		DbRowBuilder r3Builder = DbRowUtils.newDbRow();
		r3Builder.addValue("col1", 1, 1);
		r3Builder.addValue("col2", 2, 2);
		
		Assert.assertTrue(r1Builder.getRow().hashCode() == r3Builder.getRow().hashCode());
		Assert.assertTrue(r1Builder.getRow().equals(r3Builder.getRow()));
		Assert.assertTrue(r3Builder.getRow().equals(r1Builder.getRow()));
		Assert.assertTrue(r1Builder.getRow().equals(r1Builder.getRow()));
		
		Assert.assertFalse(r1Builder.getRow().equals(null));
		Assert.assertFalse(r1Builder.getRow().equals(this));
	}
	
	@Test
	public void testUpperAndLowerCase() {
		DbRow row = DbRowUtils.newDbRow()
				.addValue("lower", 1, "lower")
				.addValue("Mixed", 1, "Mixed")
				.addValue("UPPER", 1, "UPPER")
				.getRow();
		
		Assert.assertEquals("LOWER", row.getColumnName(0));
		Assert.assertEquals("MIXED", row.getColumnName(1));
		Assert.assertEquals("UPPER", row.getColumnName(2));
		
		Assert.assertEquals("lower", row.getValue("lower"));
		Assert.assertEquals("lower", row.getValue("Lower"));
		Assert.assertEquals("lower", row.getValue("LOWER"));
	}
	
	@Test
	public void testNull() {
		DbRow row = DbRowUtils.newDbRow()
				.addValue("lower", 1, null)
				.getRow();
		
		Assert.assertNull(row.getValue("lower"));
		Assert.assertNull(row.getValue("something"));
		Assert.assertNull(row.getValue(null));
		
		try {
			DbRowUtils.newDbRow()
				.addValue(null, 1, null);
			Assert.fail("Adding null column should throw NullPointerException");
		} catch (NullPointerException ex) {
			// Expected
		}
	}
}
