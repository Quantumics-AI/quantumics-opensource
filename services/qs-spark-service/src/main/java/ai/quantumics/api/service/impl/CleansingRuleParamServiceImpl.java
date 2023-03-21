/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ai.quantumics.api.model.CleansingParam;
import ai.quantumics.api.repo.CleansingRuleParamRepository;
import ai.quantumics.api.service.CleansingRuleParamService;

@Service
public class CleansingRuleParamServiceImpl implements CleansingRuleParamService {
  @Autowired private CleansingRuleParamRepository CleanseRuleParamRepo;

  @Override
  public CleansingParam saveRuleWithParam(CleansingParam cleanseRule)throws SQLException {
    return CleanseRuleParamRepo.saveAndFlush(cleanseRule);
  }

  @Override
  public List<CleansingParam> saveAll(List<CleansingParam> cleanseRulesList) {
    return CleanseRuleParamRepo.saveAll(cleanseRulesList);
  }

  @Override
  public List<CleansingParam> getRulesCatalogue(int fileId) {
    return CleanseRuleParamRepo.findByFileId(fileId);
  }

  @Override
  public Optional<CleansingParam> getRuleById(int cleansingParamId) {
    return CleanseRuleParamRepo.findById(cleansingParamId);
  }

  @Override
  public List<CleansingParam> getRulesInfo(int folderId, int fileId) {
    return CleanseRuleParamRepo.findByFolderIdAndFileIdAndActiveTrue(folderId, fileId);
  }

  @Override
  public void deleteRule(int cleansingParamId) {
    CleanseRuleParamRepo.deleteById(cleansingParamId);
  }

  @Override
  public List<CleansingParam> getSeqGreaterThan(int fileId, int sequenceId) {
    return CleanseRuleParamRepo.findByFileIdAndRuleSequenceGreaterThanAndActiveTrue(fileId, sequenceId);
  }

  @Override
  public List<CleansingParam> getRulesOrderByRuleSequence(int folderId, int fileId) {
    return CleanseRuleParamRepo.findByFolderIdAndFileIdAndActiveOrderByRuleSequence(folderId, fileId, true);
  }
  
  @Override
  public List<CleansingParam> getRulesOrderByModifiedDateDesc() {
    return CleanseRuleParamRepo.findByActiveTrue(Sort.by("modifiedDate").descending());
  }
}
