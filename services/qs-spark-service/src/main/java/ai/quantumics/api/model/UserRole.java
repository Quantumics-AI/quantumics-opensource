/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Table(name = "QS_User_Project_role")
@Entity
public class UserRole {
  @Id
  @SequenceGenerator(
      name = "userRoleSequence",
      sequenceName = "qsUserRoleSequence",
      initialValue = 1,
      allocationSize = 5)
  @GeneratedValue(generator = "userRoleSequence")
  private int userRoleId;

  private String userRole;
  private int userId;
  private int projectId;
}
