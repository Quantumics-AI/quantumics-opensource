/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"folderId", "userId", "projectId", "folderName", "folderDisplayName","filesCount", "createdBy", "createDate", "modifiedDate", "latest"})
public class CleanseFolderResponse {
  @JsonProperty private int folderId;
  @JsonProperty private int userId;
  @JsonProperty private int projectId;
  @JsonProperty private String folderName;
  @JsonProperty private String folderDisplayName;
  @JsonProperty private String createdBy;
  @JsonProperty private Date createDate;
  @JsonProperty private Date modifiedDate;
  @JsonProperty private int filesCount;

  @JsonProperty("latest")
  private String latestFileName;
}
