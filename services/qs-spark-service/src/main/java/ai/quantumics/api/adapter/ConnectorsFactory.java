package ai.quantumics.api.adapter;

public class ConnectorsFactory {
	
	public Connectors getConnector(String connectorType) {
		
		if(connectorType == null) {
			return null;
		}
		
		if("pgsql".equalsIgnoreCase(connectorType)) {
			return new PgSqlConnector();
		}else if("mysql".equalsIgnoreCase(connectorType)) {
			return new MySqlConnector();
		}else if("mssql".equalsIgnoreCase(connectorType)) {
			return new MsSqlConnector();
		}else if("orcle".equalsIgnoreCase(connectorType)) {
			return new OracleConnector();
		}
		
		return null;
	}
}
