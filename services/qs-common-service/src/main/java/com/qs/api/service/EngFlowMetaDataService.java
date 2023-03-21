/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.service;

import java.util.List;
import java.util.Optional;
import com.qs.api.model.EngFlowMetaDataAwsRef;

public interface EngFlowMetaDataService {

  Optional<EngFlowMetaDataAwsRef> getById(int engFlowAwsRefId);

  EngFlowMetaDataAwsRef getByFlowId(int engFlowId);
  
  List<EngFlowMetaDataAwsRef> getFlowsByAthenaTableName(String athenaTableName);
}
