/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.service.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.qs.api.model.EngFlowMetaDataAwsRef;
import com.qs.api.repo.EngFlowMetaDataRepository;
import com.qs.api.service.EngFlowMetaDataService;

@Service
public class EngFlowMetaDataServiceImpl implements EngFlowMetaDataService {

  private final EngFlowMetaDataRepository engFlowMetaDataRepository;

  public EngFlowMetaDataServiceImpl(EngFlowMetaDataRepository engFlowMetaDataRepositoryCi) {
    engFlowMetaDataRepository = engFlowMetaDataRepositoryCi;
  }

  @Override
  public Optional<EngFlowMetaDataAwsRef> getById(int engFlowAwsRefId) {
    return engFlowMetaDataRepository.findById(engFlowAwsRefId);
  }

  @Override
  public EngFlowMetaDataAwsRef getByFlowId(int engFlowId) {
    return engFlowMetaDataRepository.findByEngFlowId(engFlowId);
  }
  
  @Override
  public List<EngFlowMetaDataAwsRef> getFlowsByAthenaTableName(String athenaTable) {
    return engFlowMetaDataRepository.findByAthenaTable(athenaTable);
  }
  
}
