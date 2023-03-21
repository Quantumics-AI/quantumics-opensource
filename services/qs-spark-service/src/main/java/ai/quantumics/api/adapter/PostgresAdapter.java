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

@Slf4j
//@NoArgsConstructor
//@AllArgsConstructor
public class PostgresAdapter {
  /*private String hostname;
  private String username;
  private String password;
  private String defaultDB;
  private int port;

  public PostgresAdapter(DataBaseRequest request) {
    hostname = request.getHostName();
    username = request.getUserName();
    password = request.getPassword();
    defaultDB = request.getDbName();
    port = request.getPort();
  }

  public Connection getConnection() {
    Connection postgresConn;
    try {
      Class.forName("org.postgresql.Driver");
      postgresConn = DriverManager.getConnection(getConnectionString(), username, password);
    } catch (final Exception e) {
      log.error("Error - Establishing connection with postgresql database {}", e.getMessage());
      postgresConn = null;
    }
    return postgresConn;
  }

  private String getConnectionString() {
    return String.format("jdbc:postgresql://%s:%d/%s", hostname, port, defaultDB);
  }

  public JSONArray getTables() throws SQLException {
    JSONArray tableNames = new JSONArray();
    try (Connection connection = getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(getSqlQuery())) {
      tableNames = ResultSetToJsonMapper.getJSONDataFromResultSet(resultSet);
    } catch (final Exception e) {
      log.error("Exception occurred while executing statement {}", e.getMessage());
    }
    return tableNames;
  }

  private String getSqlQuery() {
    return "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;";
  }

  public boolean testConnection() throws SQLException {
    final Connection conn = getConnection();
    if (conn != null) {
      conn.close();
      return true;
    } else {
      return false;
    }
  }*/
}
