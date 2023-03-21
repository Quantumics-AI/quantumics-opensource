/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.helper;

import ai.quantumics.api.user.exceptions.QuantumsparkUserNotFound;
import ai.quantumics.api.user.exceptions.SchemaChangeException;
import ai.quantumics.api.user.model.Projects;
import ai.quantumics.api.user.model.QsUserProfileV2;
import ai.quantumics.api.user.model.QsUserV2;
import ai.quantumics.api.user.req.*;
import ai.quantumics.api.user.service.ProjectService;
import ai.quantumics.api.user.service.UserServiceV2;
import ai.quantumics.api.user.util.CipherUtils;
import ai.quantumics.api.user.util.DbSessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONValue;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

import static ai.quantumics.api.user.constants.QsConstants.*;

@Slf4j
@Component
public class ControllerHelper {
	private static final Gson gson = new Gson();

	private final DbSessionUtil dbUtil;
	private final UserServiceV2 userService;
	// private final LivyActions livyActions;
	private final ProjectService projectService;
	// private final EngFlowEventService engFlowEventService;
	// private final SimpMessageSendingOperations messagingTemplate;
	// private final MetadataHelper metadataHelper;
	// private final EngFlowJobService flowJobService;
	private final Environment applicationProperties;
	// private final EngFlowService engFlowService;
	// private final EngFlowDatalineageService datalineageService;

	public ControllerHelper(DbSessionUtil dbUtilCi, ProjectService projectServiceCi, UserServiceV2 userServiceCi, Environment applicationPropertiesCi) {
		dbUtil = dbUtilCi;
		projectService = projectServiceCi;
		userService = userServiceCi;
		applicationProperties = applicationPropertiesCi;
	}

	private static String joinListWithCustomDelimiter(final ArrayList<String> tempData) {
		return String.join("','", tempData).toLowerCase();
	}

	public String getProjectAndChangeSchema(int projectId) throws SchemaChangeException {
		dbUtil.changeSchema("public");
		final Projects project = projectService.getProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		return project.getDbSchemaName();
	}

	public void swtichToPublicSchema() throws SchemaChangeException {
		dbUtil.changeSchema(PUBLIC);
	}

	public Projects getProjects(int projectId) throws SchemaChangeException {
		dbUtil.changeSchema("public");
		final Projects project = projectService.getProject(projectId);
		dbUtil.changeSchema(project.getDbSchemaName());
		return project;
	}

	public Projects getProjects(int projectId, int userId) throws SchemaChangeException {
		dbUtil.changeSchema("public");
		final Projects project = projectService.getProject(projectId, userId);
		dbUtil.changeSchema(project.getDbSchemaName());
		return project;
	}

	public String getUserName(int userId, String schema) throws SchemaChangeException, SQLException {
		dbUtil.changeSchema("public");
		QsUserV2 userById = userService.getUserById(userId);
		dbUtil.changeSchema(schema);
		QsUserProfileV2 userProfile = userById.getQsUserProfile();
		return getFullName(userProfile);
	}

	/**
	 * @param userById
	 * @return
	 */
	public String getFullName(QsUserProfileV2 userProfile) {
		return (userProfile != null) ? getFullName(userProfile.getUserFirstName(), userProfile.getUserLastName()) : "";
	}

	/* only for public schema */
	public String getUserName(int userId) throws SchemaChangeException, SQLException {
		dbUtil.changeSchema("public");
		QsUserV2 userById = userService.getUserById(userId);
		QsUserProfileV2 userProfile = userById.getQsUserProfile();
		return (userProfile != null) ? getFullName(userProfile.getUserFirstName(), userProfile.getUserLastName()) : "";
	}

	public HashMap<String, Object> failureMessage(final int code, final String message) {
		final HashMap<String, Object> success = new HashMap<>();
		success.put("code", code);
		success.put("message", message);
		return success;
	}

	public HashMap<String, Object> createSuccessMessage(final int code, final String message, Object result) {
		final HashMap<String, Object> success = new HashMap<>();
		success.put("code", code);
		success.put("result", result);
		success.put("message", message);

		return success;
	}

