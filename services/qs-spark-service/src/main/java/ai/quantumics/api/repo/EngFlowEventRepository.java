/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import ai.quantumics.api.model.EngFlowEvent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngFlowEventRepository extends CrudRepository<EngFlowEvent, Integer> {

  List<EngFlowEvent> findByEngFlowId(int engFlowId);

  Optional<EngFlowEvent> findByAutoConfigEventId(int autoConfigEventId);
  
  Optional<EngFlowEvent> findByEngFlowIdAndAutoConfigEventId(int engFlowId, int autoConfigEventId);
  
  Optional<EngFlowEvent> findByEngFlowIdAndEventType(int engFlowId, String eventType);
  
  List<EngFlowEvent> findByEngFlowIdAndFolderIdAndFileIdAndFileType(int engFlowId, int folderId, int fileId, String fileType);

  List<EngFlowEvent> findByEngFlowIdAndAndEventType(int engFlowId, String type);
  
  List<EngFlowEvent> findByEngFlowIdAndEventStatusTrueAndEventTypeIn(int engFlowId, List<String> types);

  List<EngFlowEvent> findByEventStatusAndEngFlowId(boolean isPersist, int engFlowId);

  @Modifying
  @Query("update EngFlowEvent e set e.eventStatus = :isPersist WHERE e.engFlowId = :engFlowId")
  void updateEventStatus(@Param("engFlowId") int engFlowId, @Param("isPersist") boolean isPersist);
  
  @Modifying
  @Query("update EngFlowEvent e set e.eventStatus = :isPersist WHERE e.engFlowId = :engFlowId AND e.deleted = :isDelete")
  void updateDeletedEventStatus(@Param("engFlowId") int engFlowId, @Param("isPersist") boolean isPersist, @Param("isDelete") boolean isDelete);

  @Modifying
  @Query("delete from EngFlowEvent e WHERE e.engFlowId = :engFlowId AND e.eventStatus = :isPersist")
  void deleteEventStatus(@Param("engFlowId") int engFlowId, @Param("isPersist") boolean isPersist);
}
