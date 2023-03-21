package ai.quantumics.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConnectorProperties {
	
	  private String hostName;
	  private String userName;
	  private String password;
	  private String databaseName;
	  private int port;
	  private String serviceName;

}
