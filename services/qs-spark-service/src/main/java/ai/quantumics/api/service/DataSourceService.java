/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ai.quantumics.api.model.ConnectorDetails;

public interface DataSourceService {

  Optional<ConnectorDetails> getById(int id) throws SQLException;
  
  List<ConnectorDetails> getByConnectorType(String connectorType) throws SQLException;

  ConnectorDetails save(ConnectorDetails connector) throws SQLException;

  ConnectorDetails saveIfNotPresent(ConnectorDetails connector) throws SQLException;

  int countConnections(int userId);
}
