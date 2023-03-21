/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */
package ai.quantumics.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qs_audit_events")
public class AuditEvents {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int auditId;
  
  private int projectId;
  private int userId;
  private String eventType;
  private String eventTypeAction;
  private String auditMessage;
  private String notificationMessage;
  private String userName; 
  private Date creationDate;
  private boolean isNotify;
  private boolean active;
  private boolean isNotifyRead;
}
