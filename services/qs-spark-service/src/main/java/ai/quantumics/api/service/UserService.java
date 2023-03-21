/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.QsUser;

import java.sql.SQLException;

public interface UserService {

  QsUser findUserByEmail(final String email) throws SQLException;

  QsUser getUserById(final int userId) throws SQLException;

  boolean save(final QsUser user);
}
