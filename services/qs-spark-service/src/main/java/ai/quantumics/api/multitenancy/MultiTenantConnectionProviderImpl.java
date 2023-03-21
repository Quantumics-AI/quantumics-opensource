/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static ai.quantumics.api.constants.QsConstants.QS_DEFAULT_TENANT_ID;

@Slf4j
@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

  private static final long serialVersionUID = 1L;
  @Autowired private DataSource dataSource;

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    log.debug(" MultiTenantConnectionProviderImpl releaseAnyConnection called ");
    connection.close();
  }

  @Override
  public Connection getConnection(String tenantIdentifier) throws SQLException {
    log.debug("  MultiTenantConnectionProviderImpl getConnection called ");
    final Connection connection = getAnyConnection();
    try {
      if (tenantIdentifier != null) {
        connection.createStatement().execute("SET search_path TO " + tenantIdentifier);
      } else {
        connection.createStatement().execute("SET search_path TO " + QS_DEFAULT_TENANT_ID);
      }
    } catch (SQLException e) {
      throw new HibernateException(
          "Could not alter JDBC connection to specified schema [" + tenantIdentifier + "]", e);
    }
    return connection;
  }

  @Override
  public void releaseConnection(String tenantIdentifier, Connection connection)
      throws SQLException {
    log.debug("  MultiTenantConnectionProviderImpl releaseConnection called ");
    try {
      connection.createStatement().execute("SET search_path TO " + QS_DEFAULT_TENANT_ID);
    } catch (SQLException e) {
      throw new HibernateException(
          "Could not alter JDBC connection to specified schema [" + tenantIdentifier + "]", e);
    }
    connection.close();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean isUnwrappableAs(Class unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    return null;
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return true;
  }
}
