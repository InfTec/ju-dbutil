package ch.inftec.ju.db.query;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.db.query.QueryUtils.NativeQueryBuilder.AttributeOrdering;
import ch.inftec.ju.db.query.QueryUtils.NativeQueryBuilder.Ordering;
import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DataSet;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.testing.db.JuAssumeUtils;
import ch.inftec.ju.util.ConversionUtils;
import ch.inftec.ju.util.JuStringUtils;

public class NativeQueryBuilderTest extends AbstractDbTest {
	@Override
	protected void runDbInitializationScripts(JuEmfUtil emfUtil) {
		try (EmfWork ew = emfUtil.startWork()) {
			new DbSchemaUtil(ew.getEm()).prepareDefaultSchemaAndTestData();;
		}
	}
	
	@Test
	@DataSet("datasets/singleTestingEntityData.xml")
	public void canIssue_query() {
		Query qry = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canIssue_query.sql")
			.createQuery();
		
		Assert.assertEquals(1, qry.getResultList().size());
	}
	
	@Test
	@DataSet("datasets/singleDataTypes_noTimeStamp.xml")
	public void canFilter_byObject() {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(new FilterObjectIntegerNumber(1))
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("datasets/singleDataTypes_noTimeStamp.xml")
	public void filter_returnsNothing_forNoMatch() {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(new FilterObjectIntegerNumber(2))
			.createQuery();
		
		Assert.assertEquals(0, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void canFilter_byExtendedObject() {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(new FilterObjectIntegerNumberBigIntNumber(1, 2L))
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
		this.assertIdEquals(1L, qry1.getResultList(), 0);
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void doesNotFilter_onNullValue() {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(new FilterObjectIntegerNumberBigIntNumber(1, null))
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.createQuery();
		
		Assert.assertEquals(2, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void canFilter_allTypes() throws Exception {
		FilterAllTypes filter = new FilterAllTypes(1, 2L);
		filter.setVarCharText("one");
		filter.setClobText("oneClob");
		filter.setDateField(JuStringUtils.DATE_FORMAT_DAYS.parse("03.12.1980"));
		
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(filter)
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.exludeAttribute("clobText")
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void wildcardFiltering_isDisabled_byDefault() throws Exception {
		FilterAllTypes filter = new FilterAllTypes(1, 2L);
		filter.setVarCharText("o%");
		
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(filter)
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.createQuery();
		
		Assert.assertEquals(0, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void canFilter_usingWildcards() throws Exception {
		FilterAllTypes filter = new FilterAllTypes(1, 2L);
		filter.setVarCharText("o%");
		
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.wildcardSearch(true)
			.filterByObject(filter)
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void canFilter_caseInsensitively() throws Exception {
		FilterAllTypes filter = new FilterAllTypes(1, 2L);
		filter.setVarCharText("ONE");
		
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.caseInsensitiveSearch(true)
			.filterByObject(filter)
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void caseInsensitiveSearch_isDisabled_byDefault() throws Exception {
		/*
		 * By Default, MySQL is case-insensitive. 
		 * See http://dba.stackexchange.com/questions/15250/how-to-do-a-case-sensitive-search-in-where-clause for more details
		 */
		JuAssumeUtils.dbIsNot(this.connUtil, DbType.MYSQL);
		
		FilterAllTypes filter = new FilterAllTypes(1, 2L);
		filter.setVarCharText("ONE");
		
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(filter)
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.createQuery();
		
		Assert.assertEquals(0, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("datasets/singleDataTypes_noTimeStamp.xml")
	public void canFilter_byEmptyFilter() throws Exception {
		Object filter = new Object();
		
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(filter)
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("datasets/singleDataTypes_noTimeStamp.xml")
	public void canFilter_byNullFilter() throws Exception {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canFilter_byObject.sql")
			.filterByObject(null)
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void canOrder_byAttribute() throws Exception {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canOrder_byAttribute.sql")
			.orderBy("id", Ordering.DESCENDING)
			.createQuery();
		
		Assert.assertEquals(2, qry1.getResultList().size());
		this.assertIdEquals(2L, qry1.getResultList(), 0);
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void canOrder_byMultipleAttributes() throws Exception {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_canOrder_byAttribute.sql")
			.orderBy(new AttributeOrdering[] {
					new AttributeOrdering("intNumber", Ordering.ASCENDING)
					, new AttributeOrdering("bigIntNumber", Ordering.DESCENDING)
			})
			.createQuery();
		
		Assert.assertEquals(2, qry1.getResultList().size());
		this.assertIdEquals(2L, qry1.getResultList(), 0);
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void supports_defaultPrefix() throws Exception {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_supports_defaultPrefix.sql")
			.defaultPrefix("d")
			.filterByObject(new FilterObjectIntegerNumberBigIntNumber(1, 3L))
			.attributeMapping("bigIntegerNumber", "bigIntNumber")
			.orderBy("id", Ordering.ASCENDING)
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
		this.assertIdEquals(2L, qry1.getResultList(), 0);
	}
	
	@Test
	@DataSet("NativeQueryBuilderTest_dataTypes.xml")
	public void defaultPrefix_isNotApplied_forAttributesContainingPrefix() throws Exception {
		Query qry1 = QueryUtils.createNativeQuery(this.em)
			.fromResourceRelativeTo(this.getClass(), "NativeQueryBuilderTest_supports_defaultPrefix.sql")
			.defaultPrefix("d3") // Invalid prefix, shouldn't be applied...
			.filterByObject(new FilterObjectIntegerNumberBigIntNumber(null, 3L))
			.attributeMapping("bigIntegerNumber", "d.bigIntNumber")
			.orderBy("d.id", Ordering.ASCENDING)
			.createQuery();
		
		Assert.assertEquals(1, qry1.getResultList().size());
		this.assertIdEquals(2L, qry1.getResultList(), 0);
	}
	
	private void assertIdEquals(Long expectedId, List<?> resultList, int index) {
		if (resultList.get(0).getClass().isArray()) {
			Assert.assertEquals(new Long(expectedId), ConversionUtils.toLong(((Object[]) resultList.get(index))[0]));
		} else {
			Assert.assertEquals(new Long(expectedId), ConversionUtils.toLong((resultList.get(index))));
		}
	}
	
	private static class FilterObjectIntegerNumber {
		private Integer intNumber;
		
		public FilterObjectIntegerNumber(Integer intNumber) {
			this.intNumber = intNumber;
		}
		
		public Integer getIntNumber() {
			return intNumber;
		}
	}
	
	private static class FilterObjectIntegerNumberBigIntNumber extends FilterObjectIntegerNumber {
		private Long bigIntegerNumber;
		
		public FilterObjectIntegerNumberBigIntNumber(Integer intNumber, Long bigIntegerNumber) {
			super(intNumber);
			this.bigIntegerNumber = bigIntegerNumber;
		}
		
		public Long getBigIntegerNumber() {
			return this.bigIntegerNumber;
		}
	}
	
	private static class FilterAllTypes extends FilterObjectIntegerNumberBigIntNumber {
		private String varCharText;
		private String clobText;
		private Date dateField;
		
		public FilterAllTypes(Integer intNumber, Long bigIntegerNumber) {
			super(intNumber, bigIntegerNumber);
		}

		public String getVarCharText() {
			return varCharText;
		}

		public void setVarCharText(String varCharText) {
			this.varCharText = varCharText;
		}

		public String getClobText() {
			return clobText;
		}

		public void setClobText(String clobText) {
			this.clobText = clobText;
		}

		public Date getDateField() {
			return dateField;
		}

		public void setDateField(Date dateField) {
			this.dateField = dateField;
		}
	}
}
