/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.adapter;

import ai.quantumics.api.req.DataBaseRequest;
import ai.quantumics.api.util.ResultSetToJsonMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Slf4j
//@NoArgsConstructor
//@AllArgsConstructor
public class OracleAdapter {
  /*private String hostname;
  private String username;
  private String password;
  private int port;
  private String defaultDB;
  private String serviceName;

  public OracleAdapter(DataBaseRequest request) {
    hostname = request.getHostName();
    username = request.getUserName();
    password = request.getPassword();
    port = request.getPort();
    serviceName = request.getServiceName();
  }

  public Connection getConnection() {
    Connection oracleConnection;
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      oracleConnection = DriverManager.getConnection(getConnectionString(), username, password);
    } catch (final Exception e) {
      oracleConnection = null;
      log.error("Error While establishing connection with oracle database {}", e.getMessage());
    }
    return oracleConnection;
  }

  private String getConnectionString() {
    return String.format("jdbc:oracle:thin:@%s:%d:%s", hostname, port, serviceName);
  }

  public JSONArray getTables() throws SQLException {
    JSONArray tableNames = new JSONArray();
    try (Connection connection = getConnection();
        Statement statement = connection != null ? connection.createStatement() : null;
        ResultSet resultSet = Objects.requireNonNull(statement).executeQuery(getSqlQuery())) {
      tableNames = ResultSetToJsonMapper.mapResultSet(resultSet);
    } catch (final Exception e) {
      log.error("Exception occurred while executing statement {}", e.getMessage());
    }
    return tableNames;
  }

  private String getSqlQuery() {
    return "SELECT table_name FROM all_tables ORDER BY table_name;";
  }

  public boolean testConnection() {
    return getConnection() != null;
  }*/
}
