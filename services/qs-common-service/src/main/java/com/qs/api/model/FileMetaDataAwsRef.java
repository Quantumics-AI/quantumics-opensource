/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.model;

import java.util.Date;
import javax.persistence.Column;
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
@Table(name = "qsp_filemetadata_awsref")
public class FileMetaDataAwsRef {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long awsRefId;

  private int fileId;
  private String fileType;

  private String athenaTable;
  private String tablePartition;
  private Date createdDate;
  private String createdBy;

  @Column(columnDefinition = "TEXT")
  private String fileMetaData;
}
