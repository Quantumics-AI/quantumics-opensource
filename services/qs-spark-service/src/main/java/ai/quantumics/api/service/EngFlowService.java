/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.EngFlow;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EngFlowService {

  EngFlow save(EngFlow engFlow) throws SQLException;

  EngFlow saveIfNotPresent(EngFlow engFlow) throws SQLException;

  List<EngFlow> getFlowsForProjectAndFlowName(int projectId, String flowName) throws SQLException;

  Optional<EngFlow> getFlowsForProjectAndId(int projectId, int engFlowId) throws SQLException;

  List<EngFlow> getFlowForUser(int userId, String flowName) throws SQLException;

  List<EngFlow> getFlowForUserFromFlow(int userId, int engFlowId) throws SQLException;

  List<EngFlow> getFlowNames(int projectId, int userId) throws SQLException;
  
  List<EngFlow> getFlowNames(int projectId) throws SQLException;

  List<EngFlow> getFlowNamesDescOrder(int projectId) throws SQLException;

  List<EngFlow> getAllActiveEngFlowsForProject(int projectId) throws SQLException;
  
  List<EngFlow> getChildEngFlows(int projectId, int parentEngFlowId) throws SQLException;
  
  List<EngFlow> getRecentChildEngFlow(int projectId, int parentEngFlowId) throws SQLException;

  void deleteEngFlow(int engFlowId) throws SQLException;

  void deleteEngFlow(String engFlowName) throws SQLException;

  void updateFlowStatus(int engFlowId) throws SQLException;
  
  boolean isExists(String engFlowName) throws SQLException;
}
