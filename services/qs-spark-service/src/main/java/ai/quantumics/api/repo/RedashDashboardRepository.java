/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import ai.quantumics.api.model.QsRedashDashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedashDashboardRepository extends JpaRepository<QsRedashDashboard, Integer> {

    List<QsRedashDashboard> findAllByActiveTrue();

    List<QsRedashDashboard> findByRdKeyAndActiveTrue(String rdKey);

    QsRedashDashboard findByDashboardIdAndRdKeyAndActiveTrue(int dashboardId, String rdKey);
}
