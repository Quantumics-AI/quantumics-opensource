/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import ai.quantumics.api.model.QsHomeKPI;
import ai.quantumics.api.repo.HomeKPIRepository;
import ai.quantumics.api.service.HomeKPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class HomeKPIServiceImpl implements HomeKPIService {

    @Autowired
    private HomeKPIRepository homeKPIRepository;


    @Override
    public QsHomeKPI updateHomeKPI(QsHomeKPI qsHomeKPI) throws SQLException {
        return homeKPIRepository.saveAndFlush(qsHomeKPI);
    }

    @Override
    public QsHomeKPI getHomeKPIForUser(int userId) throws SQLException {
        return homeKPIRepository.findByUserId(userId);
    }
}