	public static String buildCamelCase(String type) {
		if (Objects.isNull(type) || type.isEmpty())
			return type;

		String[] tokens = type.split("\\s");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			char capLetter = Character.toUpperCase(tokens[i].charAt(0));
			if (i != 0)
				builder.append(" ");
			builder.append(capLetter + tokens[i].substring(1));
		}
		return builder.toString();
	}

	public JsonNode readableJson(JsonElement activity) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		JsonNode readTree = null;

		if (activity != null) {
			final String atr = JSONValue.parse(activity.toString()).toString();
			readTree = mapper.readTree(atr);
		}

		return readTree;
	}

	private String getAggregateMetadata(int eventId, JsonNode readTree) throws Exception {
		// Since the first node is empty, to get the Column Metadata, use the second
		// node..
		JsonNode firstNode = readTree.get(1);
		if (firstNode == null) {
			return "";
		}

		FileJoinColumnMetadata aggregateMetadata = new FileJoinColumnMetadata();
		aggregateMetadata.setEventId(eventId);

		List<Metadata> metadataList = new ArrayList<>();
		final ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> values = mapper.readValue(firstNode.toString(), Map.class);
		if (values != null && !values.isEmpty()) {
			for (String key : values.keySet()) {
				Metadata metadata = new Metadata();
				metadata.setKey(key);
				metadata.setValue(key.substring(key.lastIndexOf("_") + 1));

				metadataList.add(metadata);
			}
		}
		aggregateMetadata.setMetadata(metadataList);

		return gson.toJson(aggregateMetadata);
	}

	private String prepareAggregateQuery(AggregateRequest aggregateRequest) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("df").append(aggregateRequest.getEventId()).append("=").append("df")
				.append(aggregateRequest.getDataFrameEventId()).append(".groupBy('")
				.append(joinListWithCustomDelimiter((ArrayList<String>) aggregateRequest.getGroupByColumns()))
				.append("')").append(".agg(");
		List<AggReqColumn> columns = aggregateRequest.getColumns();
		for (AggReqColumn column : columns) {
			if (column.getOpName() != null) {
				queryBuilder.append("f.").append(column.getOpName().toLowerCase()).append("('")
						.append(column.getColumnName().toLowerCase()).append("')").append(".alias").append("('")
						.append(column.getAliasName().toLowerCase()).append("'),");
			}
		}
		int i = queryBuilder.lastIndexOf(",");
		queryBuilder.deleteCharAt(i);
		queryBuilder.append(")");
		return queryBuilder.toString();
	}

	public void readLinesFromTemplate(StringBuilder fileContents) throws Exception {
		File contentSource = ResourceUtils.getFile(String.format("classpath:%s", QS_LIVY_TEMPLATE_ENG_NAME));
		log.info("File in classpath Found {} : ", contentSource.exists());
		fileContents.append(new String(Files.readAllBytes(contentSource.toPath())));
	}

	public void etlScriptVarsInit(StringBuilder fileContents, String engDb) {
		fileContents.toString().replace(DB_NAME, String.format("'%s'", engDb));
	}

	public String getFullName(String firstName, String lastName) {
		StringBuffer fullName = new StringBuffer(firstName);
		if (lastName != null) {
			fullName.append(" ");
			fullName.append(lastName);
		}
		return fullName.toString();
	}


	/**
	 * @param originalFileName
	 */
	public String stripFileNameExtn(String originalFileName) {
		int periodPointer = originalFileName.lastIndexOf(".");
		return originalFileName.substring(0, periodPointer);
	}

	/**
	 * @param originalFileName
	 */
	public String getFileNameExtn(String originalFileName) {
		int periodPointer = originalFileName.lastIndexOf(".");
		return originalFileName.substring(periodPointer + 1);
	}

	public void dropColumnsFromCsvFile(List<Object> csvLines, String dropColumnsStr) {
		if (dropColumnsStr.isEmpty())
			return;

		log.info("Columns to be dropped from the CSV file are: {}", dropColumnsStr);
		String[] columns = dropColumnsStr.split(",");

		for (Object obj : csvLines) {
			Map<String, String> values = (Map<String, String>) obj;

			for (String column : columns) {
				values.remove(column);
			}
		}

		log.info("Dropped columns '{}' from the CSV file.", dropColumnsStr);
	}

	public void encryptColumnsOfCsvFile(List<Object> csvLines, String encryptColumnsStr) {
		if (encryptColumnsStr.isEmpty())
			return;

		log.info("Columns to be encrypted in the CSV file are: {}", encryptColumnsStr);
		String[] columns = encryptColumnsStr.split(",");

		for (Object obj : csvLines) {
			Map<String, String> values = (Map<String, String>) obj;

			for (String column : columns) {
				values.put(column, CipherUtils.encrypt(values.get(column)));
			}
		}

		log.info("Encrypted columns '{}' in the CSV file.", encryptColumnsStr);
	}

	public void decryptColumnsOfCsvFile(List<Object> csvLines, String decryptColumnsStr) {
		if (decryptColumnsStr.isEmpty())
			return;

		log.info("Columns to be decrypted in the CSV file are: {}", decryptColumnsStr);
		String[] columns = decryptColumnsStr.split(",");

		for (Object obj : csvLines) {
			Map<String, String> values = (Map<String, String>) obj;

			for (String column : columns) {
				values.put(column.trim(), CipherUtils.decrypt(values.get(column.trim())));
			}
		}

		log.info("Decrypted columns '{}' in the CSV file.", decryptColumnsStr);
	}

	public void processFileBeforeUpload(UploadFileRequest uploadFileRequest, File csvFile, File csvFileTmp)
			throws IOException {
		BufferedWriter bw = null;
		CsvMapper csvMapper = new CsvMapper();
		List<Object> readAllLines = null;
		try {
			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			readAllLines = csvMapper.readerFor(Map.class).with(csvSchema)
					.readValues(new BufferedReader(new FileReader(csvFile))).readAll();

			if (uploadFileRequest.getDropColumns() != null && uploadFileRequest.getEncryptPiiColumns() != null) {
				String dropColumns = uploadFileRequest.getDropColumns();
				// Drop the columns from the CSV file here..
				dropColumnsFromCsvFile(readAllLines, dropColumns);

				String encryptPiiColumns = uploadFileRequest.getEncryptPiiColumns();
				// Encrypt the columns here..
				encryptColumnsOfCsvFile(readAllLines, encryptPiiColumns);
			}

			// Finally write the list of lines back to the CSV file and then upload...
			CsvSchema.Builder schemaBuilder = CsvSchema.builder();
			CsvSchema schema = null;
			if (readAllLines != null && !readAllLines.isEmpty()) {
				Object obj = readAllLines.get(0);
				Map<String, String> firstItem = (Map<String, String>) obj;
				for (String col : firstItem.keySet()) {
					schemaBuilder.addColumn(col);
				}
				schema = schemaBuilder.build().withLineSeparator("\r").withHeader();
				bw = new BufferedWriter(new FileWriter(csvFileTmp));
				csvMapper.writer(schema).writeValues(bw).writeAll(readAllLines);
			}

			bw.flush();
		} catch (IOException e) {
			log.error("Exception occurred while processing the CSV file..{}", e.getMessage());

			throw new IOException("Exception occurred while processing the CSV file..");
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException ioe) {
				// do nothing...
			}
		}
	}

	public boolean isAdmin(int userId) throws QuantumsparkUserNotFound {
		boolean admin = false;
		try {
			QsUserV2 qsUserV2 = userService.getActiveUserById(userId);
			if (qsUserV2 == null) {
				throw new QuantumsparkUserNotFound("User not found");
			} else {
				admin = "Admin".equalsIgnoreCase(qsUserV2.getUserRole()) ? true : false;
			}
		} catch (SQLException sqlException) {
			throw new QuantumsparkUserNotFound(
					"Unable to fetch user details from the database. Please contact Technical support", sqlException);
		}
		return admin;
	}

	public boolean isActiveUser(int userId) {
		boolean active = false;
		try {
			QsUserV2 qsUserV2 = userService.getUserById(userId);
			if (qsUserV2 != null && qsUserV2.isActive()) {
				active = true;
			}
		} catch (SQLException sqlException) {
			throw new QuantumsparkUserNotFound(
					"Unable to fetch user details from the database. Please contact Technical support", sqlException);
		}
		return active;
	}

}
