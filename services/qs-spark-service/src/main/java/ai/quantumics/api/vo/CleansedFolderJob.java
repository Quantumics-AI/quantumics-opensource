/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
public class CleansedFolderJob {
  private int folderId;
  private int fileId;
  private int userId;
  private int projectId;
  private int jobId;
  private String folderName;
  private String lastJobStatus;
  private Date lastExecutionDate;
  private Date createdDate;
  private String dataOwner;
}
