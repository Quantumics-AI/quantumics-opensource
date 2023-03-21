package ai.quantumics.api.adapter;

import javax.sql.DataSource;

import ai.quantumics.api.req.ConnectorProperties;

public interface Connectors {
	
	public String POSTGRESQL_DRIVER = "org.postgresql.Driver";
	public String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	
	DataSource getRDBMSConnection(ConnectorProperties properties);

}
