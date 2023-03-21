/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.repo;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ai.quantumics.api.model.CleansingParam;

public interface CleansingRuleParamRepository extends JpaRepository<CleansingParam, Integer> {

  List<CleansingParam> findByFileId(int fileId);

  List<CleansingParam> findByFolderIdAndFileIdAndActiveTrue(int folderId, int fileId);

  List<CleansingParam> findByFileIdAndRuleSequenceGreaterThanAndActiveTrue(int fileId, int ruleSequence);

  List<CleansingParam> findByFolderIdAndFileIdAndActiveOrderByRuleSequence(int folderId, int fileId, boolean active);
  
  List<CleansingParam> findByActiveTrue(Sort sort);
  
}
