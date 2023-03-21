/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.TestUriInfo;
import ai.quantumics.api.repo.TestUriInfoRepository;
import ai.quantumics.api.service.TestUriInfoService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class TestUriInfoServiceImpl implements TestUriInfoService {
  private final TestUriInfoRepository uriInfoRepo;

  public TestUriInfoServiceImpl(TestUriInfoRepository uriInfoRepoCi) throws SQLException {
    uriInfoRepo = uriInfoRepoCi;
  }

  @Override
  public List<TestUriInfo> getAll() throws SQLException {
    return uriInfoRepo.findAll();
  }

  @Override
  public TestUriInfo save(TestUriInfo testUriInfo) throws SQLException {
    return uriInfoRepo.save(testUriInfo);
  }

  @Override
  public Optional<TestUriInfo> getById(int id) throws SQLException {
    return uriInfoRepo.findById(id);
  }

  @Override
  public void delete(int id) throws SQLException {
    uriInfoRepo.deleteById(id);
  }
}
