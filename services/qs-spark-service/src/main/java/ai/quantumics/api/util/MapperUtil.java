/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.util;

import ai.quantumics.api.model.Projects;
import ai.quantumics.api.vo.ProjectResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperUtil {
  private final ModelMapper modelMapper = new ModelMapper();

  public List<ProjectResponse> mapAll(List<Projects> projects, String userName, String userSubscriptionType, int validDays) {
    List<ProjectResponse> map =
        modelMapper.map(projects, new TypeToken<List<ProjectResponse>>() {}.getType());
    map.forEach(
        projectResponse -> {
          projectResponse.setCreatedBy(userName);
          projectResponse.setSubscriptionType(userSubscriptionType);
          projectResponse.setValidDays(validDays);
        });
    return map;
  }

  public ProjectResponse mapObject(Projects projects, String userName) {
    ProjectResponse map = modelMapper.map(projects, ProjectResponse.class);
    map.setCreatedBy(userName);
    return map;
  }

  private <D, T> D map(final T entity, Class<D> outClass) {
    return modelMapper.map(entity, outClass);
  }
}
