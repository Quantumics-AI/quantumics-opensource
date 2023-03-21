package ai.quantumics.api.adapter;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ai.quantumics.api.req.ConnectorProperties;

public class OracleConnector implements Connectors{

	@Override
	public DataSource getRDBMSConnection(ConnectorProperties properties) {
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(Connectors.ORACLE_DRIVER);
		dataSource.setUrl(String.format("jdbc:oracle:thin:@%s:%d:%s", properties.getHostName(), properties.getPort(), properties.getServiceName()));
		dataSource.setUsername(properties.getUserName());
		dataSource.setPassword(properties.getPassword());
		
		return dataSource;
	}

}
