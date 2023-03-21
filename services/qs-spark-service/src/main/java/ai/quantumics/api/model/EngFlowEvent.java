/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qsp_eng_flow_event")
public class EngFlowEvent {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int engFlowEventId;

  @Column(columnDefinition = "TEXT")
  private String engFlowEventData;

  @Column(columnDefinition = "TEXT")
  private String engFlowConfig;

  private String engFlowS3Location;

  @Column(columnDefinition = "TEXT")
  private String fileMetaData;

  @Column(columnDefinition = "TEXT")
  private String joinOperations;

  private String eventType;
  private int engFlowId;
  private int autoConfigEventId;
  private boolean eventStatus;
  private double eventProgress;
  
  private String livyStmtExecutionStatus;
  private String livyStmtOutput;
  private Date livyStmtStartTime;
  private Date livyStmtEndTime;
  private long livyStmtDuration;
  
  private int projectId;
  private int folderId;
  private int fileId;
  private int userId;
  private Date creationDate;
  private String fileType;
  private boolean deleted;
}
