/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.EngGraph;

import java.sql.SQLException;
import java.util.Optional;

public interface EngGraphService {

  Optional<EngGraph> getContent(int projectId, int userId) throws SQLException;

  Optional<EngGraph> getGraphXml(int engFlowId,int userId) throws SQLException;

  Optional<EngGraph> isGraphIdPresent(int engGraphId) throws SQLException;

  Optional<EngGraph> getRecordForEngFlowId(int projectId, int engFlowId);

  EngGraph save(EngGraph state) throws SQLException;

  void deleteGraph(int engGraphId) throws SQLException;
}
