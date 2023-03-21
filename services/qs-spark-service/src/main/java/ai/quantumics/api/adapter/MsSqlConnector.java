package ai.quantumics.api.adapter;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ai.quantumics.api.req.ConnectorProperties;

public class MsSqlConnector implements Connectors{

	@Override
	public DataSource getRDBMSConnection(ConnectorProperties properties) {
		
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(Connectors.MSSQL_DRIVER);
		dataSource.setUrl(String.format("jdbc:sqlserver://%s:%d\\\\%s;databaseName=%s;user=%s;password=%s",
				properties.getHostName(), properties.getPort(), properties.getServiceName(), properties.getDatabaseName(), properties.getUserName(), properties.getPassword()));
		
		return dataSource;
	}

}
