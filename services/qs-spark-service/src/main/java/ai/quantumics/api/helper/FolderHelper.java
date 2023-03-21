package ai.quantumics.api.helper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsPartition;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.PartitionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class FolderHelper {
	@Autowired
	FileService fileService;
	@Autowired
	PartitionService partitionService;
	@Autowired
	AwsAdapter awsAdapter;
	@Autowired
	ControllerHelper controllerHelper;

	public void deleteFolderContents(Projects project, QsUserV2 qsUserV2, QsFolders qsFolders) {
		List<QsFiles> files = fileService.getFiles(project.getProjectId(), qsFolders.getFolderId());

		if (files != null && !files.isEmpty()) {
			try {
				files.stream().forEach((file) -> {
					try {
						Optional<QsPartition> partitionOp = partitionService
								.getPartitionByFileId(qsFolders.getFolderId(), file.getFileId());
						if (partitionOp.isPresent()) {
							QsPartition partition = partitionOp.get();
							String bucketName = project.getBucketName() + "-" + QsConstants.RAW;
							String objectKey = String.format("%s/%s/%s", qsFolders.getFolderName(),
									partition.getPartitionName(), file.getFileName());
							awsAdapter.deleteObject(bucketName, objectKey);
						}
					} catch (SQLException se) {
						log.error(se.getMessage());
					}
					file.setActive(false);
				});
				fileService.saveFilesCollection(files);
			} catch (SQLException se) {
				log.error(se.getMessage());
			}
		}
		try {
			awsAdapter.athenaDropTableQuery(project.getRawDb(), qsFolders.getFolderName().toLowerCase());
		} catch (AmazonClientException | InterruptedException e) {
			log.error(e.getMessage());
		}
		String userName = qsUserV2.getQsUserProfile().getUserFirstName() + " "
				+ qsUserV2.getQsUserProfile().getUserLastName();
		// Insert a record into the Notifications table..
		String auditMessage = controllerHelper.getAuditMessage(QsConstants.AUDIT_FOLDER_MSG,
				AuditEventTypeAction.DELETE.getAction().toLowerCase());
		String notification = controllerHelper.getNotificationMessage(QsConstants.AUDIT_FOLDER_MSG,
				AuditEventTypeAction.DELETE.getAction().toLowerCase());
		controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.DELETE,
				String.format(auditMessage, qsFolders.getFolderDisplayName()),
				String.format(notification, qsFolders.getFolderDisplayName(), userName), qsUserV2.getUserId(),
				project.getProjectId());
	}
}
