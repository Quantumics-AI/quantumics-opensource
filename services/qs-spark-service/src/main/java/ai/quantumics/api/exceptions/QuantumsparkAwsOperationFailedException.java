/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.exceptions;

public class QuantumsparkAwsOperationFailedException extends RuntimeException {

  /** */
  private static final long serialVersionUID = -971061793313868621L;

  public QuantumsparkAwsOperationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public QuantumsparkAwsOperationFailedException(String message) {
    super(message);
  }
}
