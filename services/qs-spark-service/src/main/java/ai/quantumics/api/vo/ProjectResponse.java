/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.vo;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectResponse {
  private int projectId;
  private String createdBy;
  private Date createdDate;
  private String projectName;
  private String projectDesc;
  private String projectDisplayName;
  private List<String> projectMembers;
  private String projectLogo;
  private String subscriptionType;
  private int validDays;
  private String subscriptionStatus;
  private String subscriptionPlanType;
  private int subscriptionPlanTypeId;
  private boolean active;
  @JsonProperty("isDeleted")
  private boolean deleted;
  private boolean markAsDefault;
 
}
