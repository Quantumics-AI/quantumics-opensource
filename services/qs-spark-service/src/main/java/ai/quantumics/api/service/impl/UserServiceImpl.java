/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.QsUser;
import ai.quantumics.api.repo.UserRepository;
import ai.quantumics.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired private UserRepository repository;

    @Override
    public QsUser getUserById(int userId) {
        Optional<QsUser> findById = repository.findById(userId);
        return findById.get();
    }

    @Override
    public boolean save(QsUser user) {
        try {
            repository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public QsUser findUserByEmail(String email) throws SQLException {
        return repository.findByEmail(email);
    }
}
