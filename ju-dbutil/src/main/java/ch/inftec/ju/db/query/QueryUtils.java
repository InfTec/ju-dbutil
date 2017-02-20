package ch.inftec.ju.db.query;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.JuUrl;
import ch.inftec.ju.util.XString;

/**
 * Class containing utility functions that can be used to construct SQL queries.
 * @author martin.meyer@inftec.ch
 *
 */
public class QueryUtils {
	/**
	 * Returns a builder to create and configure a native SQL query.
	 * <p>
	 * NativeQuery are built using a query template that can be modified using filter objects and the likes.
	 * @return
	 */
	public static NativeQueryBuilder createNativeQuery(EntityManager em) {
		return new NativeQueryBuilder(em);
	}
	
	public static final class NativeQueryBuilder {
		private Logger logger = LoggerFactory.getLogger(QueryUtils.class);
		
		private final EntityManager em;
		private final JuEmUtil emUtil;
		
		private String baseQuery;
		
		private String whereClausePlaceholder = "${whereClause}";
		private String orderByClausePlaceholder = "${orderByClause}";
		private String orderByClausePlaceholderNoPrefix = "${orderByClauseNoPrefix}";
		
		private Object filterObject;
		private Set<String> excludedFilterObjectProperties = new HashSet<>();
		private Map<String, String> attributeMappings = new HashMap<>();
		private Map<String, Object> queryAttributes = new HashMap<>();
		
		private List<AttributeOrdering> ordering = new ArrayList<>();
		
		private String defaultPrefix = null;
		
		private boolean caseInsensitiveSearch = false;
		
		private boolean wildcardSearch = false;
		private static final char DB_WILDCARD_CHAR = '%';
		
		private NativeQueryBuilder(EntityManager em) {
			this.em  = em;
			this.emUtil = new JuEmUtil(em);
			
			this.excludedFilterObjectProperties.add("class");
		}
		
		public NativeQueryBuilder fromResourceRelativeTo(Class<?> clazz, String resourceName) {
			URL url = JuUrl.existingResourceRelativeTo(resourceName, clazz);
			this.baseQuery = new IOUtil().loadTextFromUrl(url);
			
			return this;
		}
		
		/**
		 * The default prefix to be used for attributes. This will be prefixed to all attributes when not null.
		 * <p>
		 * Example: defaultPrefix: p, Attribute id -&gt; p.id
		 * @param prefix Default prefix
		 * @return
		 */
		public NativeQueryBuilder defaultPrefix(String prefix) {
			this.defaultPrefix = prefix;
			return this;
		}
		
		/**
		 * Enables or disables wildcard search.
		 * <p>
		 * By default, wildcard search is disabled.
		 * <p>
		 * This uses the default database wildcard character.
		 * @param wildcardSearch True to enable wildcard search
		 * @return
		 */
		public NativeQueryBuilder wildcardSearch(boolean wildcardSearch) {
			this.wildcardSearch = wildcardSearch;
			return this;
		}
		
		/**
		 * Enables or disables case insensitive search.
		 * <p>
		 * By default, case insensitive search is disabled.
		 * @param caseInsensitiveSearch
		 * @return
		 */
		public NativeQueryBuilder caseInsensitiveSearch(boolean caseInsensitiveSearch) {
			this.caseInsensitiveSearch = caseInsensitiveSearch;
			return this;
		}
		
		/**
		 * Specifies a filter object to filter by.
		 * <p>
		 * We take all public bean properties from the object and set them in the where clause of the
		 * query.
		 * <p>
		 * The constructed where clause will replace the whereClause placeholder (by default: ${whereClause})
		 * @param obj
		 * @return
		 */
		public NativeQueryBuilder filterByObject(Object obj) {
			this.filterObject = obj;
			return this;
		}
		
		/**
		 * Specifies an explicit attribute mapping, e.g. for a filter object.
		 * <p>
		 * Use this if the name of the column on the DB is different from the bean property name.
		 * @param attributeName Name of the property, e.g. in the filter object
		 * @param nameOnDb Name of the column on the DB
		 * @return
		 */
		public NativeQueryBuilder attributeMapping(String attributeName, String nameOnDb) {
			this.attributeMappings.put(attributeName, nameOnDb);
			return this;
		}
		
		/**
		 * Excludes an attribute (from a filter object) from the query.
		 * @param attributeName Attribute to be excluded
		 * @return
		 */
		public NativeQueryBuilder exludeAttribute(String attributeName) {
			this.excludedFilterObjectProperties.add(attributeName);
			return this;
		}
		
		/**
		 * Adds an order by clause for the specified attribute.
		 * <p>
		 * The constructed order by clause will replace the order by placeholder, by default ${orderByClause}
		 * @param columnName Column name (or attribute name)
		 * @param ordering Ordering
		 * @return
		 */
		public NativeQueryBuilder orderBy(String columnName, Ordering ordering) {
			this.ordering.add(new AttributeOrdering(columnName, ordering));
			return this;
		}

		/**
		 * Adds order by clauses for all specified attribute orderings.
		 * @param attributeOrderings List of attribute orderings
		 * @return
		 */
		public NativeQueryBuilder orderBy(AttributeOrdering... attributeOrderings) {
			for (AttributeOrdering attributeOrdering : attributeOrderings) {
				this.ordering.add(attributeOrdering);
			}
			return this;
		}
		
