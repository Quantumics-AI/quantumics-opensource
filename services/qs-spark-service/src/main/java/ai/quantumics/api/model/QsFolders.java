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
import javax.persistence.Transient;

import lombok.Data;

@Entity
@Table(name = "QSP_Folder")
@Data
public class QsFolders {
  /*
   * @Id
   * 
   * @SequenceGenerator( name = "qspFolderSequence", sequenceName = "qspFolderSequence",
   * initialValue = 1, allocationSize = 7)
   * 
   * @GeneratedValue(generator = "qspFolderSequence")
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int folderId;

  private int userId;
  private int projectId;
  private int fileId;
  private String folderName;
  private String folderDesc;
  private String dataOwner;
  private String dataPrepFreq;
  private String cleansingRuleSetName;
  private String createdBy;
  private String modifiedBy;
  private Date createdDate;
  private Date modifiedDate;
  private int parentId;
  private boolean isExternal;
  private String columnHeaders;
  private boolean active;
  private String folderDisplayName;
  
  @Transient
  private int filesCount;
  
  @Transient
  private String pipelineName;
  
  @Transient
  private String pipelineType;

  /** This method displays the folder display name. if it is null, the folder name is displayed.
   * @return folderDisplayName
   */
  
  /*public String getFolderDisplayName() {    
    if(folderDisplayName == null || folderDisplayName.isEmpty()) {
      folderDisplayName = folderName;
    }
    return folderDisplayName;
  }
  
  public String getFolderName() {    
    if(folderDisplayName != null && !folderDisplayName.isEmpty()) {
      folderName = folderDisplayName;
    }
    return folderName;
  }*/

}
