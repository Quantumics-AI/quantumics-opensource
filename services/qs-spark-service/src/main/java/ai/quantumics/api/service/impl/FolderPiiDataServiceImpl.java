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
import org.springframework.stereotype.Service;

import ai.quantumics.api.model.QsFoldersPiiData;
import ai.quantumics.api.repo.FolderPiiDataRepository;
import ai.quantumics.api.service.FolderPiiDataService;

@Service
public class FolderPiiDataServiceImpl implements FolderPiiDataService {

	@Autowired
	private FolderPiiDataRepository folderPiiDataRepository;

	@Override
	public QsFoldersPiiData saveFoldersPiiData(QsFoldersPiiData folders) throws SQLException {
		return folderPiiDataRepository.saveAndFlush(folders);
	}
	
	@Override
	public List<QsFoldersPiiData> findByFolderId(int folderId) {
		return folderPiiDataRepository.findByFolderId(folderId);
	}
	
	@Override
	public Optional<QsFoldersPiiData> findByFolderIdAndActive(int folderId){
		return folderPiiDataRepository.findByFolderIdAndActive(folderId,true);
	}

}
