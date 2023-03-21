/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.misc;

import ai.quantumics.api.model.EngFlowEvent;

import java.util.Comparator;

public class SortByFlowEvent implements Comparator<EngFlowEvent> {

  @Override
  public int compare(EngFlowEvent event1, EngFlowEvent event2) {
    return event1.getEngFlowEventId() - event2.getEngFlowEventId();
  }
}
