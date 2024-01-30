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
import lombok.*;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;
import java.util.Date;

@Data
public class CleansedJobResponse implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

      public CleansedJobResponse(){}

    public CleansedJobResponse(int folderId, int userId, int projectId, String folderName, String folderDisplayName, String createdBy, Date createdDate,   String modifiedBy, Date modifiedDate,  boolean isExternal, String dataOwner,String jobStatus, Date jobCreatedDate) {
        this.folderId = folderId;
        this.userId = userId;
        this.projectId = projectId;
        this.folderName = folderName;
        this.folderDisplayName = folderDisplayName;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.dataOwner = dataOwner;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
        this.jobCreatedDate = jobCreatedDate;
        this.jobStatus = jobStatus;
        this.isExternal = isExternal;
    }

     private int folderId;
      private int userId;
      private int projectId;
      private String folderName;
      private String folderDisplayName;
      private String createdBy;
      private Date createdDate;
      private String dataOwner;
      private String modifiedBy;
      private Date modifiedDate;
      private Date jobCreatedDate;
      private String jobStatus;
      private boolean isExternal;
}
