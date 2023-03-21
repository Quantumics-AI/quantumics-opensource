/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package com.qs.api.service;

import java.sql.SQLException;
import java.util.List;
import com.qs.api.model.FileMetaDataAwsRef;

public interface FileMetaDataAwsService {

  List<FileMetaDataAwsRef> getDistinctFileDesc(int fileId) throws SQLException;

}
