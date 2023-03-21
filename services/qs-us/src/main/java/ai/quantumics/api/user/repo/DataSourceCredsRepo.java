/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.quantumics.api.user.model.ConnectorDetails;

public interface DataSourceCredsRepo extends JpaRepository<ConnectorDetails, Integer> {

  Optional<ConnectorDetails> findByConnectorNameIgnoreCaseAndActiveTrue(String connectorName);
  
  Optional<ConnectorDetails> findByConnectorId(int connectorId);
  
  List<ConnectorDetails> findByConnectorType(String connectorType);

  int countByCreatedBy(int createdBy);
}
