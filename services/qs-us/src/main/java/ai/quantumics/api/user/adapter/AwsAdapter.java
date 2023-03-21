/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.adapter;

import ai.quantumics.api.user.constants.QsConstants;
import ai.quantumics.api.user.exceptions.QsRecordNotFoundException;
import ai.quantumics.api.user.model.*;
import ai.quantumics.api.user.req.ColumnMetaData;
import ai.quantumics.api.user.req.UploadFileRequest;
import ai.quantumics.api.user.service.*;
import ai.quantumics.api.user.util.*;
import ai.quantumics.api.user.vo.QsFileContent;
import ai.quantumics.api.user.vo.S3FileUploadResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.model.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ai.quantumics.api.user.constants.QsConstants.*;

@Slf4j
@Component
public class AwsAdapter {

	private static final String S3proto = "s3://";
	private static final String hyphen = "-";
	private static final String path_sep = "/";
	private final DbSessionUtil dbUtil;
	private final AmazonS3 amazonS3Client;
	private final MetadataHelper fileHelper;
	private final AmazonAthena athenaClient;
	private final RunJobService runJobService;
	private final FileMetaDataAwsService awsDataService;
	private final PartitionService partitionService;
	private final FileService fileService;
	private final ProjectService projectService;
	private final ProjectCumulativeSizeService projectSizeService;

	@Autowired private QsUtil qsUtil;

	@Value("${qs.athena.query.output}")
	private String qsAthenaOpBucket;

	@Value("${qs.athena.sleep}")
	private int qsAthenaSleepTimer;

	@Value("${qs.athena.query.limit}")
	private int qsQueryLimit;


	@Value("${s3.credentials.accessKey}")
	private String s3AccessKey;

	@Value("${s3.credentials.secretKey}")
	private String s3SecretKey;

	public AwsAdapter(
			AmazonS3 amazonS3ClientCi,
			MetadataHelper fileHelperCi,
			AmazonAthena athenaClientCi,
			RunJobService runJobServiceCi,
			DbSessionUtil dbUtilCi,
			FileMetaDataAwsService awsDataServiceCi,
			PartitionService partitionServiceCi,
			FileService fileServiceCi,
			ProjectService projectServiceCi,
			ProjectCumulativeSizeService projectSizeServiceCi
			) {
		amazonS3Client = amazonS3ClientCi;
		fileHelper = fileHelperCi;
		athenaClient = athenaClientCi;
		runJobService = runJobServiceCi;
		dbUtil = dbUtilCi;
		awsDataService = awsDataServiceCi;
		partitionService = partitionServiceCi;
		fileService = fileServiceCi;
		projectService = projectServiceCi;
		projectSizeService = projectSizeServiceCi;
	}

	private String athenaPrepareQuery(
			final QueryExecutionContext queryContext, final String queryString) {

		final StartQueryExecutionRequest queryExecutionRequest = new StartQueryExecutionRequest();
		final ResultConfiguration resultConfiguration = new ResultConfiguration();
		resultConfiguration.setOutputLocation(qsAthenaOpBucket);
		queryExecutionRequest.setQueryString(queryString);
		queryExecutionRequest.setQueryExecutionContext(queryContext);
		queryExecutionRequest.setResultConfiguration(resultConfiguration);

		StartQueryExecutionResult startQueryExecutionResult;
		startQueryExecutionResult = athenaClient.startQueryExecution(queryExecutionRequest);
		final String queryExecutionId = startQueryExecutionResult.getQueryExecutionId();

		log.info(
				"RequestId - {} and Query executionId - {}",
				startQueryExecutionResult.getSdkResponseMetadata().getRequestId(),
				queryExecutionId);

		return queryExecutionId;
	}

