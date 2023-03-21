package ai.quantumics.api.adapter;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ai.quantumics.api.req.ConnectorProperties;

public class MySqlConnector implements Connectors{

	@Override
	public DataSource getRDBMSConnection(ConnectorProperties properties) {
		
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(Connectors.MYSQL_DRIVER);
		dataSource.setUrl(String.format("jdbc:mysql://%s:%d/%s", properties.getHostName(), properties.getPort(), properties.getDatabaseName()));
		dataSource.setUsername(properties.getUserName());
		dataSource.setPassword(properties.getPassword());
		
		return dataSource;
	}

}
