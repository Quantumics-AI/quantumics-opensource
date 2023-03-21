/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "QS_User")
public class QsUser {
  @Id
  // @GeneratedValue(strategy = GenerationType.AUTO)
  @SequenceGenerator(
      name = "userSequence",
      sequenceName = "qsUserSequence",
      initialValue = 1,
      allocationSize = 3)
  @GeneratedValue(generator = "userSequence")
  private int userId;

  private String firstName;
  private String lastName;
  private String middleName;
  private String userName;
  private String company;
  private String department;
  private String role;
  private int dataPrepCount;

  @Column(unique = true)
  private String email;

  private String password;
  private String salt;
  private String redashKey;
  private String phone;
  private String country;
}