	private GetQueryResultsResult athenaRunQuery(final String queryExecutionId)
			throws InterruptedException {
		boolean isQueryStillRunning = true;
		final GetQueryResultsRequest getQueryResultsRequest = new GetQueryResultsRequest();
		getQueryResultsRequest.setQueryExecutionId(queryExecutionId);
		final GetQueryExecutionRequest getQueryExecutionRequest = new GetQueryExecutionRequest();
		getQueryExecutionRequest.setQueryExecutionId(queryExecutionId);
		GetQueryExecutionResult queryExecutionResponse;
		String failureReason;
		String queryState;
		while (isQueryStillRunning) {
			queryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
			queryState = queryExecutionResponse.getQueryExecution().getStatus().getState();
			log.debug("Current query state - {} ", queryState);

			if (queryState.equals(QueryExecutionState.FAILED.toString())) {
				failureReason =
						queryExecutionResponse.getQueryExecution().getStatus().getStateChangeReason();
				log.error("Athena Query Failed ! {}", failureReason);
				throw new RuntimeException("Athena Query Failed !" + failureReason);
			} else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
				throw new RuntimeException("Query was cancelled.");
			} else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
				isQueryStillRunning = false;
			} else {
				Thread.sleep(qsAthenaSleepTimer); // can be configured to increase query performance
			}
			log.info("Check Query Status : {}", queryState);
		}

		final GetQueryResultsRequest alterQueryResultsRequest = getQueryResultsRequest;
		alterQueryResultsRequest.setQueryExecutionId(queryExecutionId);
		return athenaClient.getQueryResults(alterQueryResultsRequest);
	}

	public void deleteObject(final String bucketName, final String key) {
		try {
			amazonS3Client.deleteObject(bucketName, key);
		} catch (final AmazonServiceException serviceException) {
			log.error("Error - {}", serviceException.getErrorMessage());
		} catch (final AmazonClientException exception) {
			log.error("Error while deleting file {}", exception.getMessage());
		}
	}

	public S3Object fetchObject(final String bucketNameLoc, final String path) {
		S3Object s3Object = null;
		try {
			log.info("Trying to fetch the S3 Object in Bucket: {} and Key: {}", bucketNameLoc, path);

			ObjectListing objectListing = amazonS3Client.listObjects(bucketNameLoc, path);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				log.info("Key: {}", objectSummary.getKey());
				//if (objectSummary.getKey().contains("part") || objectSummary.getKey().contains("run")) {
				if(!objectSummary.getKey().contains("_SUCCESS")) {
					s3Object = amazonS3Client.getObject(bucketNameLoc, objectSummary.getKey());

					break;
				}
			}
		} catch (final AmazonServiceException serviceException) {
			throw new RuntimeException("Error while streaming File." + serviceException.getMessage());
		}

		log.info("Object found and returning: {}", s3Object);

		return s3Object;
	}

	public Map<String, List<String>> getFinalEventData(String bucketName, String path) throws Exception{
		log.info("Project Bucket Name: {} and Job Id: {}", bucketName, path);
		List<String> lines = new ArrayList<>();
		S3Object finalEventObject = fetchObject(bucketName, path);
		if(finalEventObject != null) {
			try (final InputStreamReader streamReader = new InputStreamReader(finalEventObject.getObjectContent(), StandardCharsets.UTF_8);
					final BufferedReader reader = new BufferedReader(streamReader)) {
				lines = reader.lines().collect(Collectors.toList());
				if(lines != null && !lines.isEmpty()) {
					log.info("Header line is: {}", lines.get(0));
				}

				Map<String, List<String>> s3ObjMap = new HashMap<>();
				s3ObjMap.put(finalEventObject.getKey(), lines);

				// Close the opened S3Object..
				finalEventObject.close();

				return s3ObjMap;

			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
		}

		return Collections.emptyMap();
	}

	public ArrayNode getFileContentHelper(
			final QsFileContent detailsObj, boolean skipMetadataUpdate, String type)
					throws InterruptedException {
		final String fileName = detailsObj.getFileName();
		final String tableName = detailsObj.getTableName();
		final int folderId = detailsObj.getFolderId();
		/*Data And Metadata Started !*/
		final QueryExecutionContext queryContext = new QueryExecutionContext();
		String database = chooseDatabase(detailsObj.getProject(), type);
		queryContext.setDatabase(database);
		final String queryString = selectQuery(tableName, detailsObj.getPartition());
		// TODO EncryptionConfiguration
		log.info("Query to athena : {}", queryString);
		final String queryExecutionId = athenaPrepareQuery(queryContext, queryString);
		final GetQueryResultsResult queryResults = athenaRunQuery(queryExecutionId);

		final ResultSet dataResultSet = queryResults.getResultSet();
		final List<Row> dataRowsList = dataResultSet.getRows();

		final List<ColumnInfo> dataColumnInfo = dataResultSet.getResultSetMetadata().getColumnInfo();
		final ObjectMapper globalObjectMapper = new ObjectMapper();
		final ArrayNode dataArrayNode = globalObjectMapper.createArrayNode();
		final ArrayNode metaDataArrayNode = globalObjectMapper.createArrayNode();
		final List<String> columnDetails = new ArrayList<>();
		final List<ColumnMetaData> columns = new ArrayList<>();
		boolean isPartition = false;
		for (final ColumnInfo dataColumnInfoRecord : dataColumnInfo) {
			final ColumnMetaData colMetadata = new ColumnMetaData();
			final ObjectNode metaDataObject = globalObjectMapper.createObjectNode();
			metaDataObject.put("column_name", dataColumnInfoRecord.getName());
			metaDataObject.put("data_type", dataColumnInfoRecord.getType());
			colMetadata.setColumnName(dataColumnInfoRecord.getName());
			colMetadata.setDataType(dataColumnInfoRecord.getType());
			columnDetails.add(dataColumnInfoRecord.getName());
			if (!dataColumnInfoRecord.getName().equals("partition_0")) {
				isPartition = true;
				columns.add(colMetadata);
				metaDataArrayNode.add(metaDataObject);
			}
		}
		if (!skipMetadataUpdate) {
			fileHelper.updateFileMetadata(metaDataArrayNode, detailsObj.getProject(), fileName, folderId);
		}
		log.info("Column metadata List with size {} and contains {}", columns.size(), columns);


		prepareData(dataRowsList, globalObjectMapper, dataArrayNode, columnDetails, isPartition);
		/*Data And Metadata Ended !*/
		/*Analytics Part started !*/

		final String prepareAnalyticsQuery =
				prepareAnalyticsQuery(columns, tableName, detailsObj.getPartition());
		final String athenaQueryId = athenaPrepareQuery(queryContext, prepareAnalyticsQuery);
		final GetQueryResultsResult athenaRunQuery = athenaRunQuery(athenaQueryId);
		final ResultSet analyticsDataResultSet = athenaRunQuery.getResultSet();
		final List<Row> analyticsDataRowsList = analyticsDataResultSet.getRows();
		final List<ColumnInfo> analyticsDataColumnInfo =
				analyticsDataResultSet.getResultSetMetadata().getColumnInfo();
		final List<String> analyticsColDetails = new ArrayList<>();
		final ArrayNode analyticsArrayNode = globalObjectMapper.createArrayNode();
		ObjectNode analyticsObjectNode;
		for (final ColumnInfo analyticsColumnInfo : analyticsDataColumnInfo) {
			analyticsColDetails.add(analyticsColumnInfo.getName());
		}

		for (int j = 1; j < analyticsDataRowsList.size(); j++) {
			final Row row = analyticsDataRowsList.get(j);
			int index = 0;
			analyticsObjectNode = globalObjectMapper.createObjectNode();
			for (final Datum rowData : row.getData()) {
				analyticsObjectNode.put(analyticsColDetails.get(index), rowData.getVarCharValue());
				index++;
			}
			analyticsArrayNode.add(analyticsObjectNode);
		}

		/*Analytics Part Ended !*/

		final ArrayNode finalJson = globalObjectMapper.createArrayNode();
		finalJson.add(dataArrayNode);
		finalJson.add(metaDataArrayNode);
		finalJson.add(analyticsArrayNode);
		return finalJson;
	}

	public ArrayNode getRawFileContentHelper(final QsFileContent detailsObj) throws Exception{
		Instant now = Instant.now();
		final List<Row> dataRowsList = getFileData(detailsObj);
		final ObjectMapper globalObjectMapper = new ObjectMapper();
		final ArrayNode dataArrayNode = globalObjectMapper.createArrayNode();

		String columnMetadataStr = detailsObj.getFileMetadata();
		ObjectMapper mapper = new ObjectMapper();
		ColumnMetaData[] columnMetadataArr = mapper.readValue(columnMetadataStr, ColumnMetaData[].class);
		List<ColumnMetaData> columnMetadata = Arrays.asList(columnMetadataArr);

		final ArrayNode metaDataArrayNode = globalObjectMapper.createArrayNode();
		List<String> columnDetails = new ArrayList<>();
		columnMetadata.stream().forEach((colMetadata) -> {
			columnDetails.add(colMetadata.getColumnName());
			metaDataArrayNode.add(globalObjectMapper.createObjectNode().
					put("column_name", colMetadata.getColumnName()).
					put("data_type", colMetadata.getDataType())
					);
		});

		prepareDataV2(dataRowsList, globalObjectMapper, dataArrayNode, columnDetails);

		log.info("Elapsed time to fetch data rows: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

		final ArrayNode analyticsArrayNode = globalObjectMapper.createArrayNode();
		final ArrayNode finalJson = globalObjectMapper.createArrayNode();
		finalJson.add(dataArrayNode);
		finalJson.add(metaDataArrayNode);
		finalJson.add(analyticsArrayNode); // This node will be removed after the integration in the UI.

		return finalJson;
	}

	public ArrayNode getRawFileColumnAnalytics(final QsFileContent detailsObj) throws Exception{
		final ObjectMapper globalObjectMapper = new ObjectMapper();

		String columnMetadataStr = detailsObj.getFileMetadata();
		ObjectMapper mapper = new ObjectMapper();
		final QueryExecutionContext queryContext = new QueryExecutionContext();
		queryContext.setDatabase(detailsObj.getProject().getRawDb());

		ColumnMetaData[] columnMetadataArr = mapper.readValue(columnMetadataStr, ColumnMetaData[].class);
		List<ColumnMetaData> columnMetadata = Arrays.asList(columnMetadataArr);

		String tableName = qsUtil.nameCheck(detailsObj.getFolderName(), detailsObj.getFileName());

		final String prepareAnalyticsQuery =
				prepareAnalyticsQuery(columnMetadata, tableName, detailsObj.getPartition());
		final String athenaQueryId = athenaPrepareQuery(queryContext, prepareAnalyticsQuery);
		final GetQueryResultsResult athenaRunQuery = athenaRunQuery(athenaQueryId);
		final ResultSet analyticsDataResultSet = athenaRunQuery.getResultSet();
		final List<Row> analyticsDataRowsList = analyticsDataResultSet.getRows();

		final List<String> analyticsColDetails = Arrays.asList("column_value", "count", "column_name"); // These are the three columns that we use for Analytics..
		final ArrayNode analyticsArrayNode = globalObjectMapper.createArrayNode();

		int size = analyticsDataRowsList.size();
		for (int i = 1; i < size; i++) {
			final Row row = analyticsDataRowsList.get(i);

			ObjectNode analyticsObjectNode = globalObjectMapper.createObjectNode();
			int j = 0;
			for(Datum data : row.getData()) {
				analyticsObjectNode.put(analyticsColDetails.get(j), data.getVarCharValue());
				j++;
			}
			analyticsArrayNode.add(analyticsObjectNode);
		}

		final ArrayNode finalJson = globalObjectMapper.createArrayNode();
		finalJson.add(analyticsArrayNode);

		return finalJson;
	}

	public ArrayNode getRawFileSingleColumnAnalytics(final Projects project, final String folderName, final String columnName, final String partitionName) throws Exception{
		final ObjectMapper globalObjectMapper = new ObjectMapper();

		final QueryExecutionContext queryContext = new QueryExecutionContext();
		queryContext.setDatabase(project.getRawDb());

		ColumnMetaData cm = new ColumnMetaData(columnName, "string");
		List<ColumnMetaData> columnMetadata = Arrays.asList(cm);

		final String prepareAnalyticsQuery =
				prepareAnalyticsQuery(columnMetadata, project.getRawDb()+"."+folderName.toLowerCase(), partitionName);
		final String athenaQueryId = athenaPrepareQuery(queryContext, prepareAnalyticsQuery);
		final GetQueryResultsResult athenaRunQuery = athenaRunQuery(athenaQueryId);
		final ResultSet analyticsDataResultSet = athenaRunQuery.getResultSet();
		final List<Row> analyticsDataRowsList = analyticsDataResultSet.getRows();

		final List<String> analyticsColDetails = Arrays.asList("column_value", "count", "column_name"); // These are the three columns that we use for Analytics..
		final ArrayNode analyticsArrayNode = globalObjectMapper.createArrayNode();

		int size = analyticsDataRowsList.size();
		for (int i = 1; i < size; i++) {
			final Row row = analyticsDataRowsList.get(i);

			ObjectNode analyticsObjectNode = globalObjectMapper.createObjectNode();
			int j = 0;
			for(Datum data : row.getData()) {
				analyticsObjectNode.put(analyticsColDetails.get(j), data.getVarCharValue());
				j++;
			}
			analyticsArrayNode.add(analyticsObjectNode);
		}

		log.info("Array Node value: {}", analyticsArrayNode);
		if(analyticsArrayNode != null && analyticsArrayNode.size() > 0) {
			final ArrayNode finalJson = globalObjectMapper.createArrayNode();
			finalJson.add(analyticsArrayNode);

			return finalJson;
		}

		return null;
	}

	private String prepareAthenaQueryForColumnStats(Projects project, String folderName, String columnName) {
		StringBuilder sb = new StringBuilder();

		sb.append("select ");
		sb.append(columnName);
		sb.append(", count(");
		sb.append(columnName);
		sb.append(") as col_value_count from ");
		sb.append(project.getRawDb());
		sb.append(".");
		sb.append(folderName);
		sb.append(" group by ");
		sb.append(columnName);
		sb.append(" order by col_value_count asc ");

		return sb.toString();
	}

	@Deprecated
	public ArrayNode getRawFileContentAnalytics(final QsFileContent detailsObj) throws Exception{
		Instant now = Instant.now();
		final ObjectMapper globalObjectMapper = new ObjectMapper();
		final ArrayNode piiDataArrayNode = globalObjectMapper.createArrayNode();

		String columnMetadataStr = detailsObj.getFileMetadata();
		ObjectMapper mapper = new ObjectMapper();
		ColumnMetaData[] columnMetadataArr = mapper.readValue(columnMetadataStr, ColumnMetaData[].class);
		List<ColumnMetaData> columnMetadata = Arrays.asList(columnMetadataArr);

		List<String> columnDetails = new ArrayList<>();
		columnMetadata.stream().forEach((colMetadata) -> {
			columnDetails.add(colMetadata.getColumnName());
		});

		List<Row> dataRowsList = getFileData(detailsObj);
		if(dataRowsList != null) {
			int dataSize = dataRowsList.size();
			if(dataSize >=2) {
				// Get the sublist of dataRowsList of size 2. As we need only one data record to
				// identify PII columns
				List<Row> dataRowsSubList = null;
				if(dataSize > 1000) {
					dataRowsSubList = dataRowsList.subList(0, 1000);
				} else {
					dataRowsSubList = dataRowsList.subList(0, dataSize);
				}

				preparePiiData(dataRowsSubList, globalObjectMapper, piiDataArrayNode, columnDetails);
			}
		}

		log.info("Elapsed time to PII Information: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

		final ArrayNode finalJson = globalObjectMapper.createArrayNode();
		finalJson.add(piiDataArrayNode);

		return finalJson;
	}

	public String getRawFileContentAnalyticsV2(final String bucketNameLocal, final QsFileContent detailsObj) throws Exception {
		// Get the Python Script used for PII Detection from Classpath as a Resource before proceeding
		// further..
		URL url = null;

		if (QsConstants.PII_DETECTION.equals(detailsObj.getAnalyticsType())) {
			url = getClass().getClassLoader().getResource(QsConstants.PII_PYTHON_FILE_REL_LOC);
		} else if (QsConstants.OUTLIERS_DETECTION.equals(detailsObj.getAnalyticsType())) {
			url = getClass().getClassLoader().getResource(QsConstants.OUTLIERS_PYTHON_FILE_REL_LOC);
		}

		File detectionPyFile = null;
		if (url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if (isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(bucketNameLocal);
		commands.add(detailsObj.getFileObjectKey());
		commands.add(s3AccessKey);
		commands.add(s3SecretKey);

		return runExternalCommand(commands);
	}

	public String getDeltaBtwnFiles(final String bucketNameLocal, final String file1ObjKey, final String file2ObjKey) throws Exception{
		URL url = getClass().getClassLoader().getResource(QsConstants.DELTA_PYTHON_FILE_REL_LOC);

		File detectionPyFile = null;
		if(url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if(isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(bucketNameLocal);
		commands.add(file1ObjKey);
		commands.add(bucketNameLocal);
		commands.add(file2ObjKey);
		commands.add(s3AccessKey);
		commands.add(s3SecretKey);

		return runExternalCommand(commands);
	}

	public String getFileStatistics(final String bucketNameLocal, final String fileObjKey) throws Exception{
		URL url = getClass().getClassLoader().getResource(QsConstants.FILE_STATS_PYTHON_FILE_REL_LOC);

		File detectionPyFile = null;
		if(url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if(isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(bucketNameLocal);
		commands.add(fileObjKey);
		commands.add(s3AccessKey);
		commands.add(s3SecretKey);

		return runExternalCommand(commands);
	}

	public String getFileColumnValueFreq(final String  bucketNameLocal, final String fileObjKey, final String columnName) throws Exception{
		URL url = getClass().getClassLoader().getResource(QsConstants.COLUMN_VAL_FREQ_PYTHON_FILE_REL_LOC);

		File detectionPyFile = null;
		if(url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if(isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(bucketNameLocal);
		commands.add(fileObjKey);
		commands.add(columnName);
		commands.add(s3AccessKey);
		commands.add(s3SecretKey);

		return runExternalCommand(commands);
	}

	public String getFileQualityStats(final String bucketNameLocal, final String fileObjKey, final String dataQualityQuery) throws Exception{
		URL url = getClass().getClassLoader().getResource(QsConstants.FILE_QUALITY_CHECK_PYTHON_FILE_REL_LOC);

		File detectionPyFile = null;
		if(url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if(isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(bucketNameLocal);
		commands.add(fileObjKey);

		if(dataQualityQuery != null && !dataQualityQuery.isEmpty()) {
			commands.add(dataQualityQuery);
		} else {
			commands.add("NO_DATA_QUALITY_CHECKS");
		}

		commands.add(s3AccessKey);
		commands.add(s3SecretKey);

		return runExternalCommand(commands);
	}

	public String getColumnInvalidDataInfo(final String bucketNameLocal, final String fileObjKey, final String dataQualityQuery) throws Exception{
		URL url = getClass().getClassLoader().getResource(QsConstants.FILE_QUALITY_CHECK_INV_PYTHON_FILE_REL_LOC);

		File detectionPyFile = null;
		if(url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if(isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(bucketNameLocal);
		commands.add(fileObjKey);

		if(dataQualityQuery != null && !dataQualityQuery.isEmpty()) {
			commands.add(dataQualityQuery);
		} else {
			commands.add("NO_DATA_QUALITY_CHECKS");
		}

		commands.add(s3AccessKey);
		commands.add(s3SecretKey);

		return runExternalCommand(commands);
	}

	public String getPiiColumnInfo(final String inputFileWithPath) throws Exception{
		URL url = getClass().getClassLoader().getResource(QsConstants.PII_COL_DETECTION_PYTHON_FILE_REL_LOC);

		File detectionPyFile = null;
		if(url != null) {
			detectionPyFile = new File(url.toURI());
		} else {
			log.info("Couldn't locate the Python script.");

			return null;
		}

		List<String> commands = new ArrayList<>();

		if(isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add("python");
		} else {
			commands.add("python3");
		}

		commands.add(detectionPyFile.getAbsolutePath());
		commands.add(inputFileWithPath);

		return runExternalCommand(commands);
	}


	private String runExternalCommand(List<String> commands) throws IOException, InterruptedException{
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(commands);
		Process process = pb.start();

		String jsonResponse="";
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			br.lines().forEach((line) -> {
				sb.append(line);
			});

			jsonResponse = sb.toString();
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			br.lines().forEach((line) -> System.out.println(line));
		}

		process.waitFor();

		return jsonResponse;
	}

	private List<Row> getFileData(final QsFileContent detailsObj) throws Exception{
		final QueryExecutionContext queryContext = new QueryExecutionContext();
		String database = detailsObj.getProject().getRawDb();
		queryContext.setDatabase(database);

		String tableName = qsUtil.nameCheck(detailsObj.getFolderName(), detailsObj.getFileName());
		final String queryString = selectQuery(tableName, detailsObj.getPartition());
		// TODO EncryptionConfiguration
		log.info("Query to athena : {}", queryString);
		final String queryExecutionId = athenaPrepareQuery(queryContext, queryString);
		final GetQueryResultsResult queryResults = athenaRunQuery(queryExecutionId);

		final ResultSet dataResultSet = queryResults.getResultSet();
		final List<Row> dataRowsList = dataResultSet.getRows();

		return dataRowsList;
	}

	public String getRegion() {
		return amazonS3Client.getRegion().toString();
	}

	private String prepareAnalyticsQuery(
			final List<ColumnMetaData> columns, String tableName, final String partitionName) {

		log.info("------------->Column Metadata is: {}", columns);
		StringBuilder sb = new StringBuilder();

		// tableName and partition name
		if (partitionName != null) {
			sb.append(tableName);
			sb.append(" where partition_0 ='");
			sb.append(partitionName);
			sb.append("'");
		} else {
			sb.append(tableName);
		}

		final String tableNameTmp = sb.toString(); 
		sb.delete(0, sb.length());

		columns.stream().forEach((column) -> sb.append(prepareSubQuery(column.getColumnName(), tableNameTmp)));

		String query = sb.toString();
		if(query != null && query.endsWith(" UNION ALL ")) {
			query = query.substring(0, query.lastIndexOf(" UNION ALL "));
		}

		log.info("Query to athena - {}", query);
		return query;
	}

	private String prepareSubQuery(String column, String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT CAST(");
		sb.append(specialCharColName(column));
		sb.append(" AS varchar) AS column_value, count(1) AS count, '");
		sb.append(column);
		sb.append("' AS column_name FROM ");
		sb.append(tableName);
		sb.append(" GROUP BY ");
		sb.append(specialCharColName(column));
		sb.append(" UNION ALL ");

		return sb.toString();
	}

	private void tempTableHandler(QsFileContent tableAwsName, String queryString)
			throws InterruptedException {
		final QueryExecutionContext queryContext = new QueryExecutionContext();
		queryContext.setDatabase(tableAwsName.getProject().getRawDb());
		log.info("Query to athena : {}", queryString);
		final String queryExecutionId = athenaPrepareQuery(queryContext, queryString);
		log.info("Query to athena ExecutionId : {}", queryExecutionId);
		athenaRunQuery(queryExecutionId);
		log.info("Athena Query completed ");
	}

	public FileMetaDataAwsRef createEntryAwsRef(RunJobStatus runJob, QsFileContent tableAwsName)
			throws SQLException, InterruptedException {
		FileMetaDataAwsRef fileMetaDataRef = new FileMetaDataAwsRef();
		fileMetaDataRef.setFileType(PROCESSED);
		fileMetaDataRef.setCreatedDate(new Date());
		fileMetaDataRef.setFileId(runJob.getFileId());
		fileMetaDataRef.setCreatedBy(String.valueOf(runJob.getUserId()));
		tableAwsName.setTableName(null);
		tableAwsName.setPartition(String.valueOf(runJob.getRunJobId()));
		QsFileContent qsFileContent = qsUtil.decideTableName(tableAwsName, true);
		//ArrayNode contents = getFileContentHelper(qsFileContent, true, PROCESSED);
		fileMetaDataRef.setAthenaTable(qsFileContent.getTableName());
		fileMetaDataRef.setTablePartition(String.valueOf(runJob.getRunJobId()));
		//if (!contents.isEmpty()) {
		//fileMetaDataRef.setFileMetaData(contents.get(1).toString());
		//}
		dbUtil.changeSchema(tableAwsName.getProject().getDbSchemaName());
		return awsDataService.save(fileMetaDataRef);
	}



	public String selectQuery(final String tableName, final String partitionName) {
		String query;
		if (partitionName != null) {
			query =
					"SELECT * FROM \""
							+ tableName
							+ "\" where partition_0 ='"
							+ partitionName
							+ "' limit "
							+ qsQueryLimit;
		} else {
			query = "SELECT * FROM \"" + tableName + "\" limit " + qsQueryLimit;
		}
		return query;
	}

	private String createQuery(
			final String newTableName, String oldTableName, final String partitionName) {
		String query;
		if (partitionName != null) {
			query =
					"CREATE TABLE "
							+ newTableName
							+ " AS SELECT * FROM \""
							+ oldTableName
							+ "\" where partition_0 ='"
							+ partitionName
							+ "'";
		} else {
			query = "CREATE TABLE " + newTableName + " AS SELECT * FROM \"" + oldTableName + "\"";
		}
		return query;
	}

	private String DeleteQuery(String tableName) {
		String query = "DROP TABLE " + tableName;
		return query;
	}

	private String specialCharColName(final String columnName) {
		final Pattern pattern = Pattern.compile("[a-zA-Z0-9_]*");
		final Matcher matcher = pattern.matcher(columnName);
		if (!matcher.matches()) {
			return "'" + columnName + "'";
		} else {
			return columnName;
		}
	}

	/**
	 *  Utility method for uploading files to S3 location. This API uses efficient TransferManager based approach
	 *  for uploading files to the remote S3 bucket. It also updates Athena by creating/altering Athena table
	 *  with the file uploaded to AWS S3 bucket 
	 *  
	 * @param fileName
	 * @param contentType
	 * @return
	 */
	public S3FileUploadResponse storeObjectInS3Async(final String bucketNameLocal,
			final Object inputFile, final String partitionName, final String dbName, 
			final String folderName, final String fileName, final String contentType) throws Exception{

		log.info("\nReceived file upload request for the file: {}", fileName);

		Instant now = Instant.now();
		TransferManager transferManager = null;
		InputStream is = null;
		MultipartFile multipartFile = null;
		File normalFile = null;
		Map<String, String> columnMetadata = new LinkedHashMap<>();
		S3FileUploadResponse s3FileUploadResp = new S3FileUploadResponse();
		try {

			final ObjectMetadata objectMetadata = new ObjectMetadata();      
			objectMetadata.setContentType(contentType);

			if(inputFile instanceof MultipartFile) {
				multipartFile = (MultipartFile) inputFile;
				objectMetadata.setContentLength(multipartFile.getSize());
				s3FileUploadResp.setFileSize(String.valueOf(multipartFile.getSize()));
				s3FileUploadResp.setFileName(multipartFile.getOriginalFilename());
				is = multipartFile.getInputStream();
				log.info("File is a MultipartFile Instance with name: {}", multipartFile.getOriginalFilename());
			} else if(inputFile instanceof File) {
				normalFile = (File) inputFile;
				log.info("File is a regular file Instance with name: {} and path: {}", normalFile.getName(), normalFile.getAbsolutePath());
				objectMetadata.setContentLength(normalFile.length());
				s3FileUploadResp.setFileSize(String.valueOf(normalFile.length()));
				s3FileUploadResp.setFileName(normalFile.getName());
				is = new FileInputStream(normalFile);
			}
			s3FileUploadResp.setFileContentType(contentType);
			log.info("Bucket Name: {} and File Name: {}", bucketNameLocal, fileName);

			// Enable Transfer Acceleration before uploading the file to the bucket.
			amazonS3Client.setBucketAccelerateConfiguration(
					new SetBucketAccelerateConfigurationRequest(bucketNameLocal,
							new BucketAccelerateConfiguration(
									BucketAccelerateStatus.Enabled)));

			// Verify whether the transfer acceleration is enabled on the bucket or not...
			String accelerateStatus = amazonS3Client.getBucketAccelerateConfiguration(
					new GetBucketAccelerateConfigurationRequest(bucketNameLocal))
					.getStatus();

			log.info("Transfer acceleration status of the bucket {} is {}", bucketNameLocal, accelerateStatus);

			// Approach 1:
			transferManager = TransferManagerBuilder.standard()
					.withMultipartUploadThreshold((long) (1 * 1024 * 1025))
					.withS3Client(amazonS3Client)
					.build(); 

			Upload upload = transferManager.upload(bucketNameLocal, fileName, is, objectMetadata);
			upload.waitForCompletion();

			// After the upload is complete, release the TransferManager resources by calling shutdownNow() API of AWS SDK..
			transferManager.shutdownNow(false); // Shutdown only the TransferManager and not the underlying S3Client instance.

			// Approach 2:
			//amazonS3Client.putObject(bucketName, fileName, is, objectMetadata);

			log.info("Uploaded file to destination : {}/{}", bucketNameLocal, fileName);
			log.info("Elapsed time to upload file in AWS S3 bucket is: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

			now = Instant.now();
			log.info("Started Athena process...");

			final QueryExecutionContext queryContext = new QueryExecutionContext();
			queryContext.setDatabase(dbName);

			String athenaCheckQuery = "SHOW TABLES IN "+ dbName.toLowerCase() +" '"+folderName.toLowerCase()+"'";

			log.info("Query to athena to check whether table is present in database '{}' or not", athenaCheckQuery);
			final String checkQueryExeId = athenaPrepareQuery(queryContext, athenaCheckQuery);
			final GetQueryResultsResult checkQueryResults = athenaRunQuery(checkQueryExeId);
			final ResultSet dataResultSet = checkQueryResults.getResultSet();
			final List<Row> dataRowsList = dataResultSet.getRows();

			if(inputFile instanceof MultipartFile) {
				multipartFile = (MultipartFile) inputFile;
				is = multipartFile.getInputStream();

			} else if (inputFile instanceof File) {
				normalFile = (File) inputFile;
				is = new FileInputStream(normalFile);
			}

			List<String> lines = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				// Limiting to only reading two lines, with the assumption that 
				// the first line is a header line, followed by data line.
				br.lines().limit(2).forEach(lines::add); 
			} catch (Exception e) {
				e.printStackTrace();
			}

			columnMetadata = readMetadata(lines);
			s3FileUploadResp.setColumnMetaData(columnMetadata);
			String s3FileLocation = "s3://"+bucketNameLocal + "/" + folderName+"/"+partitionName;

			if(dataRowsList == null || dataRowsList.isEmpty()) {
				// Table is not present in Athena. So, first create it and then add the partition later.
				createTableInAthena(columnMetadata, dbName, partitionName, folderName, s3FileLocation);
			}

			if(partitionName != null) {
				addPartitionToTableInAthena(bucketNameLocal, dbName, folderName, partitionName);
			}

		} catch (AmazonClientException | IOException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		}

		// Below line of code is applicable only to the File that is created using API. It is not longer required to be
		// present in the physical Local file system in the temporary directory. Hence deleting it.
		if(normalFile != null) {
			normalFile.delete();
		}

		URL s3FileUrl = amazonS3Client.getUrl(bucketNameLocal, fileName);
		log.info("Elapsed time to complete Athena table processing is: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

		//columnMetadata.put("S3_FILE_URL", s3FileUrl.toString());
		s3FileUploadResp.setS3FileUrl(s3FileUrl.toString());

		//return columnMetadata;
		return s3FileUploadResp;
	}

	public String storeUdfDefInS3Async(final String udfBucketName, 
			final File inputFile, final String fileName, final String contentType) throws Exception{
		log.info("\nReceived file upload request for the udf file: {}", fileName);

		Instant now = Instant.now();
		TransferManager transferManager = null;
		InputStream is = null;
		try {

			final ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(inputFile.length());
			is = new FileInputStream(inputFile);

			log.info("Bucket Name: {} and File Name: {}", udfBucketName, fileName);

			// Enable Transfer Acceleration before uploading the file to the bucket.
			amazonS3Client.setBucketAccelerateConfiguration(
					new SetBucketAccelerateConfigurationRequest(udfBucketName,
							new BucketAccelerateConfiguration(
									BucketAccelerateStatus.Enabled)));

			// Verify whether the transfer acceleration is enabled on the bucket or not...
			String accelerateStatus = amazonS3Client.getBucketAccelerateConfiguration(
					new GetBucketAccelerateConfigurationRequest(udfBucketName))
					.getStatus();

			log.info("Transfer acceleration status of the bucket {} is {}", udfBucketName, accelerateStatus);

			// Approach 1:
			transferManager = TransferManagerBuilder.standard()
					.withMultipartUploadThreshold((long) (1 * 1024 * 1025))
					.withS3Client(amazonS3Client)
					.build(); // TODO: We have to create a Singleton instance of TransferManager instance and close it as part of shutdown hook.


			Upload upload = transferManager.upload(new PutObjectRequest(udfBucketName, fileName, is, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));

			upload.waitForCompletion();
			// After the upload is complete, release the TransferManager resources by calling shutdownNow() API of AWS SDK..
			transferManager.shutdownNow(false); // Shutdown only the TransferManager and not the underlying S3Client instance.

			log.info("Uploaded udf file to destination : {}/{}", udfBucketName, fileName);
			log.info("Elapsed time to upload udf file in AWS S3 bucket is: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

			URL s3FileUrl = amazonS3Client.getUrl(udfBucketName, fileName);
			return s3FileUrl.toString();
		}
		catch (AmazonClientException | IOException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading udf file." + exception.getMessage());
		}
	}

	public String storeImageInS3Async(final String imagesBucketName, 
			final MultipartFile inputFile, final String fileName, final String contentType) throws Exception{

		log.info("\nReceived file upload request for the file: {}", fileName);

		Instant now = Instant.now();
		TransferManager transferManager = null;
		InputStream is = null;
		try {

			final ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(inputFile.getSize());
			is = inputFile.getInputStream();

			log.info("Bucket Name: {} and File Name: {}", imagesBucketName, fileName);

			// Enable Transfer Acceleration before uploading the file to the bucket.
			amazonS3Client.setBucketAccelerateConfiguration(
					new SetBucketAccelerateConfigurationRequest(imagesBucketName,
							new BucketAccelerateConfiguration(
									BucketAccelerateStatus.Enabled)));

			// Verify whether the transfer acceleration is enabled on the bucket or not...
			String accelerateStatus = amazonS3Client.getBucketAccelerateConfiguration(
					new GetBucketAccelerateConfigurationRequest(imagesBucketName))
					.getStatus();

			log.info("Transfer acceleration status of the bucket {} is {}", imagesBucketName, accelerateStatus);

			// Approach 1:
			transferManager = TransferManagerBuilder.standard()
					.withMultipartUploadThreshold((long) (1 * 1024 * 1025))
					.withS3Client(amazonS3Client)
					.build(); // TODO: We have to create a Singleton instance of TransferManager instance and close it as part of shutdown hook.


			Upload upload = transferManager.upload(new PutObjectRequest(imagesBucketName, fileName, is, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));



			upload.waitForCompletion();
			// After the upload is complete, release the TransferManager resources by calling shutdownNow() API of AWS SDK..
			transferManager.shutdownNow(false); // Shutdown only the TransferManager and not the underlying S3Client instance.

			log.info("Uploaded file to destination : {}/{}", imagesBucketName, fileName);
			log.info("Elapsed time to upload file in AWS S3 bucket is: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

			URL s3FileUrl = amazonS3Client.getUrl(imagesBucketName, fileName);
			return s3FileUrl.toString();
		}
		catch (AmazonClientException | IOException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		}
	}

	
	public void savePartition(
			@PathVariable("projectId") int projectId,
			@PathVariable("folderId") int folderId,
			String partitionRef,
			QsFolders folder,
			QsFiles saveFileInfo,
			URL destUrl)
					throws SQLException {
		final QsPartition partition = new QsPartition();

		partition.setFolderId(folderId);
		partition.setProjectId(projectId);
		partition.setPartitionName(partitionRef);
		partition.setS3Location(destUrl.toString());
		partition.setFileId(saveFileInfo.getFileId());
		partition.setFolderName(folder.getFolderName());
		partition.setFileName(saveFileInfo.getFileName());
		partitionService.save(partition);
	}

	public QsFiles getQsFiles(int projectId, int folderId, Projects project, String userName, String columnMetadataStr, 
			int userId, UploadFileRequest uploadFileRequest, S3FileUploadResponse s3FileUploadRes) throws SQLException, JsonProcessingException{
		String contentType = "";
		String fileName = "";
		String fileSize = "";

		/*if(fileObj instanceof MultipartFile) {
      MultipartFile mpFile = (MultipartFile) fileObj;
      contentType = mpFile.getContentType();
      fileName = mpFile.getOriginalFilename();
      fileSize = String.valueOf(mpFile.getSize());
    } else if(fileObj instanceof File) {
      File file = (File) fileObj;
      //contentType = "application/csv";
      contentType = s3FileUploadRes.getFileContentType();
      fileName = file.getName();
      //fileSize = String.valueOf(file.length());
      fileSize = s3FileUploadRes.getFileSize();
    }*/

		contentType = s3FileUploadRes.getFileContentType();
		fileName = s3FileUploadRes.getFileName();
		fileSize = s3FileUploadRes.getFileSize();

		final QsFiles fileToSave = new QsFiles();
		fileToSave.setFolderId(folderId);
		fileToSave.setFileVersionNum(1);
		fileToSave.setProjectId(projectId);
		fileToSave.setQsMetaData(columnMetadataStr);
		fileToSave.setCreatedDate(QsConstants.getCurrentUtcDate());
		fileToSave.setUserId(userId); 
		fileToSave.setFileType(contentType);
		fileToSave.setFileName(fileName);
		fileToSave.setRuleCreatedFrom(userName);
		fileToSave.setFileSize(fileSize);
		fileToSave.setActive(true);
		fileToSave.setAdditionalInfo(
				(uploadFileRequest != null) ? new ObjectMapper().writeValueAsString(uploadFileRequest)
						: (s3FileUploadRes != null) ? s3FileUploadRes.getS3FileUrl():
								null);

		// Update the Project Cumulative Size after uploading the file..
		dbUtil.changeSchema("public");
		ProjectCumulativeSizeInfo projectSizeInfo = projectSizeService.getSizeInfoForProject(projectId, userId);
		long lfileSize = Long.parseLong(fileSize);
		if(projectSizeInfo != null) {
			long currentProjSize = projectSizeInfo.getCumulativeSize();
			projectSizeInfo.setCumulativeSize(currentProjSize+ lfileSize);
		} else
		{
			projectSizeInfo = new ProjectCumulativeSizeInfo();
			projectSizeInfo.setProjectId(projectId);
			projectSizeInfo.setUserId(userId);
			projectSizeInfo.setCumulativeSize(lfileSize);
		}
		projectSizeService.save(projectSizeInfo);

		dbUtil.changeSchema(project.getDbSchemaName());
		return fileService.saveFileInfo(fileToSave);
	}


	private void createTableInAthena(Map<String, String> columns, String dbName, String partitionName, String folderName, String s3FileLocation) {
		String athenaTableName = normalizeAthenaTableName(folderName);
		String athenaCreateQuery = prepareAthenaCreateQuery(partitionName, athenaTableName, s3FileLocation, columns);

		log.info("Table is not present, creating a new table with query: {}", athenaCreateQuery);

		try {

			final QueryExecutionContext queryContext2 = new QueryExecutionContext();
			queryContext2.setDatabase(dbName);
			final String createQueryExeId = athenaPrepareQuery(queryContext2, athenaCreateQuery);
			final GetQueryResultsResult createQueryResults = athenaRunQuery(createQueryExeId);

		}catch (AmazonClientException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		}
	}

	public void createTableInAthena(Map<String, String> columns, String dbName, 
			String partitionName, String folderName, String s3FileLocation, List<MetadataReference> metadataRef) {
		
		String athenaTableName = normalizeAthenaTableName(folderName);
		String athenaCreateQuery = prepareAthenaCreateQuery(partitionName, athenaTableName,
				s3FileLocation, columns, metadataRef);

		log.info("Table is not present, creating a new table with query: {}", athenaCreateQuery);

		try {

			final QueryExecutionContext queryContext2 = new QueryExecutionContext();
			queryContext2.setDatabase(dbName);
			final String createQueryExeId = athenaPrepareQuery(queryContext2, athenaCreateQuery);
			final GetQueryResultsResult createQueryResults = athenaRunQuery(createQueryExeId);

		}catch (AmazonClientException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		}
	}
	
	public void createTableInAthena(List<List<String>> columns, String dbName, 
			String partitionName, String tableName, String s3FileLocation, List<MetadataReference> metadataRef) {
		
		String athenaTableName = normalizeAthenaTableName(tableName);
		String athenaCreateQuery = prepareAthenaCreateQuery(partitionName, athenaTableName,
				s3FileLocation, columns, metadataRef);

		log.info("Table is not present, creating a new table with query: {}", athenaCreateQuery);

		try {

			final QueryExecutionContext queryContext2 = new QueryExecutionContext();
			queryContext2.setDatabase(dbName);
			final String createQueryExeId = athenaPrepareQuery(queryContext2, athenaCreateQuery);
			final GetQueryResultsResult createQueryResults = athenaRunQuery(createQueryExeId);

		}catch (AmazonClientException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		}
	}

	public void addPartitionToTableInAthena(String bucketNameLocal, String dbName, String tableName, String partitionName) {
		String athenaTableName = normalizeAthenaTableName(tableName);
		StringBuilder sb = new StringBuilder();
		sb.append("s3://");
		sb.append(bucketNameLocal);
		sb.append("/");
		sb.append(tableName);
		sb.append("/");
		sb.append(partitionName);
		sb.append("/");
		String s3FileLocation = sb.toString();

		sb.delete(0, sb.length());
		// Table is present in the DB, so execute an alter query to add new partition to the existing table.
		sb.append("ALTER TABLE ");
		sb.append(athenaTableName.toLowerCase());
		sb.append(" ADD IF NOT EXISTS PARTITION (partition_0='");
		sb.append(partitionName);
		sb.append("') LOCATION '");
		sb.append(s3FileLocation);
		sb.append("'");

		String addPartitionQuery = sb.toString();

		try {

			final QueryExecutionContext queryContext = new QueryExecutionContext();
			queryContext.setDatabase(dbName);
			log.info("Table already present. Alter query to add new partition is: {}", addPartitionQuery);
			final String addPartQueryExeId = athenaPrepareQuery(queryContext, addPartitionQuery);
			final GetQueryResultsResult addPartQueryResults = athenaRunQuery(addPartQueryExeId);

		}catch (AmazonClientException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		}
	}

	public boolean deletePartitionForTableInAthena(String dbName, String folderName, String partitionName) {
		StringBuilder sb = new StringBuilder();

		// Table is present in the DB, so execute an alter query to add new partition to the existing table.
		sb.append("ALTER TABLE ");
		sb.append(folderName.toLowerCase());
		sb.append(" DROP PARTITION (partition_0='");
		sb.append(partitionName);
		sb.append("'); ");

		String dropPartitionQuery = sb.toString();
		try {

			final QueryExecutionContext queryContext = new QueryExecutionContext();
			queryContext.setDatabase(dbName);
			log.info("Alter query to add new partition is: {}", dropPartitionQuery);
			final String dropPartQueryExeId = athenaPrepareQuery(queryContext, dropPartitionQuery);
			final GetQueryResultsResult dropPartQueryResults = athenaRunQuery(dropPartQueryExeId);

			return true;
		}catch (AmazonClientException | InterruptedException exception) {
			log.error("Failed to delete the partition '{}' in the Athena Table: {}", partitionName, folderName);
			return false;
		}
	}

	public boolean deleteFileMetaDataAwsRef(String runJobId) throws QsRecordNotFoundException{
		long numberOfRecordsDeleted = awsDataService.deleteByPartitionName(runJobId);
		log.info("Deleted the FileMedatDataAwsRef entry for the job id {}. "
				+ "No. of entries deleted(hint: must be always 1): {}", runJobId, numberOfRecordsDeleted);
		return numberOfRecordsDeleted == 1 ? true : false;
	}

	private void prepareData(
			final List<Row> dataRowsList,
			final ObjectMapper globalObjectMapper,
			final ArrayNode dataArrayNode,
			final List<String> columnDetails,
			final boolean isPartition) {
		ObjectNode dataObject;
		for (int i = 1; i < dataRowsList.size(); i++) {
			final Row row = dataRowsList.get(i);
			int index = 0;
			int itrSize;
			if (isPartition) {
				itrSize = (row.getData().size()) - 1;
			} else {
				itrSize = row.getData().size();
			}
			dataObject = globalObjectMapper.createObjectNode();
			// changed to avoid partition_0 column
			for (int j = 0; j <= itrSize; j++) {
				final Datum rowData = row.getData().get(j);
				dataObject.put(columnDetails.get(index), rowData.getVarCharValue());
				index++;
			}
			dataArrayNode.add(dataObject);
		}
	}

	private void prepareDataV2(
			final List<Row> dataRowsList,
			final ObjectMapper globalObjectMapper,
			final ArrayNode dataArrayNode,
			final List<String> columnDetails) {

		if(dataRowsList == null || dataRowsList.isEmpty()) {
			return;
		}

		ObjectNode dataObject;
		int rowsSize = dataRowsList.size();
		int colsSize = columnDetails.size();

		for (int i = 1; i < rowsSize; i++) {
			final Row row = dataRowsList.get(i);
			dataObject = globalObjectMapper.createObjectNode();
			// changed to avoid partition_0 column
			for (int j = 0; j < colsSize; j++) {
				final Datum rowData = row.getData().get(j);
				dataObject.put(columnDetails.get(j), rowData.getVarCharValue());
			}
			dataArrayNode.add(dataObject);
		}
	}

	private void preparePiiData(
			final List<Row> dataRowsList,
			final ObjectMapper globalObjectMapper,
			final ArrayNode dataArrayNode,
			final List<String> columnDetails) {

		int rowsSize = dataRowsList.size();
		int colsSize = columnDetails.size();

		Set<String> piiCols = new HashSet<>();
		for (int j = 0; j < colsSize; j++) {
			for (int i = 1; i < rowsSize; i++) {
				final Row row = dataRowsList.get(i);
				final Datum rowData = row.getData().get(j);
				if(!piiCols.contains(columnDetails.get(j)) && checkPiiColumn(rowData.getVarCharValue())) {
					piiCols.add(columnDetails.get(j));
				}
			}
		}

		if(piiCols != null && !piiCols.isEmpty()) {
			piiCols.stream().forEach((col) -> {
				final ObjectNode dataObject = globalObjectMapper.createObjectNode().put(col, true);
				dataArrayNode.add(dataObject);
			});
		}
	}

	private boolean checkPiiColumn(String str) {
		boolean flag = false;
		if (RegexUtils.isCreditCardNum(str)) {
			flag = true;
		} else if (RegexUtils.isCreditCardCvv(str)) {
			flag = true;
		} else if (RegexUtils.isEmailId(str)) {
			flag = true;
		} else if (RegexUtils.isSsn(str)) {
			flag = true;
		} else if(RegexUtils.isPassportNum(str)) {
			flag = true;
		} else if(RegexUtils.isPhNumberItut(str)) {
			flag = true;
		} else if(RegexUtils.isPhNumberEpp(str)) {
			flag = true;
		}

		return flag;
	}


	private String chooseDatabase(Projects project, String type) {
		return type.equals(RAW) ? project.getRawDb() : project.getProcessedDb();
	}

	public ArrayNode getFileContentForDownload(Projects project, String athenaTableName)
			throws Exception {

		final QueryExecutionContext queryContext = new QueryExecutionContext();
		queryContext.setDatabase(project.getRawDb());
		final String athenaQuery = selectQuery(athenaTableName, null);

		// TODO EncryptionConfiguration
		log.info("Query to athena : {}", athenaQuery);
		final String queryExecutionId = athenaPrepareQuery(queryContext, athenaQuery);
		final GetQueryResultsResult queryResults = athenaRunQuery(queryExecutionId);

		final ResultSet dataResultSet = queryResults.getResultSet();
		final List<Row> rows = dataResultSet.getRows();

		log.info(
				"Started processing the records retrieved from Athena Table. Number of records to process is: {} rows.",
				rows.size());

		int count = 0;
		for (Row row : rows) {
			// Row represents a complete tuple/record in the table.
			++count;
			List<Datum> rowData = row.getData();
			// Datum represents a data for every column in the row.
			for (Datum datum : rowData) {
				log.info(datum.getVarCharValue());
			}
			if (count == 10) {
				break;
			}
		}

		return null;
	}

	public String generatePresignedUrl(String bucketName, String objectKey) throws Exception {

		log.info(
				"Generate presigned URL request is received for S3 Bucket Name: {} and Object Key: {}",
				bucketName,
				objectKey);

		Date expiration = new Date();
		long expTimeMillis = expiration.getTime();
		expTimeMillis += 1000 * 60 * 60;
		expiration.setTime(expTimeMillis);

		GeneratePresignedUrlRequest generatePresignedUrlRequest =
				new GeneratePresignedUrlRequest(bucketName, objectKey)
				.withMethod(HttpMethod.GET)
				.withExpiration(expiration);
		URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);

		log.info("Presigned URL is: {} and it expires in one hour.", url);

		return url.toString();
	}

	public Map<String, String> readMetadata(List<String> lines) {
		if (lines != null && !lines.isEmpty() && lines.size() == 2) {
			String headerLine = lines.get(0);
			log.info("Header line is: {}", headerLine);
			String dataLine = lines.get(1);
			log.info("Data line is: {}", dataLine);
			Map<String, String> columnMetadata = getDataTypes(headerLine, dataLine);
			log.info("Column Metadata is: {}", columnMetadata);

			return columnMetadata;
		}
		return Collections.emptyMap();
	}

	/**
	 * Utility method to identify the datatypes of each column.
	 * @param headerLine
	 * @param dataLine
	 * @return
	 */
	public static Map<String, String> getDataTypes(final String headerLine, final String dataLine) {
		Map<String, String> columnMetadata = new LinkedHashMap<>();
		String[] headers = headerLine.split(QsConstants.DELIMITER_SPLIT_PATTERN);
		String[] dataValues = dataLine.split(QsConstants.DELIMITER_SPLIT_PATTERN);
		if (headerLine != null && headerLine.indexOf(QsConstants.DELIMITER) != -1 && dataLine != null 
				&& dataLine.indexOf(QsConstants.DELIMITER) != -1) {   
			for (int i = 0; i < dataValues.length; i++) {
				//columnMetadata.put(headers[i], getDataType(dataValues[i]));
				columnMetadata.put(headers[i], "string");
			}
			return columnMetadata;
		}else if(headers.length == 1 && dataValues.length == 1){ //Added  condition  for Single column for PII
			columnMetadata.put(headerLine, "string");
			return columnMetadata;
		}

		return Collections.emptyMap();
	}

	/**
	 * Utility method used to identify the Data Type.
	 * @param value
	 * @return
	 */
	public static String getDataType(String value) {
		String dataType = "string"; // default type

		if(value != null && !value.isEmpty()) {
			if (RegexUtils.isInteger(value)) {
				dataType = "int";
			} else if (RegexUtils.isLong(value)) {
				dataType = "bigint";
			} else if (RegexUtils.isDouble(value)) {
				dataType = "double";
			} else if (ValidatorUtils.isDate(value)) {
				dataType = "timestamp";
			} else if (RegexUtils.isAlphaNumeric(value)) {
				dataType = "string";
			} else {
				dataType = "string";
			}
		}

		//if (!flag && ValidatorUtils.isCurrency(value)) {
		//flag = true;
		//dataType = "currency";
		//}

		return dataType;
	}

	/**
	 * Utility method used to normalize the String by replacing all the special chars with "_" chars
	 * to ensure the string is Athena compatible.
	 * 
	 * Athena table name can have only "_" as a special char
	 * 
	 * @param tableName
	 * @return
	 */
	public String normalizeAthenaTableName(final String tableName) {
		return (tableName != null) ? tableName.replaceAll(RegexPatterns.SPECIAL_CHARS.getPattern(), "_").toLowerCase() : "";
	}

	/**
	 * Utility method to prepare the Athena Create Table query
	 * 
	 * @param tableName
	 * @param s3FileLocation
	 * @param columnMetadata
	 * @return
	 */
	private String prepareAthenaCreateQuery(String partitionName, String tableName, String s3FileLocation,
			Map<String, String> columns) {
		// Query is prepared in lower case to meet Athena Standards...

		log.info("Started preparing Create Table query for Athena...");

		StringBuilder sb = new StringBuilder(128);
		if (columns != null && !columns.isEmpty()) {
			sb.append("CREATE EXTERNAL TABLE IF NOT EXISTS `");
			sb.append(tableName);
			sb.append("` (");
			columns.keySet().stream().forEach(column -> {
				sb.append("`");
				sb.append(column.toLowerCase());
				//sb.append("` " + column.getValue() + ",");
				sb.append("` string,");
			});

			// Remove the last comma that is appended above..
			sb.deleteCharAt(sb.length() - 1);

			sb.append(") ");
			if(partitionName != null) {
				sb.append(" PARTITIONED BY (partition_0 string) ");
			}

			sb.append(" ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' ");
			sb.append(" WITH SERDEPROPERTIES ( 'separatorChar' = ',', 'quoteChar' = '\"', 'escapeChar' = '\\\\') ");

			//sb.append(" ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' ESCAPED BY '\\' ");
			sb.append(" STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' ");
			sb.append(" OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ");
			sb.append(" LOCATION '");
			sb.append(s3FileLocation);
			sb.append("' ");
			sb.append(" TBLPROPERTIES ('skip.header.line.count'='1', 'averageRecordSize'='50', 'recordCount'='50');");
		}

		log.info("Query to be executed in Athena is: {}", sb.toString());

		return sb.toString();
	}


	public String prepareAthenaCreateQuery(String partitionName, String tableName, 
			String s3FileLocation, List<List<String>> columns, List<MetadataReference> metadataRef) {
		// Query is prepared in lower case to meet Athena Standards...

		log.info("Started preparing Create Table query for Athena...");
		StringBuilder sb = new StringBuilder(128);
       try {
    	   
		if(metadataRef != null && metadataRef.size() > 0) {
			Map<String, String> refMap = new LinkedHashMap<>();
			metadataRef.stream().forEach(ref -> {
				refMap.put(ref.getSourceColumnnameType(), ref.getDestinationColumnnameType());
			});
			if (columns != null && !columns.isEmpty()) {
				sb.append("CREATE EXTERNAL TABLE IF NOT EXISTS `");
				sb.append(tableName);
				sb.append("` (");
				columns.forEach(column -> {
					sb.append("`");
					sb.append(column.get(0).toLowerCase());
					sb.append("` " + (refMap.get(column.get(1)) != null ? refMap.get(column.get(1)) : "string") + ",");
					//sb.append("` string,");
				});

				// Remove the last comma that is appended above..
				sb.deleteCharAt(sb.length() - 1);

				sb.append(") ");
				if(partitionName != null) {
					sb.append(" PARTITIONED BY (partition_0 string) ");
				}

				sb.append(" ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' ");
				sb.append(" WITH SERDEPROPERTIES ( 'separatorChar' = ',', 'quoteChar' = '\"', 'escapeChar' = '\\\\') ");

				//sb.append(" ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' ESCAPED BY '\\' ");
				sb.append(" STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' ");
				sb.append(" OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ");
				sb.append(" LOCATION '");
				sb.append(s3FileLocation);
				sb.append("' ");
				sb.append(" TBLPROPERTIES ('skip.header.line.count'='1', 'averageRecordSize'='50', 'recordCount'='50')");
			}

			log.info("Query to be executed in Athena is: {}", sb.toString());
		}
		
       }catch(final Exception e) {
     	  e.printStackTrace(); 
       }
		return sb.toString();
	}
	
	public String prepareAthenaCreateQuery(String partitionName, String tableName, 
			String s3FileLocation, Map<String, String> columns, List<MetadataReference> metadataRef) {
		// Query is prepared in lower case to meet Athena Standards...

		log.info("Started preparing Create Table query for Athena...");
		StringBuilder sb = new StringBuilder(128);
       try {
    	   
		if(metadataRef != null && metadataRef.size() > 0) {
			Map<String, String> refMap = new LinkedHashMap<>();
			metadataRef.stream().forEach(ref -> {
				refMap.put(ref.getSourceColumnnameType(), ref.getDestinationColumnnameType());
			});
			if (columns != null && !columns.isEmpty()) {
				sb.append("CREATE EXTERNAL TABLE IF NOT EXISTS `");
				sb.append(tableName);
				sb.append("` (");
				columns.entrySet().stream().forEach(column -> {
					sb.append("`");
					sb.append(column.getKey().toLowerCase());
					sb.append("` " + (refMap.get(column.getValue()) != null ? refMap.get(column.getValue()) : "string") + ",");
					//sb.append("` string,");
				});

				// Remove the last comma that is appended above..
				sb.deleteCharAt(sb.length() - 1);

				sb.append(") ");
				if(partitionName != null) {
					sb.append(" PARTITIONED BY (partition_0 string) ");
				}

				sb.append(" ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' ");
				sb.append(" WITH SERDEPROPERTIES ( 'separatorChar' = ',', 'quoteChar' = '\"', 'escapeChar' = '\\\\') ");

				//sb.append(" ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' ESCAPED BY '\\' ");
				sb.append(" STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' ");
				sb.append(" OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ");
				sb.append(" LOCATION '");
				sb.append(s3FileLocation);
				sb.append("' ");
				sb.append(" TBLPROPERTIES ('skip.header.line.count'='1', 'averageRecordSize'='50', 'recordCount'='50')");
			}

			log.info("Query to be executed in Athena is: {}", sb.toString());
		}
		
       }catch(final Exception e) {
     	  e.printStackTrace(); 
       }
		return sb.toString();
	}

	public boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public List<String> getModifiedColHeaders(String headerLine){
		headerLine = headerLine.replaceAll("a_[0-9]*_", "");
		String[] colHeadersArr = headerLine.split(",");
		List<String> colHeaders = Arrays.asList(colHeadersArr);
		List<String> modifiedColHeaders = new ArrayList<>();

		int i = 0;
		for(String col : colHeaders) {
			col = col.trim();
			if(!modifiedColHeaders.contains(col)) {
				modifiedColHeaders.add(col);
			} else {
				modifiedColHeaders.add(col+"_"+(i++));
			}
		}

		return modifiedColHeaders;
	}

	public String getCheckHeader(String header) {

		if(header == null || (header.indexOf(",") == -1)) {
			return header;
		}

		String[] strarr = header.split(",");
		List<String> strList = Arrays.asList(strarr);
		String checkHeader = "";

		StringBuilder sb = new StringBuilder();
		for(String s : strList) {
			s = s.trim();
			if(s.endsWith("_0")) {
				break;
			}
			sb.append(s);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);

		checkHeader = sb.toString();
		log.info("CheckHeader used for removing the unnecessary row is: {}", checkHeader);

		return checkHeader;
	}

	public void handleAthenaProcessForCleanseJob(final String bucketName, final String folderName, final String partitionName, 
			final String dbName, FileMetaDataAwsRef fileMetaDataAwsRef) throws Exception{
		Instant now = Instant.now();
		S3Object s3Object = null;
		try {
			String path = folderName+"/"+partitionName;

			// Fetch the S3 Object
			s3Object = fetchObject(bucketName, path);

			String s3FileLocation = "s3://" + bucketName + "/" + folderName + "/" + partitionName;
			if(s3Object != null) {
				Map<String, String> columnMetadata = new HashMap<>();
				final QueryExecutionContext queryContext = new QueryExecutionContext();
				queryContext.setDatabase(dbName);

				String athenaCheckQuery = "SHOW TABLES IN " + dbName.toLowerCase() + " '" + folderName.toLowerCase() + "'";
				log.info("Query to athena to check whether table is present in database '{}' or not",
						athenaCheckQuery);
				final String checkQueryExeId = athenaPrepareQuery(queryContext, athenaCheckQuery);
				final GetQueryResultsResult checkQueryResults = athenaRunQuery(checkQueryExeId);
				final ResultSet dataResultSet = checkQueryResults.getResultSet();
				final List<Row> dataRowsList = dataResultSet.getRows();

				List<String> lines = new ArrayList<>();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
					// Limiting to only reading two lines, with the assumption that
					// the first line is a header line, followed by data line.
					br.lines().limit(2).forEach(lines::add);
				} catch (Exception e) {
					e.printStackTrace();
				}

				columnMetadata = readMetadata(lines);
				// Update the File MetadataAWS Ref table with the column metadata..
				List<ColumnMetaData> cols = new ArrayList<>();
				List<String> columnNames = new ArrayList<>();
				columnMetadata.keySet().stream().forEach((me) -> {
					cols.add(new ColumnMetaData(me, "string"));
					columnNames.add(me);
				});

				ObjectMapper mapper = new ObjectMapper();
				fileMetaDataAwsRef.setFileMetaData(mapper.writeValueAsString(cols));
				awsDataService.save(fileMetaDataAwsRef);

				if (dataRowsList == null || dataRowsList.isEmpty()) {
					// Table is not present in Athena. So, first create it and then add the partition later.
					createTableInAthena(columnMetadata, dbName, partitionName, folderName, s3FileLocation);

					log.info("Created a new table in Athena for this Cleanse Job...");
				} 
				else {
					// Table is already present, now check the Table structure. If mismatched, we have to 
					// add/drop columns in the table excluding the partition.
					// Note: Drop columns is not supported currently in Athena.

					String tableColsQuery = "SHOW COLUMNS IN " + dbName.toLowerCase() + "." + folderName.toLowerCase();
					log.info("Query to athena to retrieve columns in table '{}'", tableColsQuery);
					final String tableColsQueryExeId = athenaPrepareQuery(queryContext, tableColsQuery);
					final GetQueryResultsResult tableColsQueryResults = athenaRunQuery(tableColsQueryExeId);
					final ResultSet rs = tableColsQueryResults.getResultSet();
					final List<Row> dataRows = rs.getRows();

					List<String> athenaTableColNames = new ArrayList<>();
					for(Row row : dataRows) {
						row.getData().stream().forEach((data) -> {
							String cn = data.getVarCharValue().trim();
							log.info("Column Name: {}", cn);
							if(!"partition_0".equals(cn)) {
								athenaTableColNames.add(cn);
							}
						});
					}

					// Compare the Column Names now..
					log.info("Column names in the Athena Table: {}", athenaTableColNames);
					log.info("Column names after the cleanse job is complete: {} ", columnNames);
					if(!athenaTableColNames.equals(columnNames)) {
						try {

							// Remove the existing columns and only retain the new ones, so that they are added to the Athena table..
							columnNames.removeAll(athenaTableColNames);

							athenaAddColumnQuery(queryContext, folderName.toLowerCase(), columnNames);
						}catch(AmazonClientException|InterruptedException e) {
							// 
						}
					}
				}

				if (partitionName != null) {
					addPartitionToTableInAthena(bucketName, dbName, folderName, partitionName);
					log.info("Added a new Partition in Athena for this Cleanse Job");
				}
			}else {
				log.info("Failed to perform Athena Table operation for this cleanse job, because of missing Processed/Cleansed file in S3 Location: {}", s3FileLocation);
			}



		} catch (AmazonClientException|InterruptedException exception) {
			throw new RuntimeException("Error while uploading file." + exception.getMessage());
		} finally {
			if(s3Object != null) {
				try {
					s3Object.close();
				}catch(IOException e) {

				}
			}
		}

		log.info("Elapsed time to complete Athena table processing is: {} msecs",
				(Duration.between(now, Instant.now()).toMillis()));
	}

	private void athenaDropColumnQuery(QueryExecutionContext queryContext, String tableName, String columnName) throws AmazonClientException, InterruptedException{
		StringBuilder sbr = new StringBuilder();
		sbr.append("ALTER TABLE ");
		sbr.append(tableName);
		sbr.append(" DROP COLUMN ");
		sbr.append(columnName);

		String dropColQuery = sbr.toString();

		log.info("Query to athena to drop column in table is: {}", dropColQuery);
		final String dropColsQueryExeId = athenaPrepareQuery(queryContext, dropColQuery);
		final GetQueryResultsResult dropColsQueryResults = athenaRunQuery(dropColsQueryExeId);
		final ResultSet rs1 = dropColsQueryResults.getResultSet();
	}

	public void athenaDropTableQuery(String dbName, String tableName) throws AmazonClientException, InterruptedException{
		StringBuilder sbr = new StringBuilder();
		sbr.append("DROP TABLE IF EXISTS ");
		sbr.append(normalizeAthenaTableName(tableName));

		String dropTableQuery = sbr.toString();

		log.info("Query to athena to drop table in Athena is: {}", dropTableQuery);

		final QueryExecutionContext queryContext = new QueryExecutionContext();
		queryContext.setDatabase(dbName);
		final String dropTblQueryExeId = athenaPrepareQuery(queryContext, dropTableQuery);
		final GetQueryResultsResult dropTblQueryResults = athenaRunQuery(dropTblQueryExeId);
		final ResultSet rs1 = dropTblQueryResults.getResultSet();
	}

	private void athenaDropDatabaseQuery(QueryExecutionContext queryContext, String databaseName) throws AmazonClientException, InterruptedException{
		StringBuilder sbr = new StringBuilder();
		sbr.append("DROP DATABASE IF EXISTS ");
		sbr.append(databaseName);
		sbr.append(" CASCADE ");

		String dropDbQuery = sbr.toString();
		log.info("Query to athena to drop database is: {}", dropDbQuery);
		final String dropColsQueryExeId = athenaPrepareQuery(queryContext, dropDbQuery);
		final GetQueryResultsResult dropColsQueryResults = athenaRunQuery(dropColsQueryExeId);
		final ResultSet rs1 = dropColsQueryResults.getResultSet();
	}

	private void athenaAddColumnQuery(QueryExecutionContext queryContext, String tableName, List<String> columnNames) throws AmazonClientException, InterruptedException{
		StringBuilder sbr = new StringBuilder();
		sbr.append("ALTER TABLE ");
		sbr.append(tableName);
		sbr.append(" ADD COLUMNS (");

		columnNames.stream().forEach((col) -> {
			sbr.append(col);
			sbr.append(" string,");
		});

		sbr.deleteCharAt(sbr.length()-1);
		sbr.append(") ");

		String addColQuery = sbr.toString();

		log.info("Query to athena to add column(s) in table is: {}", addColQuery);
		final String addColsQueryExeId = athenaPrepareQuery(queryContext, addColQuery);
		final GetQueryResultsResult addColsQueryResults = athenaRunQuery(addColsQueryExeId);
		final ResultSet rs1 = addColsQueryResults.getResultSet();
	}

	public void handlePostFileDeleteActions(Projects project, int folderId, String folderName, boolean emptyFolder, String partitionName, String fileName) throws Exception{
		String bucketName = project.getBucketName()+"-"+QsConstants.RAW;
		String objectKey = String.format("%s/%s/%s", folderName, partitionName, fileName);
		log.info("Object Key to be deleted: {}", objectKey);

		// Delete the Raw file from the S3 bucket.
		deleteObject(bucketName, objectKey);

		// Drop the partition from Athena now.
		deletePartitionForTableInAthena(project.getRawDb(), folderName, partitionName);

		if(emptyFolder) {
			// Since there are no partitions in this Athena table, delete the Athena table..
			athenaDropTableQuery(project.getRawDb(), folderName.toLowerCase());
		}
	}

	public void cleanProjectsInternal(List<Projects> deletedProjects) {
		Instant now = Instant.now();
		log.info("----> Started deleted projects cleanup operation in AWS and Athena..");

		if(deletedProjects != null && !deletedProjects.isEmpty()) {
			deletedProjects.stream().forEach((deletedProject) -> {
				cleanProject(deletedProject);
			});
		}

		log.info("----> Completed deleted projects cleanup operation in AWS and Athena..");
		log.info("Elapsed time in deleted projects cleanup operation is: {} msecs", (Duration.between(now, Instant.now()).toMillis()));
	}

	public void cleanProject(Projects deletedProject) {
		try {
			log.info("Cleaning project: {}", deletedProject.getProjectDisplayName());

			log.info("Started cleaning and deleting the buckets associated with the Project...");
			cleanAwsBucket(deletedProject.getBucketName()+"-"+RAW);
			cleanAwsBucket(deletedProject.getBucketName()+"-"+PROCESSED);
			cleanAwsBucket(deletedProject.getBucketName()+"-"+ENG);
			log.info("Completed cleaning and deleting the buckets associated with the Project...");

			log.info("Started dropping the Athena Databases associated with the Project...");
			deleteAthenaDb(deletedProject.getRawDb());
			deleteAthenaDb(deletedProject.getProcessedDb());
			deleteAthenaDb(deletedProject.getEngDb());
			log.info("Completed dropping the Athena Databases associated with the Project...");

			log.info("Cleaned project: {}", deletedProject.getProjectDisplayName());
		}catch(Exception e) {
			log.info("Exception occured while deleting the project and its contents...");
		}
	}

	private void cleanAwsBucket(String bucketName) {
		try {
			ObjectListing objList = amazonS3Client.listObjects(bucketName);
			List<S3ObjectSummary> objSummaries = objList.getObjectSummaries();

			if(objSummaries != null && !objSummaries.isEmpty()) {
				objSummaries.stream().forEach((obj) -> {
					log.info("Deleting the Object {} from Bucket: {}", obj.getKey(), bucketName);
					amazonS3Client.deleteObject(bucketName, obj.getKey());
				});
			}

			amazonS3Client.deleteBucket(bucketName);
			log.info("Cleaned and subsequently deleted the bucket: {}", bucketName);
		}catch(Exception e) {
			e.printStackTrace();
			log.info("Failed to clean the bucket: {}", bucketName);
		}
	}

	private void deleteAthenaDb(String databaseName) throws InterruptedException{
		try {
			final QueryExecutionContext queryContext = new QueryExecutionContext();
			queryContext.setDatabase(databaseName);
			athenaDropDatabaseQuery(queryContext, databaseName);
		}catch(Exception e) {
			e.printStackTrace();
			log.info("Failed to drop the Athena Database: {}", databaseName);
		}
	}

	public void listMetrics() throws Exception{

		log.info("\n\n\n-------> Invoked the list metrics method:\n");
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIA3XKNU2WB6YOAP7XC", "qzovqrYq4338BgDCB1pHP3Gouf0zbNa5bgOrt5e5");
		final AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().
				withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.EU_WEST_2).build();

		ListMetricsRequest request = new ListMetricsRequest()
				.withNamespace("AWS/ElasticMapReduce");
		//.withMetricName("CPUUtilization");

		boolean done = false;

		while (!done) {
			ListMetricsResult response = cw.listMetrics(request);

			for (Metric metric : response.getMetrics()) {
				log.info("Retrieved metric {}", metric.getMetricName());
				List<Dimension> dimensions = metric.getDimensions();

				if (dimensions != null && !dimensions.isEmpty()) {
					for (Dimension dim : dimensions) {
						log.info("Dimension Name: {} and Dimension Value: {}", dim.getName(), dim.getValue());
					}
				}
			}

			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}
		}

		log.info("\n-------> Completed the list metrics method.\n\n\n");
	}

	public String getEmrMetrics() throws Exception{
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIA3XKNU2WB6YOAP7XC", "qzovqrYq4338BgDCB1pHP3Gouf0zbNa5bgOrt5e5");
		final AmazonCloudWatch cloudWatch = AmazonCloudWatchClientBuilder.standard().
				withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.EU_WEST_2).build();

		String data = getMetricsRequestInput();
		log.info("Metric input is: {}", data);
		ObjectMapper objectMapper = new ObjectMapper();
		GetMetricDataRequest requestObj = objectMapper.readValue(data, GetMetricDataRequest.class);
		GetMetricDataResult result = cloudWatch.getMetricData(requestObj);
		String metricsResp = objectMapper.writeValueAsString(result);
		log.info("Metric output is: {}", metricsResp);

		return metricsResp;
	}

	private String getMetricsRequestInput() {
		long ONE_MINUTE_IN_MILLIS = 60000;
		Calendar date = Calendar.getInstance();
		long t = date.getTimeInMillis();
		Date afterAddingTenMins = new Date(t - (10 * 24 * 60 * ONE_MINUTE_IN_MILLIS)); // 10 Days

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(" \"startTime\":\"");
		sb.append(afterAddingTenMins.getTime());
		sb.append("\", ");
		sb.append(" \"endTime\":\"");
		sb.append(new Date().getTime());
		sb.append("\", ");
		sb.append(" \"metricDataQueries\":");
		sb.append(" [");
		sb.append("     {");
		sb.append("         \"id\":\"m1\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"TaskNodesRequested\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m2\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"MemoryTotalMB\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m3\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"MemoryAllocatedMB\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m4\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"HDFSBytesRead\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m5\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"MRTotalNodes\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m6\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"MRUnhealthyNodes\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m7\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"CapacityRemainingGB\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m8\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"TotalLoad\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m9\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"S3BytesRead\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m10\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"S3BytesWritten\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m11\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"CapacityRemainingGB\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Count\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m12\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"YARNMemoryAvailablePercentage\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Percent\"");
		sb.append("         }");
		sb.append("     },");
		sb.append("     {");
		sb.append("         \"id\":\"m13\",");
		sb.append("         \"metricStat\":");
		sb.append("         {");
		sb.append("             \"metric\":");
		sb.append("             {\"dimensions\":[{\"name\":\"JobFlowId\",\"value\":\"j-YV6EY16JS92M\"}],");
		sb.append("                 \"namespace\":\"AWS/ElasticMapReduce\",");
		sb.append("                 \"metricName\":\"LiveDataNodes\"");
		sb.append("             },");
		sb.append("             \"period\":43200,");
		sb.append("             \"stat\":\"Average\",\"unit\":\"Percent\"");
		sb.append("         }");
		sb.append("     }");
		sb.append(" ]");
		sb.append("}");

		return sb.toString();
	}

	public boolean checkColDatatypeCasting(String dbName, String folderName, String colName, String targetDType) {
		boolean allowed = true;
		try {
			final String query = String.format("SELECT CAST(%s as %s) from %s", colName, targetDType, folderName);
			log.info("Query to Athena: {}", query);
			final QueryExecutionContext queryContext = new QueryExecutionContext();
			queryContext.setDatabase(dbName);
			final String queryExeId = athenaPrepareQuery(queryContext, query);
			final GetQueryResultsResult dropTblQueryResults = athenaRunQuery(queryExeId);
			final ResultSet rs1 = dropTblQueryResults.getResultSet();
		}catch(Exception e) {
			log.error("Failed to typecast Column: "+ colName+" to target Datatype: "+ targetDType+", because: "+ e.getMessage());
			allowed = false;
		}

		return allowed;
	}

	public String storeSQLFileIntoS3Async(final String bucketName, 
			ByteArrayOutputStream stream, final String fileName, final String contentType) throws Exception{
		log.info("\nReceived file upload request for the udf file: {}", fileName);

		Instant now = Instant.now();
		TransferManager transferManager = null;

		try {
			ByteArrayInputStream  inputFile = new ByteArrayInputStream(stream.toByteArray());
			final ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(stream.toByteArray().length);

			log.info("Bucket Name: {} and File Name: {}", bucketName, fileName);

			// Enable Transfer Acceleration before uploading the file to the bucket.
			amazonS3Client.setBucketAccelerateConfiguration(
					new SetBucketAccelerateConfigurationRequest(bucketName,
							new BucketAccelerateConfiguration(
									BucketAccelerateStatus.Enabled)));

			// Verify whether the transfer acceleration is enabled on the bucket or not...
			String accelerateStatus = amazonS3Client.getBucketAccelerateConfiguration(
					new GetBucketAccelerateConfigurationRequest(bucketName))
					.getStatus();

			log.info("Transfer acceleration status of the bucket {} is {}", bucketName, accelerateStatus);

			// Approach 1:
			transferManager = TransferManagerBuilder.standard()
					.withMultipartUploadThreshold((long) (1 * 1024 * 1025))
					.withS3Client(amazonS3Client)
					.build(); // TODO: We have to create a Singleton instance of TransferManager instance and close it as part of shutdown hook.


			Upload upload = transferManager.upload(new PutObjectRequest(bucketName, fileName, inputFile, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));

			upload.waitForCompletion();
			// After the upload is complete, release the TransferManager resources by calling shutdownNow() API of AWS SDK..
			transferManager.shutdownNow(false); // Shutdown only the TransferManager and not the underlying S3Client instance.

			log.info("Uploaded udf file to destination : {}/{}", bucketName, fileName);
			log.info("Elapsed time to upload udf file in AWS S3 bucket is: {} msecs", (Duration.between(now, Instant.now()).toMillis()));

			URL s3FileUrl = amazonS3Client.getUrl(bucketName, fileName);
			return s3FileUrl.toString();
		}
		catch (AmazonClientException | InterruptedException exception) {
			throw new RuntimeException("Error while uploading udf file." + exception.getMessage());
		}
	}

}
