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
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "QS_Project")
@Data
@NamedStoredProcedureQuery(
    name = "createProjectSchemaAndTables", 
    procedureName = "create_project_schema_and_tables"
)
public class Projects {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int projectId;

  private int userId;
  private String rawDb;
  private String orgName;
  private Date createdDate;
  private String bucketName;
  private String rawCrawler;
  private String projectName;
  private String projectDesc;
  private String processedDb;
  private String dbSchemaName;
  private String projectOutcome;
  private String projectDataset;
  private String projectEngineer;
  private String projectAutomate;
  private String processedCrawler;
  private String projectDisplayName;
  private String createdBy;
  private String modifiedBy;
  private Date modifiedDate;
  private boolean active;
  
  private String engDb;
  private String engCrawler;
  private String projectLogo;
  
  //Deactivate
  private boolean isDeleted;
  private Date deletionDate;

  private boolean markAsDefault;
  
  //@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  //@JoinColumn(name = "project_id",referencedColumnName="projectId")
  //private Set<QsUserV2> users = new HashSet<>();
}
