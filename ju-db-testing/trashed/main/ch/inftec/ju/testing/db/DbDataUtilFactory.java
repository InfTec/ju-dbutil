package ch.inftec.ju.testing.db;

import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.db.JuDbUtils;

/**
 * Factory method to construct JuDataUtil instances by Spring.
 * @author tgdmemae
 *
 */
public class DbDataUtilFactory implements FactoryBean<DbDataUtil> {
	@Autowired
	private JuDbUtils juDbUtils;
	
	@Autowired
	private ConnectionInfo connectionInfo;
	
	private DefaultDataTypeFactory dataTypeFactory = null;
	
	public void setDataTypeFactory(DefaultDataTypeFactory factory) {
		this.dataTypeFactory = factory;
	}
	
	@Override
	public DbDataUtil getObject() throws Exception {
//		DbDataUtil util = new DbDataUtil(this.juDbUtils);
//		util.setSchema(this.connectionInfo.getSchema());
//		if (this.dataTypeFactory != null) {
//			util.setConfigProperty("http://www.dbunit.org/properties/datatypeFactory", this.dataTypeFactory);
//		}
//		
//		return util;
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return DbDataUtil.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
