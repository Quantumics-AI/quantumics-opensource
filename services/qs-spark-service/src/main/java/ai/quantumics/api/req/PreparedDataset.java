/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.req;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreparedDataset implements Serializable {
	/**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  private String athenaTable;
  private String tablePartition;
  private String fileType;
  private String fileMetaData;
  private Date createdDate;
  private String createdBy;
  private int fileId;
  private String fileName;
  private int folderId;
  private String folderName;
  private int projectId;
}
