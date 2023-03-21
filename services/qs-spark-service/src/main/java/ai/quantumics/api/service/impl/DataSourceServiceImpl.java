/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.quantumics.api.model.ConnectorDetails;
import ai.quantumics.api.repo.DataSourceCredsRepo;
import ai.quantumics.api.service.DataSourceService;

@Service
public class DataSourceServiceImpl implements DataSourceService {

  private final DataSourceCredsRepo dataSourceRepo;

  public DataSourceServiceImpl(final DataSourceCredsRepo credsRepo) {
      dataSourceRepo = credsRepo;
  }

  @Override
  public Optional<ConnectorDetails> getById(final int id) throws SQLException {
    return dataSourceRepo.findByConnectorId(id);
  }
  
  @Override
  public List<ConnectorDetails> getByConnectorType(String connectorType) throws SQLException {
    return dataSourceRepo.findByConnectorType(connectorType);
  }

  @Override
  public ConnectorDetails save(final ConnectorDetails connector) throws SQLException {
    return dataSourceRepo.save(connector);
  }

  @Override
  public ConnectorDetails saveIfNotPresent(ConnectorDetails connector) throws SQLException {
	  ConnectorDetails savedRecord = null;
    Optional<ConnectorDetails> byUserIdAndDbSourceType =
        dataSourceRepo.findByConnectorNameIgnoreCaseAndActiveTrue(
        		connector.getConnectorName());
    if (byUserIdAndDbSourceType.isPresent()) {
      savedRecord = byUserIdAndDbSourceType.get();
    } else {
      savedRecord = dataSourceRepo.save(connector);
    }
    return savedRecord;
  }

  @Override
  public int countConnections(int userId) {
    return dataSourceRepo.countByCreatedBy(userId);
  }
}
