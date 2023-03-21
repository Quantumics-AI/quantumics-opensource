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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "qsp_eng_flow_job")
public class EngFlowJob {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int flowJobId;

  private String status;
  private int engFlowId;
  private Date rundate;
  
  private int batchJobId;
  private String batchJobLog;
  private long batchJobDuration;
  private String autorunReqPayload;
  
}
