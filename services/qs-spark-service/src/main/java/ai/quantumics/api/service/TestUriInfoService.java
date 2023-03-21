/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import ai.quantumics.api.model.TestUriInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TestUriInfoService {

  List<TestUriInfo> getAll() throws SQLException;

  TestUriInfo save(TestUriInfo testUriInfo) throws SQLException;

  Optional<TestUriInfo> getById(int id) throws SQLException;

  void delete(int id) throws SQLException;
}