		private Query buildQuery() {
			AssertUtil.assertNotEmpty("Base Query was not supplied", this.baseQuery);
			
			String actualQuery = this.baseQuery;
			
			// Replace where clause (if any)
			if (this.baseQuery.indexOf(this.whereClausePlaceholder) > 0) {
				XString xsWhere = new XString();
				
				if (this.filterObject != null) {
					// Construct where clause using filter object
					try {
						BeanInfo bi = Introspector.getBeanInfo(this.filterObject.getClass());
						for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
							String name = pd.getName();
							if (!this.excludedFilterObjectProperties.contains(name)) {
								String mappedName = this.getActualColumnName(name);
								
								// Workaround for boolean property that uses the "is" instead of "get" syntax
								Method readMethod = pd.getReadMethod();
								if (readMethod == null && pd.getPropertyType() == Boolean.class) {
									String methodName = "is" + StringUtils.capitalize(pd.getName());
									try {
										readMethod = this.filterObject.getClass().getMethod(methodName);
									} catch (NoSuchMethodException ex) {
										logger.debug(String.format("Couldn't find method %s on object %s"
												, methodName, filterObject.getClass()));
										// Ignore...
									}
								}
								AssertUtil.assertNotNull("Property is not readable: " + pd.getName(), readMethod);
								
								Object value = readMethod.invoke(this.filterObject);
								if (value != null) {
									xsWhere.assertEmptyOrText(" AND ");
									
									// Handle wildcard search
									String operator = "=";
									if (this.wildcardSearch && value instanceof String && value.toString().indexOf(DB_WILDCARD_CHAR) >= 0) {
										// TODO: Wildcard escaping...
										operator = " like ";
									}
									
									// Handle case insensitive search
									String convertedColumn = mappedName;
									if (this.caseInsensitiveSearch && value instanceof String) {
										convertedColumn = this.emUtil.asConnUtil().getDbHandler().wrapInLowerString(mappedName);
										value = value.toString().toLowerCase();
									}
									
									xsWhere.addFormatted("%s%s:%s", convertedColumn, operator, mappedName);
									
									this.queryAttributes.put(mappedName, value);
								}
							}
						}
					} catch (Exception ex) {
						throw new JuRuntimeException("Couldn't introspect bean", ex);
					}
				}
				
				if (xsWhere.isEmpty()) {
					xsWhere.addText("1 = 1");
				}
				String whereClause = String.format("(%s)", xsWhere.toString());
				
				while (actualQuery.indexOf(this.whereClausePlaceholder) > 0) {
					actualQuery = actualQuery.replace(this.whereClausePlaceholder, whereClause);
				}
			}
			
			// Add order by clause
			if (this.ordering.size() > 0) {
				XString xsOrderBy = new XString();
				XString xsOrderByNoPrefix = new XString();
				for (AttributeOrdering ao : this.ordering) {
					xsOrderBy.assertEmptyOrText(", ");
					xsOrderByNoPrefix.assertEmptyOrText(", ");
					xsOrderBy.addText(this.getActualColumnName(ao.getAttributeName()));
					xsOrderByNoPrefix.addText(this.getActualColumnNameWithoutPrefix(ao.getAttributeName()));
					if (ao.getOrdering() == Ordering.DESCENDING) {
						xsOrderBy.addText(" desc");
						xsOrderByNoPrefix.addText(" desc");
					}
				}
				
				while (actualQuery.indexOf(this.orderByClausePlaceholder) > 0) {
					actualQuery = actualQuery.replace(this.orderByClausePlaceholder, xsOrderBy.toString());
				}

				while (actualQuery.indexOf(this.orderByClausePlaceholderNoPrefix) > 0) {
					actualQuery = actualQuery.replace(this.orderByClausePlaceholderNoPrefix, xsOrderByNoPrefix.toString());
				}
			}
			
			logger.debug(actualQuery);
			
			Query qry = this.em.createNativeQuery(actualQuery);
			
			// Set parameters (if any)
			for (String queryAttribute : this.queryAttributes.keySet()) {
				qry.setParameter(queryAttribute, this.queryAttributes.get(queryAttribute));
			}
			
			return qry;
		}
		
		private String getActualColumnName(String attributeName) {
			String actualName = attributeName;
			
			if (this.attributeMappings.containsKey(attributeName)) {
				actualName = this.attributeMappings.get(attributeName);
			}
			
			if (!StringUtils.isEmpty(this.defaultPrefix) && !actualName.contains(".")) {
				actualName = String.format("%s.%s", this.defaultPrefix, actualName);
			}
			
			return actualName;
		}
		
		private String getActualColumnNameWithoutPrefix(String attributeName) {
			String actualName = getActualColumnName(attributeName);
			
			int dotIndex = actualName.indexOf(".");
			if (dotIndex > 0) {
				actualName = actualName.substring(dotIndex + 1);
			}
			
			return actualName;
		}
		
		public Query createQuery() {
			return this.buildQuery();
		}
		
		public static final class AttributeOrdering {
			private final String attributeName;
			private final Ordering ordering;
			
			public AttributeOrdering(String attributeName, Ordering ordering) {
				this.attributeName = attributeName;
				this.ordering = ordering;
			}

			public String getAttributeName() {
				return attributeName;
			}

			public Ordering getOrdering() {
				return ordering;
			}
		}
		
		public static enum Ordering {
			ASCENDING,
			DESCENDING;
		}
	}
}
