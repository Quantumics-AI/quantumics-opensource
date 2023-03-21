/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "QSP_File")
public class QsFiles extends MetaData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int fileId;

  private int userId;
  private int projectId;
  private int folderId;
  private String fileName;
  private int fileVersionNum;
  private String fileSize;
  private String fileType;

  @Column(columnDefinition = "TEXT")
  private String qsMetaData;

  private String ruleCreatedFrom;
  private Date createdDate;
  private Date ModifiedDate;
  private boolean active;
  private String additionalInfo;
}
