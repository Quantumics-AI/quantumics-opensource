/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "QSP_Eng_Graph")
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class EngGraph {
  @Id
  @SequenceGenerator(
      name = "enggGraphSequence",
      sequenceName = "qspEngGraphSequence",
      initialValue = 1,
      allocationSize = 3)
  @GeneratedValue(generator = "enggGraphSequence")
  private int engGraphId;

  @Column(columnDefinition = "TEXT")
  private String content;
  @Column(columnDefinition = "TEXT")
  private String config;
  private int projectId;
  private int engFlowId;
  private int userId;
}
