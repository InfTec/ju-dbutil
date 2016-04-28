package ch.inftec.ju.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ConnectionInfoRoutingDataSource extends AbstractRoutingDataSource {
	final Logger logger = LoggerFactory.getLogger(ConnectionInfoRoutingDataSource.class);
	
	@Autowired
	private ConnectionInfoContextHolder contextHolder;
	
	@Override
	protected Object determineCurrentLookupKey() {
		ConnectionInfo connectionInfo = this.contextHolder.getConnectionInfo();
		logger.debug("Determining lookup key -> " + connectionInfo);
		
		return connectionInfo;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setTargetDataSources(@SuppressWarnings("rawtypes") Map targetDataSources) {
		Set<ConnectionInfo> connectionInfos = targetDataSources.keySet();
		this.contextHolder.setAvailableConnectionInfos(connectionInfos);
		
		super.setTargetDataSources(targetDataSources);
	}
	
	public void setTargetConnectionInfoDataSources(List<ConnectionInfoDriverManagerDataSource> dataSources) {
		Map<ConnectionInfo, DataSource> map = new LinkedHashMap<>();
		for (ConnectionInfoDriverManagerDataSource dataSource : dataSources) {
			map.put(dataSource.getConnectionInfo(), dataSource);
		}
		
		this.setTargetDataSources(map);
	}
}
