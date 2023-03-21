/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.EngFlowEvent;
import ai.quantumics.api.repo.EngFlowEventRepository;
import ai.quantumics.api.service.EngFlowEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class EngFlowEventServiceImpl implements EngFlowEventService {
  private final EngFlowEventRepository engFlowEventRepo;

  public EngFlowEventServiceImpl(EngFlowEventRepository engFlowEventRepo) {
    this.engFlowEventRepo = engFlowEventRepo;
  }

  @Override
  public EngFlowEvent save(EngFlowEvent engFlowEvent) throws SQLException {
    return engFlowEventRepo.save(engFlowEvent);
  }

  @Override
  public List<EngFlowEvent> getAllEventsData(int engFlowId) throws SQLException {
    return engFlowEventRepo.findByEngFlowId(engFlowId);
  }

  @Override
  public List<EngFlowEvent> getAllEventsDataWithStatusTrue(boolean isPersist, int engFlowId)
      throws SQLException {
    return engFlowEventRepo.findByEventStatusAndEngFlowId(isPersist, engFlowId);
  }

  @Override
  public Optional<EngFlowEvent> getFlowEvent(int engFlowEventId) throws SQLException {
    return engFlowEventRepo.findById(engFlowEventId);
  }

  @Override
  public Optional<EngFlowEvent> getFlowEventForConfigId(int autoConfigEventId) throws SQLException {
    return engFlowEventRepo.findByAutoConfigEventId(autoConfigEventId);
  }
  
  @Override
  public List<EngFlowEvent> getFlowEventForFileId(int engFlowId, int folderId, int fileId, String fileType)
      throws SQLException {
    return engFlowEventRepo.findByEngFlowIdAndFolderIdAndFileIdAndFileType(engFlowId, folderId, fileId, fileType);
  }
  
  @Override
  public Optional<EngFlowEvent> getFlowEventForEngFlowAndConfigId(int engFlowId,
      int autoConfigEventId) throws SQLException {
    return engFlowEventRepo.findByEngFlowIdAndAutoConfigEventId(engFlowId, autoConfigEventId);
  }

  @Override
  public Optional<EngFlowEvent> getFlowEventForEngFlowAndType(int engFlowId, String eventType)
      throws SQLException {
    return engFlowEventRepo.findByEngFlowIdAndEventType(engFlowId, eventType);
  }

  @Override
  public List<EngFlowEvent> getAllTypesOfFlows(int engFlowId, String type) {
    return engFlowEventRepo.findByEngFlowIdAndAndEventType(engFlowId, type);
  }
  
  @Override
  public List<EngFlowEvent> getAllEventsOfType(int engFlowId, List<String> types) {
    return engFlowEventRepo.findByEngFlowIdAndEventStatusTrueAndEventTypeIn(engFlowId, types);
  }

  @Override
  public void deleteByEventId(int engFlowEventId) throws SQLException {
    engFlowEventRepo.deleteById(engFlowEventId);
  }

  @Override
  @Transactional
  public void deleteEventsByStatus(int engFlowId, boolean isPersist) throws SQLException {
    engFlowEventRepo.deleteEventStatus(engFlowId, isPersist);
  }

  @Override
  @Transactional
  public void persistEventStatus(int engFlowId, boolean isPersist) {
    engFlowEventRepo.updateEventStatus(engFlowId, isPersist);
  }
  
  @Override
  @Transactional
  public void persistDeletedEventStatus(int engFlowId, boolean isPersist) throws SQLException {
    engFlowEventRepo.updateDeletedEventStatus(engFlowId, isPersist, true);
  }
}
