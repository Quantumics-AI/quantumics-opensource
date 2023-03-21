/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "qsp_run_job_status")
public class RunJobStatus {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int runJobId;

  private int userId;
  private int projectId;
  private String awsCreateJobId;
  private String awsRunJobId;
  private String status;
  private Date createdDate;
  private int fileId;
  private int folderId;
}
