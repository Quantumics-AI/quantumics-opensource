/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.qs.api.model.EngFlowMetaDataAwsRef;

@Repository
public interface EngFlowMetaDataRepository extends JpaRepository<EngFlowMetaDataAwsRef, Integer> {

    EngFlowMetaDataAwsRef findByEngFlowId(int engFlowId);
    
    List<EngFlowMetaDataAwsRef> findByAthenaTable(String athenaTable);

}
