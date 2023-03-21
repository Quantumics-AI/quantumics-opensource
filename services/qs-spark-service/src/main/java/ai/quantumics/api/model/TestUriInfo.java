/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "qs_testcasesendpointinfo")
public class TestUriInfo {
  @javax.persistence.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "endpoint")
  private String endPoint;

  @Column(name = "reqtype")
  private String reqType;

  @Column(columnDefinition = "TEXT")
  private String payload;

  @Column(name = "responsecode")
  private String responseCode;

  private String apiName;
}
