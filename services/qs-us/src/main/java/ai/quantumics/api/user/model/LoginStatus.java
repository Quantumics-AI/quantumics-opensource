/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginStatus {

  private String auth;
  private String message;
  private String token;
  private String refreshToken;
  private String userRole;

  @JsonProperty("user_id")
  private String userId;

  private String email;
  private String user;

  @JsonProperty("redash_key")
  private String redashKey;
  
  private boolean firstTimeLogin;
  private String userImageUrl;
  private String subscriptionType;
  private int noOfUsers;
  private String userType;
}
