/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.service.impl;

import ai.quantumics.api.user.model.QsFolders;
import ai.quantumics.api.user.repo.FolderRepository;
import ai.quantumics.api.user.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {

	@Autowired
	private FolderRepository folderRepo;

	@Override
	public QsFolders saveFolders(QsFolders folders) throws SQLException {
		return folderRepo.saveAndFlush(folders);
	}

	@Override
	public List<QsFolders> getFolders(int projectId) {
		List<QsFolders> folders = folderRepo.findByProjectIdAndActiveTrue(projectId);

		folders.stream().forEach((folder) -> {
			if (folder.getFolderDisplayName() == null || folder.getFolderDisplayName().isEmpty()) {
				folder.setFolderDisplayName(folder.getFolderName());
			}
		});

		return folders;
	}

	@Override
	public List<QsFolders> getFoldersInProjectForUser(int projectId, int userId) {
		List<QsFolders> folders = folderRepo.findByProjectIdAndUserIdAndActiveTrue(projectId, userId);

		folders.stream().forEach((folder) -> {
			if (folder.getFolderDisplayName() == null || folder.getFolderDisplayName().isEmpty()) {
				folder.setFolderDisplayName(folder.getFolderName());
			}
		});

		return folders;
	}

	@Override
	public List<QsFolders> getFoldersForUser(int userId) {
		return folderRepo.findByuserIdAndActiveTrue(userId);
	}

	@Override
	public QsFolders getFolder(int folderId) {
		return folderRepo.findByFolderIdAndActive(folderId, true);
	}

	@Override
	public Optional<QsFolders> isExists(String folderName) throws SQLException {
		return folderRepo.findByFolderNameEqualsAndActive(folderName, true);
	}

	@Override
	public int countFolders(int userId, int projectId) throws SQLException {
		return folderRepo.countByUserIdAndProjectId(userId, projectId);
	}

	@Override
	public List<QsFolders> allFolders(int userId, int projectId) throws SQLException {
		List<QsFolders> folders = folderRepo.findByUserIdAndProjectIdAndActiveOrderByCreatedDateDesc(userId, projectId,
				true);

		folders.stream().forEach((folder) -> {
			if (folder.getFolderDisplayName() != null && !folder.getFolderDisplayName().isEmpty()) {
				folder.setFolderName(folder.getFolderDisplayName());
			}
		});

		return folders;
	}

	@Override
	public List<QsFolders> allFolders(int userId, int projectId, boolean isExternal) throws SQLException {
		List<QsFolders> folders = folderRepo.findByUserIdAndProjectIdAndIsExternalAndActiveOrderByCreatedDateDesc(
				userId, projectId, isExternal, true);
		return folders;
	}

	@Override
	public List<QsFolders> saveFolderCollection(List<QsFolders> foldersList) throws SQLException {
		return folderRepo.saveAll(foldersList);
	}

	@Override
	public QsFolders getFolderByName(String folderName) {
		return folderRepo.findByFolderNameAndActiveTrue(folderName);
	}

	@Override
	public boolean isFolderNameAvailable(String folderdisplayName) throws SQLException {
		boolean available = false;
		List<QsFolders> folders = folderRepo.findByFolderDisplayNameIgnoreCaseOrFolderNameIgnoreCaseAndActive(
				folderdisplayName, folderdisplayName, true);
		Optional<QsFolders> folder = folders.stream().filter((folderP) -> folderP.isActive()).findFirst();
		available = folder.isPresent() && folder.get().isActive() ? false : true;
		return available;
	}

	@Override
	public QsFolders getActiveFolder(int folderId, int projectId, int userId) {
		return folderRepo.findByFolderIdAndProjectIdAndUserIdAndActiveTrue(folderId, projectId, userId);
	}

	@Override
	public List<QsFolders> getFoldersToDelete(List<Integer> projectIds) {
		return folderRepo.findByProjectIdIn(projectIds);
	}

	@Override
	public List<QsFolders> getFoldersToDelete(int projectId) {
		return folderRepo.findByProjectId(projectId);
	}

	@Override
	public boolean deleteFolders(List<QsFolders> folderToDelete) {
		try {
			folderRepo.deleteAll(folderToDelete);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
