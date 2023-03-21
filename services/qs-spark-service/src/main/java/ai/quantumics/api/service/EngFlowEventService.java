/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.EngFlowEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EngFlowEventService {

  EngFlowEvent save(EngFlowEvent engFlowEvent) throws SQLException;

  List<EngFlowEvent> getAllEventsData(int engFlowId) throws SQLException;

  List<EngFlowEvent> getAllEventsDataWithStatusTrue(boolean isPersist, int engFlowId)
      throws SQLException;

  Optional<EngFlowEvent> getFlowEvent(int engFlowEventId) throws SQLException;

  Optional<EngFlowEvent> getFlowEventForConfigId(int autoConfigEventId) throws SQLException;
  
  List<EngFlowEvent> getFlowEventForFileId(int engFlowId, int folderId, int fileId, String fileType) throws SQLException;
  
  Optional<EngFlowEvent> getFlowEventForEngFlowAndConfigId(int engFlowId, int autoConfigEventId) throws SQLException;
  
  Optional<EngFlowEvent> getFlowEventForEngFlowAndType(int engFlowId, String eventType) throws SQLException;

  List<EngFlowEvent> getAllTypesOfFlows(int engFlowId, String type) throws SQLException;
  
  List<EngFlowEvent> getAllEventsOfType(int engFlowId, List<String> types) throws SQLException;

  void deleteByEventId(int engFlowEventId) throws SQLException;

  void deleteEventsByStatus(int engFlowId, boolean isPersist) throws SQLException;

  void persistEventStatus(int engFlowId, boolean isPersist) throws SQLException;
  
  void persistDeletedEventStatus(int engFlowId, boolean isPersist) throws SQLException;
}
