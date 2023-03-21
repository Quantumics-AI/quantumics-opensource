/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.livy;

import static ai.quantumics.api.constants.QsConstants.ENG_OP;
import static ai.quantumics.api.constants.QsConstants.PROCESSED;
import static ai.quantumics.api.constants.QsConstants.RAW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.amazonaws.services.glue.model.JobRunState;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.model.EngFlowMetaDataAwsRef;
import ai.quantumics.api.model.FileMetaDataAwsRef;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsUdf;
import ai.quantumics.api.model.RunJobStatus;
import ai.quantumics.api.req.ColumnMetaData;
import ai.quantumics.api.req.DataFrameRequest;
import ai.quantumics.api.req.EngFileOperationRequest;
import ai.quantumics.api.req.FileJoinColumnMetadata;
import ai.quantumics.api.req.JoinOperationRequest;
import ai.quantumics.api.req.Metadata;
import ai.quantumics.api.req.UdfOperationRequest;
import ai.quantumics.api.req.UdfReqColumn;
import ai.quantumics.api.service.FileMetaDataAwsService;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.ProjectService;
import ai.quantumics.api.service.RunJobService;
import ai.quantumics.api.service.UdfService;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.MetadataHelper;
import ai.quantumics.api.util.QsUtil;
import ai.quantumics.api.vo.BatchJobInfo;
import ai.quantumics.api.vo.QsFileContent;
import ai.quantumics.api.vo.S3FileUploadResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LivyActions {

    private Random random = new Random();
    private Gson gson = new Gson();

    private final QsUtil qsUtil;
    private final DbSessionUtil dbUtil;
    private final FileService fileService;
    private final FolderService folderService;
    private final QsCustomLivyClient livyClient;
    private final ProjectService projectService;
    private final FileMetaDataAwsService fileMetaDataAwsService;
    private final AwsAdapter awsAdapter;
    private final MetadataHelper metadataHelper;
    private final RunJobService runJobService;
    private final UdfService udfService;

    @Value("${qs.s3.result.store}")
    private String resultStore;

    @Value("${qs.livy.base.batches.url}")
    private String livyBaseBatchesUrl;

    @Value("${spark.executorMemory}")
    private String executorMemory;

    @Value("${spark.driverMemory}")
    private String driverMemory;

    @Value("${s3.udf.bucketName}")
    private String udfBucketName;
    // private final SimpMessageSendingOperations messagingTemplate;

    public LivyActions(
            final DbSessionUtil sessionUtil,
            @Lazy final QsUtil qsutil,
            final QsCustomLivyClient livyClientCi,
            final ProjectService projectservice,
            @Lazy final FolderService folderservice,
            @Lazy final FileService fileservice,
            FileMetaDataAwsService fileMetaDataAwsService,
            AwsAdapter awsAdapter,
            MetadataHelper metadataHelper,
            RunJobService runJobService,
            final UdfService udfService) {
        dbUtil = sessionUtil;
        qsUtil = qsutil;
        livyClient = livyClientCi;
        projectService = projectservice;
        folderService = folderservice;
        fileService = fileservice;
        this.fileMetaDataAwsService = fileMetaDataAwsService;
        this.awsAdapter = awsAdapter;
        this.metadataHelper = metadataHelper;
        this.runJobService = runJobService;
        this.udfService = udfService;
    }

    @Async("qsThreadPool")
    public void initializeLivyAsync(int userId) {
        final String sessionName = "project0" + userId;
        initializeLivyInternal(sessionName);
    }

    public void initializeLivy(int userId) {
        final String sessionName = "project0" + userId;
        initializeLivyInternal(sessionName);
    }

    private void initializeLivyInternal(final String sessionName) {
        int sessionId = livyClient.getOrCreateApacheLivySessionId(sessionName);
        String sessionState = livyClient.getApacheLivySessionState(sessionId);

        log.info("Initialized livy in the backend. Livy Session Id is: {} and Session State is: {}", sessionId, sessionState);
    }

    private void changeSchema(final String newSchema) {
        dbUtil.changeSchema(newSchema);
    }

    private void fillSparkQuery(
            final JsonObject importJson,
            final JsonObject currentJson,
            final JsonObject finalResult,
            final JsonObject jsonLimit,
            final JsonObject currentSqlShow,
            final JsonObject dfAliasCmd,
            final JsonObject dfAliasResultCmd,
            final String dfName, final String dfAlias,
            List<String> commandsSequence) {

        pySparkImportJson(importJson);
        String jsonLimitStr = dfName + "_limit =" + dfName + ".limit(200)";
        String currentJsonStr = dfName + "_results = " + dfName + "_limit.toJSON().collect()";
        String currentSqlShowStr = dfName + "_result = [json.loads(x) for x in " + dfName + "_results]";
        String pyDfToPandasStr = dfName + ".toPandas().to_json(orient='records')";

        String dfAliasCmdStr = dfAlias + " = " + dfName;
        //String dfAliasCmdResultStr = dfAlias+"_result"+ " = "+pyDfToPandasStr;

        //String finalResultStr = dfName+"_json_resp = json.dumps(" + dfName + "_result, sort_keys=False, indent=4)";
        String removeEventPrefix = dfName + "_json_resp = re.sub(\"(a_[0-9]*_)\", \"\", json.dumps([json.loads(x) for x in " + dfName + ".toJSON().collect()], sort_keys=False, indent=4))";
        //String finalResultStr = "print(" + dfName + ".show(200))";
        String pyFinalResultStr = "print(" + pyDfToPandasStr + ")";

        jsonLimit.addProperty("code", jsonLimitStr);
        currentJson.addProperty("code", currentJsonStr);
        currentSqlShow.addProperty("code", currentSqlShowStr);
        dfAliasCmd.addProperty("code", dfAliasCmdStr);
        //dfAliasResultCmd.addProperty("code", dfAliasCmdResultStr);
        finalResult.addProperty("code", pyFinalResultStr);

        if (commandsSequence != null) {
            //commandsSequence.add(jsonLimitStr);
            //commandsSequence.add(currentJsonStr);
            //commandsSequence.add(currentSqlShowStr);
            //commandsSequence.add(finalResultStr);
            commandsSequence.add(removeEventPrefix);
        }
    }

    private void pySparkSortJsonDump(JsonObject finalResult, String dfName, List<String> commandsSequence) {
        String finalResStr = dfName + "_json_resp=json.dumps(" + dfName + "_result" + ", sort_keys=False, indent=4)";
        String removeEventPrefix = dfName + "_json_resp = re.sub(\"(a_[0-9]*_)\", \"\"," + dfName + "_json_resp)";

        if (commandsSequence != null) {
            commandsSequence.add(finalResStr);
            commandsSequence.add(removeEventPrefix);
        }
        finalResult.addProperty("code", finalResStr);
    }

    private void pySparkLoadJson(JsonObject currentSqlShow, String dfName, List<String> commandsSequence) {
        String currSqlStr = dfName + "_result = [json.loads(x) for x in " + dfName + "_results]";
        if (commandsSequence != null) {
            commandsSequence.add(currSqlStr);
        }

        currentSqlShow.addProperty("code", currSqlStr);
    }

    private void pySparkToJson(JsonObject currentJson, String dfName, List<String> commandsSequence) {
        String currJsonStr = dfName + "_results = " + dfName + ".limit(200).toJSON().collect()";
        //String currJsonStr = dfName + "_results = " + dfName + "_limit.toPandas()";

        if (commandsSequence != null) {
            commandsSequence.add(currJsonStr);
        }

        currentJson.addProperty("code", currJsonStr);
    }

    private void pySparkApplyLimit(JsonObject jsonLimit, String dfName, List<String> commandsSequence) {
        String limitQuery = dfName + "_limit = " + dfName + ".na.fill('null').limit(200)";

        if (commandsSequence != null) {
            commandsSequence.add(limitQuery);
        }
        jsonLimit.addProperty("code", limitQuery);
    }

    private void pySparkImportJson(JsonObject importJson) {
        importJson.addProperty("code", "import json");
    }

    private void pySparkInitializeLivy(JsonObject importJson) {
        importJson.addProperty("code", "print('Initialize livy...')");
    }

    private void pySparkJoinQuery(JsonObject currentJoin, StringBuilder joinQuery) {
        currentJoin.addProperty("code", joinQuery.toString());
    }

    private JsonNode getJsonNode(final JsonElement elementTemp) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode readTree;
        final String atr = JSONValue.parse(elementTemp.toString()).toString();
        readTree = mapper.readTree(atr);
    /*for (int i = 0; i < readTree.size(); i++) {
      final JsonNode jsonNode = readTree.get(i);
      final ObjectNode object = (ObjectNode) jsonNode;
      if (i == 0) {
        object.removeAll();
      }
      object.remove("partition_0");
    }*/

        return readTree;
    }

    public JsonNode initLivyContext(
            final Object requestObj,
            final boolean writeToCloud,
            Projects project,
            int flowJobId, StringBuilder fileContents, boolean fileDataFlag) {
        final JsonObject currentDb = new JsonObject();
        final JsonObject currentTable = new JsonObject();
        final JsonObject filterCondition = new JsonObject();
        JsonElement response;
        JsonNode readTree = null;
        log.info("---->Engineering Flow Job Id: {}", flowJobId);

        try {
            changeSchema(project.getDbSchemaName());
            DataFrameRequest wsRequest = null;
            EngFileOperationRequest engFlowOpRequest = null;
            String dfName = "";
            String dfAlias = "";

            String fileType = "";
            String tableName = null;
            String partition = null;
            String currentDbVal = null;
            String fileName = null;
            int eventId = -1;
            String fileColumns = "";

            final QsFileContent tableDetails = new QsFileContent();
            if (requestObj instanceof DataFrameRequest) {
                wsRequest = (DataFrameRequest) requestObj;
                eventId = wsRequest.getEventId();
                fileType = wsRequest.getFileType();
                dfName = "df" + eventId;
                dfAlias = String.format("df_file_%s_%s", ((RAW.equals(wsRequest.getFileType()) ? RAW : PROCESSED)), wsRequest.getFileId());

                fileColumns = getFileColumnsList((wsRequest.getMetadata() != null) ? wsRequest.getMetadata().getMetadata() : null);
                tableDetails.setProject(project);
                QsFolders folder = folderService.getFolder(wsRequest.getFolderId());
                tableDetails.setFolderName(folder.getFolderName());
                tableDetails.setFolderId(folder.getFolderId());
                Optional<QsFiles> fileById = fileService.getFileById(wsRequest.getFileId());
                if (fileById.isPresent()) {
                    QsFiles qsFiles = fileById.get();
                    tableDetails.setFileName(qsFiles.getFileName());

                    fileName = qsFiles.getFileName();
                    tableDetails.setFileId(qsFiles.getFileId());
                } else {
                    log.error("Unable to find record for {}", wsRequest.getFileId());
                }
            } else if (requestObj instanceof EngFileOperationRequest) {
                engFlowOpRequest = (EngFileOperationRequest) requestObj;
                eventId = engFlowOpRequest.getEventId();
                dfName = "df" + eventId;
                dfAlias = String.format("df_file_%s_%s", ENG_OP, engFlowOpRequest.getFileId());

                fileType = engFlowOpRequest.getFileType();
            }

            switch (fileType) {
                case PROCESSED: {
                    //ToDo: Need to revisit this logic based on the testing outcome.
                    List<FileMetaDataAwsRef> distinctFileDesc =
                            fileMetaDataAwsService.getDistinctFileDesc(wsRequest.getFileId());
                    log.warn("Processed Files found {}", distinctFileDesc.size());
                    tableName = distinctFileDesc.get(0).getAthenaTable();
                    partition = distinctFileDesc.get(0).getTablePartition();
                    currentDbVal = project.getProcessedDb();

                    // Set the metadata of the Processed file in case user drags and drops the
                    String fileMetadata = getFileMetadata(distinctFileDesc.get(0).getFileMetaData(), eventId);
                    FileJoinColumnMetadata fileColMetadata =
                            gson.fromJson(fileMetadata, FileJoinColumnMetadata.class);
                    wsRequest.setMetadata(fileColMetadata);

                    fileColumns = getFileColumnsList(fileColMetadata.getMetadata());

                    break;
                }
                case ENG_OP: {
                    log.info("EngFlowOperationRequest instance is: {}", engFlowOpRequest);
                    EngFlowMetaDataAwsRef engFlowMetaDataAwsRef = metadataHelper.getLatestCompletedEngFlowsInfo(engFlowOpRequest.getEngFlowName());
                    tableName = engFlowOpRequest.getEngFlowName(); // use the Athena Table name which is the Eng Flow Name....
                    partition = engFlowMetaDataAwsRef.getTablePartition();
                    currentDbVal = project.getEngDb();
                    String fileMetadata = getFileMetadata(engFlowMetaDataAwsRef.getEngFlowMetaData(), eventId);
                    FileJoinColumnMetadata fileColMetadata =
                            gson.fromJson(fileMetadata, FileJoinColumnMetadata.class);
                    engFlowOpRequest.setMetadata(fileColMetadata);

                    fileColumns = getFileColumnsList(fileColMetadata.getMetadata());

                    break;
                }
                default: {
                    QsFileContent decideTableName = qsUtil.decideTableName(tableDetails, false);
                    tableName = decideTableName.getTableName();
                    partition = decideTableName.getPartition();
                    currentDbVal = project.getRawDb();
                    log.info("Table Name: {} and Partition Name:{}", tableName, partition);

                    break;
                }
            }

            List<String> commandsSequence = null;
            if (fileContents != null) {
                commandsSequence = new ArrayList<>();
            }

            if (requestObj instanceof DataFrameRequest) {
                loadDataToFrame(
                        currentTable,
                        filterCondition,
                        currentDbVal + "." + tableName,
                        partition,
                        dfName,
                        wsRequest.getMetadata(),
                        wsRequest.getEventId(), commandsSequence, ((DataFrameRequest) requestObj).getFileType());
            } else {
                loadDataToFrame(
                        currentTable,
                        filterCondition,
                        currentDbVal + "." + tableName,
                        partition,
                        dfName,
                        engFlowOpRequest.getMetadata(),
                        engFlowOpRequest.getEventId(), commandsSequence, ((EngFileOperationRequest) requestObj).getFileType());
            }

            log.info("Current Database Pyspark query is: {}", currentDb);
            log.info("Current Table Pyspark query is: {}", currentTable);

            // Get the column metadata for further processing...
            final JsonObject finalResult = pySparkCommands(fileColumns, currentDb, currentTable, filterCondition, project, dfName, dfAlias, eventId, commandsSequence);

            // Execute only if it is a Drag and Drop action...
            if (commandsSequence == null && !fileDataFlag) {
                response = showResponse(finalResult, project, eventId);
                readTree = getJsonNode(response);
            }
            log.info("UDF commandsSequence ==>" + commandsSequence.toString());
            // Below logic is specific to Engineering Auto-run functionality..
            if (fileContents != null) {
                commandsSequence.stream().forEach((command) -> {
                    fileContents.append(command).append("\n");
                });

                fileContents.append("\n");
                // Add the REST call request with data to be updated...
                updateAutoRunBatchScript(fileContents, project.getProjectId(), eventId, dfName);
                log.info("livy file content upadated : " + fileContents.toString());
            }

        } catch (final Exception e) {
            log.error("Exception while initializing the context with livy {}", e.getMessage());
        }

        return readTree;
    }

    public JsonNode invokeUdfOperation(final UdfOperationRequest udfReq, final boolean writeToCloud, final int flowJobId,
                                       StringBuilder fileContents, boolean joinDataFlag, String udfFnDef) throws Exception {
        JsonElement elementTemp;
        JsonNode readTree = null;
        File tmpUdfFile = null;
        BufferedWriter bwUdf = null;
        S3Object s3Object = null;
        try {
            changeSchema("public");
            final Projects project = projectService.getProject(udfReq.getProjectId());
            log.info(" Project - {}, {} ,{}", udfReq.getProjectId(), project.getRawDb(), project.getRawCrawler());
            Map<String, Integer> events = udfReq.getEventIds();
            int udfEventId = events.get("eventId");
            QsUdf qsUdf = udfService.getByUdfIdAndProjectIdAndUserId(udfReq.getFileId(), udfReq.getProjectId(), project.getUserId());
            String dfNameLocal = String.format("df%d", udfEventId);
            if (qsUdf != null) {

                JsonObject consolidatedCode = new JsonObject();
                StringBuilder sbUdfObj = new StringBuilder();

                List<String> commandsSequence = null;
                if (fileContents != null) {
                    fileContents.insert(0, "from " + qsUdf.getUdfName() + " import * \n");
                    sbUdfObj.append("\n");
                    //String udfsytax = qsUdf.getUdfSyntax();
                    //udfsytax = udfsytax.replace(udfsytax.substring(udfsytax.indexOf("(")+1, udfsytax.indexOf(")")), String.format("df%d",udfReq.getEventId1())+"," +String.format("df%d", udfReq.getEventId2()));

                    String udfFunctionDef = buildUdfFunction(udfReq, udfFnDef);
                    log.info("udfFunctionDef ===> " + udfFunctionDef);
                    sbUdfObj.append(String.format("df%d = ", udfEventId)).append(udfFunctionDef);
                    // Control comes here for Auto-run flow only...

                    commandsSequence = new ArrayList<>();
                    commandsSequence.add(sbUdfObj.toString());
                    commandsSequence.add("\n");
                    commandsSequence.add(dfNameLocal + "_json_resp = re.sub(\"(a_[0-9]*_)\", \"\", json.dumps([json.loads(x) for x in " + dfNameLocal + ".toJSON().collect()], sort_keys=False, indent=4))");

                    commandsSequence.stream().forEach((command) -> {
                        fileContents.append(command).append("\n");
                    });

                    fileContents.append("\n");
                    updateAutoRunBatchScript(fileContents, project.getProjectId(), udfEventId, dfNameLocal);
                    log.info("inside if udf content :" + fileContents.toString());
                } else {
                    log.info("else part s3 udf");
                    sbUdfObj.append("import logging").append("\n");
                    //Read the file from s3
                    s3Object = awsAdapter.fetchObject(udfBucketName, udfReq.getProjectId() + "/" + new File(qsUdf.getUdfFilepath()).getName());
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
                        char[] theChars = new char[128];
                        int charsRead = br.read(theChars, 0, theChars.length);
                        while (charsRead != -1) {
                            sbUdfObj.append(new String(theChars, 0, charsRead));
                            charsRead = br.read(theChars, 0, theChars.length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    consolidatedCode.addProperty("code", sbUdfObj.toString());
                    log.info("Call the UDF function - {}", sbUdfObj.toString());
                    livySessionHandler(project, udfEventId, getRandomDoubleValueInRange(30, 70), consolidatedCode.toString());

                    StringBuilder sbUdf = new StringBuilder();
                    sbUdf.append("import json").append("\n")
                            .append(String.format("df%d = ", udfEventId)).append(udfReq.getUdfFunction()).append("\n").
                            append(String.format("print(%s.toPandas().to_json(orient='records'))", String.format("df%d", udfEventId)));

                    JsonObject finalResult = new JsonObject();
                    finalResult.addProperty("code", sbUdf.toString());
                    log.info("Print the UDF function - {}", sbUdf.toString());

                    elementTemp = showResponse(finalResult, project, udfEventId);
                    readTree = getJsonNode(elementTemp);
                    log.info("Print the UDF result - {}", readTree);
                    return readTree;
                }
            }

        } catch (final Exception e) {
            log.error("Exception while initializing the context with livy {}", e.getMessage());
        }
        return null;
    }

    public JsonNode invokeJoinOperation(
            final JoinOperationRequest joinReq, final boolean writeToCloud, final int flowJobId,
            StringBuilder fileContents, boolean joinDataFlag)
            throws Exception {
        JsonElement elementTemp;
        JsonNode readTree = null;

        final JsonObject importJson = new JsonObject();
        final JsonObject currentJoin = new JsonObject();
        //final JsonObject currentJson = new JsonObject();
        final JsonObject finalResult = new JsonObject();
        final StringBuilder joinQuery = new StringBuilder();
        //final JsonObject currentSqlQuery = new JsonObject();
        String dfNameLocal = String.format("df%d", joinReq.getEventId());
        changeSchema("public");
        final Projects project = projectService.getProject(joinReq.getProjectId());
        log.info(
                " Project - {}, {} ,{}",
                joinReq.getProjectId(),
                project.getRawDb(),
                project.getRawCrawler());

        int joinEventId = joinReq.getEventId();
        prepareJoinQuery(joinQuery, joinReq);
        pySparkJoinQuery(currentJoin, joinQuery);
        pySparkImportJson(importJson);

        List<String> commandsSequence = null;
        if (fileContents != null) {
            // Control comes here for Auto-run flow only...
            commandsSequence = new ArrayList<>();
            commandsSequence.add(joinQuery.append(".na.fill('null')").toString());
            commandsSequence.add(dfNameLocal + "_json_resp = re.sub(\"(a_[0-9]*_)\", \"\", json.dumps([json.loads(x) for x in " + dfNameLocal + ".toJSON().collect()], sort_keys=False, indent=4))");

            //pySparkToJson(currentJson, dfNameLocal, commandsSequence);
            //pySparkLoadJson(currentSqlQuery, dfNameLocal, commandsSequence);
            //pySparkSortJsonDump(finalResult, dfNameLocal, commandsSequence);

            commandsSequence.stream().forEach((command) -> {
                fileContents.append(command).append("\n");
            });

            fileContents.append("\n");
            updateAutoRunBatchScript(fileContents, project.getProjectId(), joinEventId, dfNameLocal);
        } else {
            String importJsonStr = "import json";
            //String jsonLimitStr = dfNameLocal + "_limit = " + dfNameLocal + ".na.fill('null')";
            String currJsonStr = dfNameLocal + " = " + dfNameLocal + ".na.fill('null')";

            //String livyResponse = peekDataframeInSession(project, dfNameLocal);
            //if (isErrorStatus(livyResponse) || !joinDataFlag) {
            // Control comes here for Drag & Drop events..
            StringBuilder sb = new StringBuilder();
            sb.append(importJsonStr).append("\n").
                    append(joinQuery).append("\n").
                    //append(jsonLimitStr).append("\n").
                            append(currJsonStr).append("\n");

            JsonObject consolidatedCode = new JsonObject();
            consolidatedCode.addProperty("code", sb.toString());

            livySessionHandler(project, joinEventId, getRandomDoubleValueInRange(30, 70),
                    consolidatedCode.toString());


            //}else {
            //  log.info(dfNameLocal + " dataframe already exists:");
            //}

            if (!joinDataFlag) {
                String finalResStr = String.format("print(%s.toPandas().to_json(orient='records'))", dfNameLocal);
                finalResult.addProperty("code", finalResStr);
                elementTemp = showResponse(finalResult, project, joinEventId);
                readTree = getJsonNode(elementTemp);
                if (writeToCloud) {
                    writeToCloudStore(
                            project,
                            dfNameLocal,
                            joinReq.getEventId1() + "_" + joinReq.getEventId2() + ".csv",
                            flowJobId,
                            joinReq.getEventId());
                }
            }

            return readTree;
        }

        return null;
    }

    private String livySessionHandler(final Projects project, final int eventId, final double progress, final String currentCode)
            throws Exception {
        dbUtil.changeSchema(project.getDbSchemaName());
        int livySessionId = 0;
        final String sessionName = "project0" + project.getProjectId();
        log.info("Global SessionID {} ", livySessionId);
        livySessionId = livyClient.getApacheLivySessionId(sessionName);
        livySessionId = livyClient.getOrCreateApacheLivySessionId(sessionName);
        livyClient.getApacheLivySessionState(livySessionId);
        log.info("Global SessionID {} and SessionName {}", livySessionId, sessionName);
        final int livyStatementId = livyClient.runStatement(livySessionId, currentCode, eventId, progress);

        dbUtil.changeSchema(project.getDbSchemaName());
        return livyClient.getStatementResults(livySessionId, livyStatementId, eventId);
    }

    public String invokeImportJsonStmt(final int userId, final String dbSchemaName)
            throws Exception {
        int livySessionId = 0;
        JsonObject initLivyJson = new JsonObject();
        pySparkInitializeLivy(initLivyJson);
        final String initLivyJsonStr = initLivyJson.toString();
        final String sessionName = "project0" + userId;
        log.info("Global SessionID {} ", livySessionId);
        livySessionId = livyClient.getOrCreateApacheLivySessionId(sessionName);
        livyClient.getApacheLivySessionState(livySessionId);
        log.info("Global SessionID {} and SessionName {}", livySessionId, sessionName);
        final int livyStatementId = livyClient.runStatement(livySessionId, initLivyJsonStr);

        dbUtil.changeSchema(dbSchemaName);
        return livyClient.getStatementResults(livySessionId, livyStatementId, -1);
    }

    private void loadDataToFrame(
            final JsonObject currentTable,
            final JsonObject filterCondition,
            final String tableName,
            final String partitionName,
            final String dfName,
            final FileJoinColumnMetadata metadata,
            final int eventId, List<String> commandsSequence, final String fileType) {
        log.info("Loading data to frame using the info: {}, {}, {}, {} and {}", tableName, partitionName, dfName, metadata, eventId);
        String query;
        String columnsStr = getColumnsStringWithEventAlias(metadata, eventId);
        String fileCols = getFileColumnsList(metadata.getMetadata());
        String cmd2 = String.format("newColumns=%s", fileCols);
        String cmd2a = String.format("%s=%s.toDF(*newColumns)", dfName, dfName);

        StringBuilder sb = new StringBuilder();
        sb.append(cmd2).append("\n").append(cmd2a);

        String filterCondStr = getFilterCondition(metadata, eventId, dfName, sb.toString());

        if (partitionName != null) {
            query = selectPartition(tableName, partitionName, dfName, columnsStr, fileType, (commandsSequence == null));
        } else {
            query = selectTable(tableName, dfName, columnsStr, fileType, (commandsSequence == null));
        }
        currentTable.addProperty("code", query);
        filterCondition.addProperty("code", filterCondStr);

        // Used in the Engineering Auto-run flow.
        if (commandsSequence != null) {

            commandsSequence.add(query);
            commandsSequence.add(filterCondStr);
        }
    }

    private void prepareJoinQuery(final StringBuilder joinQuery, final JoinOperationRequest joinReq) {
        final String col1Name = joinReq.getFirstFileColumn().toLowerCase();
        final String col2Name = joinReq.getSecondFileColumn().toLowerCase();
        final int eventId1 = joinReq.getEventId1();
        final int eventId2 = joinReq.getEventId2();

        final String dfName1 = String.format("df%d", eventId1);
        final String dfName2 = String.format("df%d", eventId2);
        final String joinName = String.format("df%d = ", joinReq.getEventId());
        joinQuery.append(joinName);

        joinQuery.append(dfName1);
        joinQuery.append(".join(");
        joinQuery.append(dfName2);
        joinQuery.append(",");
        joinQuery.append(dfName1);
        joinQuery.append(".");
        joinQuery.append(col1Name);
        joinQuery.append("==");
        joinQuery.append(dfName2);
        joinQuery.append(".");
        joinQuery.append(col2Name);
        joinQuery.append(",");
        joinQuery.append("how='");
        joinQuery.append(joinReq.getJoinType());
        joinQuery.append("').select(");
        joinQuery.append(dfName1);
        joinQuery.append("[\"*\"], ");
        joinQuery.append(dfName2);
        joinQuery.append("[\"*\"])");

        log.info("Join Query prepared in PySpark format is: {}" + joinQuery.toString());
    }

    private JsonObject pySparkCommands(
            final String fileCols,
            final JsonObject currentDb,
            final JsonObject currentTable,
            final JsonObject filterCondition,
            final Projects project,
            final String dfName, final String dfAlias, final int eventId, List<String> commandsSequence)
            throws Exception {
        final JsonObject importJson = new JsonObject();
        final JsonObject currentJson = new JsonObject();
        final JsonObject finalResult = new JsonObject();
        final JsonObject jsonLimit = new JsonObject();
        final JsonObject currentSqlShow = new JsonObject();
        final JsonObject dfAliasCmd = new JsonObject();
        final JsonObject dfAliasResultCmd = new JsonObject();
        fillSparkQuery(importJson, currentJson, finalResult, jsonLimit, currentSqlShow, dfAliasCmd, dfAliasResultCmd, dfName, dfAlias, commandsSequence);

        // commandsSequence instance is null only when user performs drag & drop action on the Engineering canvas on the UI.
        // In case of Engineering Auto-Run, these events are executed in a Apache Livy Batch instead of statement execution.
        // Hence gating the below commands execution.

        if (commandsSequence == null) {
            //String checkDfName = peekDataframeInSession(project, dfName);
            //if (isErrorStatus(checkDfName)) {
            // Before creating the new dataframe, just check if we can re-use the existing alias Dataframe...
            // If the Alias Dataframe is also not present then, create a new Dataframe altogether..

            //String checkDfAlias = peekDataframeInSession(project, dfAlias);
            //if (isErrorStatus(checkDfAlias)) {
            log.info("Creating new dataframe " + dfName);
            livySessionHandler(project, eventId, getRandomDoubleValueInRange(11, 45),
                    currentTable.toString());
            livySessionHandler(project, eventId, getRandomDoubleValueInRange(46, 50),
                    filterCondition.toString());
            //livySessionHandler(project, eventId, getRandomDoubleValueInRange(51, 60),
            //  dfAliasCmd.toString());
            //livySessionHandler(project, eventId, getRandomDoubleValueInRange(60, 70),
            //dfAliasResultCmd.toString());
            //}
        /*else {
          log.info("Creating new dataframe '{}' using the existing dataframe alias:'{}'", dfName, dfAlias);
          JsonObject useAliasDfCmd = new JsonObject();
          String cmd1 = String.format("%s=%s", dfName, dfAlias);
          useAliasDfCmd.addProperty("code", cmd1);

          livySessionHandler(project, eventId, getRandomDoubleValueInRange(45, 70),
              useAliasDfCmd.toString());

          // Rename the columns in the new dataframe to use the same event id..
          JsonObject newColsCmd = new JsonObject();
          String cmd2 = String.format("newColumns=%s", fileCols);
          String cmd2a = String.format("%s=%s.toDF(*newColumns)", dfName, dfName);
          StringBuilder sb = new StringBuilder();
          sb.append(cmd2).append("\n").append(cmd2a);

          newColsCmd.addProperty("code", sb.toString());
          livySessionHandler(project, eventId, getRandomDoubleValueInRange(81, 90),
              newColsCmd.toString());

          finalResult.remove("code");
          String cmd3 = String.format("print(%s.toPandas().to_json(orient='records'))", dfName);
          finalResult.addProperty("code", cmd3);
        }*/
      /*}
      else {
        log.info(dfName + " dataframe already exists:");
      }*/
        }
        return finalResult;
    }

    private String getStatus(String livyresponse) {
        ObjectMapper mapper = new ObjectMapper();
        String status = "error";
        try {
            JsonNode node = mapper.readValue(livyresponse, JsonNode.class);
            status = node.get("output").get("status").asText();
        } catch (JsonProcessingException e) {
            log.error("error while parsing livy response. signal to create new dataframes");
        }
        return status;
    }

    private String getOutputData(String livyresponse) {
        ObjectMapper mapper = new ObjectMapper();
        String data = "";
        try {
            JsonNode node = mapper.readValue(livyresponse, JsonNode.class);
            data = node.get("output").get("data").get("text/plain").asText();
        } catch (JsonProcessingException e) {
            log.error("error while parsing livy response. signal to create new dataframes");
        }
        return data;
    }

    /**
     * @param currentDb
     * @param currentTable
     * @param importJson
     * @param currentJson
     * @param jsonLimit
     * @param currentSqlShow
     * @return
     */
    @SuppressWarnings("unused")
    private JsonObject preparePayload(String dataframeVariable, final JsonObject currentDb, final JsonObject currentTable,
                                      final JsonObject importJson, final JsonObject currentJson, final JsonObject jsonLimit,
                                      final JsonObject currentSqlShow) {
        String code = "code";
        String newline = "\n";
        String tryExcept = "try: " + dataframeVariable + "\n"
                + "except NameError: " + dataframeVariable + "= None\n";
        StringBuilder builder = new StringBuilder(tryExcept);
        builder.append(String.format("if %s is None:\n", dataframeVariable));
        builder.append(currentDb.get(code).getAsString());
        builder.append(newline).append(currentTable.get(code).getAsString()).append(newline)
                .append(importJson.get(code).getAsString()).append(newline)
                .append(jsonLimit.get(code).getAsString()).append(newline)
                .append(currentJson.get(code).getAsString()).append(newline)
                .append(currentSqlShow.get(code).getAsString()).append(newline);

        builder.append(String.format("else:\n%sprint(%s is available in the current session)\n", dataframeVariable));
        final JsonObject payload = new JsonObject();
        payload.addProperty(code, builder.toString());
        log.info("livy payload : " + payload.get(code).getAsString());
        return payload;
    }

    public JsonElement runAggregateQuery(String aggregateQuery, Projects projects, String dfName, int aggEventId,
                                         StringBuilder fileContents, boolean aggEventDataFlag) throws Exception {

        JsonObject importPsf = new JsonObject();
        JsonObject jsonQueryObject = new JsonObject();
        JsonObject collectToJson = new JsonObject();
        JsonObject loadJson = new JsonObject();
        JsonObject resultAsJson = new JsonObject();

        jsonQueryObject.addProperty("code", aggregateQuery);
        String importCmd = "import pyspark.sql.functions as f";
        //String collectCmd = dfName + "_results = " + dfName + ".toJSON().collect()";
        String collectCmdDr = "print(" + dfName + ".toPandas().to_json(orient='records'))";
        String resultsCmd = dfName + "_result = [json.loads(x) for x in " + dfName + "_results]";
        //String dumpsCmd = dfName+"_json_resp=json.dumps(" + dfName + "_result, sort_keys=False, indent=4)";
        String removeEventPrefix = dfName + "_json_resp = re.sub(\"(a_[0-9]*_)\", \"\", json.dumps([json.loads(x) for x in " + dfName + ".toJSON().collect()], sort_keys=False, indent=4))";

        importPsf.addProperty("code", "import pyspark.sql.functions as f");
        collectToJson.addProperty("code", collectCmdDr);
        loadJson.addProperty("code", resultsCmd);
        resultAsJson.addProperty("code", collectCmdDr);

        List<String> commandsSequence = null;
        if (fileContents != null) {
            commandsSequence = new ArrayList<>();
            commandsSequence.add(aggregateQuery);
            //commandsSequence.add(collectCmd);
            //commandsSequence.add(resultsCmd);
            //commandsSequence.add(dumpsCmd);
            commandsSequence.add(removeEventPrefix);

            commandsSequence.stream().forEach((command) -> {
                fileContents.append(command).append("\n");
            });

            fileContents.append("\n");
            updateAutoRunBatchScript(fileContents, projects.getProjectId(), aggEventId, dfName);
        } else {
            // Online flow
            //String livyResponse = peekDataframeInSession(projects, dfName);
            //if (isErrorStatus(livyResponse) || !aggEventDataFlag) {
            StringBuilder sb = new StringBuilder();
            sb.append(importCmd).append("\n").
                    append(aggregateQuery).append("\n");

            JsonObject consolidatedCode = new JsonObject();
            consolidatedCode.addProperty("code", sb.toString());

            livySessionHandler(projects, aggEventId, getRandomDoubleValueInRange(30, 70),
                    consolidatedCode.toString());
            //} else {
            //log.info(dfName + " dataframe already exists:");
            //}

            JsonElement elementTemp = null;
            if (!aggEventDataFlag) {
                elementTemp = showResponse(resultAsJson, projects, aggEventId);
            }

            log.info("RunAggregate - {}", elementTemp);

            return elementTemp;
        }

        return null;
    }

    private boolean isErrorStatus(String livyResponse) {
        return "error".equals(getStatus(livyResponse)) || "None".equals(getOutputData(livyResponse));
    }

    private String peekDataframeInSession(Projects projects, String dataFrame) {
        String response = null;
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("code", String.format("print(%s)", dataFrame));
            log.info("Inside peekDataframeInSession() method and the Payload is: {}", payload.toString());
            response = livySessionHandler(projects, 0, 0, payload.toString());
        } catch (Exception e) {
            log.error("Exception occured while checking the Livy Session for Dataframe: ", e.getCause());
        }
        return response;
    }

    private String selectPartition(
            final String tableName,
            final String partitionName,
            final String dfName,
            final String columnsStr, final String fileType, final boolean isAutoRun) {

        return String.format(
                "%s = spark.sql('SELECT %s FROM %s where partition_0 = \"%s\" %s')",
                dfName, columnsStr, tableName, partitionName, ((isAutoRun) ? " limit 200" : ""));
    }

    private String selectTable(
            final String tableName,
            final String dfName,
            final String columnsStr, final String fileType, final boolean isAutoRun) {

        return String.format(
                "%s = spark.sql('SELECT %s FROM %s %s')",
                dfName, columnsStr, tableName, ((isAutoRun) ? " limit 200" : ""));
    }

    private JsonElement showResponse(final JsonObject currentSqlShow, final Projects project, final int eventId)
            throws Exception {
        final Gson gson = new Gson();
        JsonElement elementTemp;
        final String livySessionHandler = livySessionHandler(project, eventId, getRandomDoubleValueInRange(71, 95), currentSqlShow.toString());
        elementTemp = gson.fromJson(livySessionHandler, JsonElement.class);
        elementTemp = elementTemp.getAsJsonObject().get("output");
        JsonObject objJSON = elementTemp.getAsJsonObject();
        if (!StringUtils.strip(objJSON.get("status").toString(), "\"").equals("error")) {
            elementTemp = elementTemp.getAsJsonObject().get("data");
            elementTemp = elementTemp.getAsJsonObject().get("text/plain");
        }
        //log.info("Output {}", elementTemp.getAsString());
        return elementTemp;
    }

    private void writeToCloudStore(
            final Projects project,
            final String dfName,
            final String fileName,
            int flowJobId,
            int eventId)
            throws Exception {
        final JsonObject writeToCsv = new JsonObject();
        final String s3Path = String.format("%s/%s/%s/%s", resultStore, flowJobId, eventId, fileName);
        log.info("Writing to cloud store @ {}", s3Path);
        log.info("Final Join output query dfName {}", dfName);
        writeToCsv.addProperty(
                "code", String.format("%s.coalesce(1).write.csv('%s',header=True)", dfName, s3Path));
        livySessionHandler(project, -1, 0.0, writeToCsv.toString());
    }

    public List<String> writeToEngResultBucket(final Projects project, final String dfName, int jobId, String parentEngFlowName, String engFlowName)
            throws Exception {
        String targetBucket = project.getBucketName() + "-" + QsConstants.ENG;
        log.info("Final output For engineering screen dfName {}", dfName);

        // Get the S3 Object again and rename the column headers and then push the object again...
        Map<String, List<String>> finalEventData = awsAdapter.getFinalEventData(targetBucket, parentEngFlowName + "/" + engFlowName + "/");
        List<String> finalEventDataLines = null;
        String objKey = "";
        if (finalEventData != null && !finalEventData.isEmpty()) {
            for (Map.Entry<String, List<String>> me : finalEventData.entrySet()) {
                objKey = me.getKey();
                finalEventDataLines = me.getValue();
                String headerLine = finalEventDataLines.get(0);
                List<String> modifiedHeaders = awsAdapter.getModifiedColHeaders(headerLine);
                String modifiedHeaderLine = modifiedHeaders.toString();
                modifiedHeaderLine = modifiedHeaderLine.substring(1, modifiedHeaderLine.length() - 1); // removing the leading and trailing []
                modifiedHeaderLine = modifiedHeaderLine.replaceAll(", ", ",");

                log.info("---> Updated header line after removing the a_<EVENT_ID>_ prefix: {}", modifiedHeaderLine);

                // Removing the unnecessary extra data line with column headers...
                String checkHeader = awsAdapter.getCheckHeader(modifiedHeaderLine);
                finalEventDataLines.removeIf(key -> key.toLowerCase().startsWith(checkHeader.toLowerCase()));

                // Finally add the header again
                finalEventDataLines.set(0, modifiedHeaderLine);
                String fileName = objKey;
                log.info("File name is: {}", fileName);

                String tmpdir = System.getProperty("java.io.tmpdir");
                File csvFile = new File(tmpdir + File.separator + fileName);
                if (!csvFile.getParentFile().exists()) {
                    csvFile.getParentFile().mkdirs();
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
                    for (String line : finalEventDataLines) {
                        bw.write(line);
                        bw.newLine();
                    }
                } catch (IOException ie) {
                    ie.printStackTrace();
                }

                //awsAdapter.setBucketName(targetBucket);
                awsAdapter.storeObjectInS3Async(targetBucket, csvFile, engFlowName, project.getEngDb(), parentEngFlowName, fileName, "text/csv;charset=utf-8;");
            }
        }

        return finalEventDataLines;
    }

    private String getColumnsStringWithEventAlias(
            final FileJoinColumnMetadata metadata, final int eventId) {
        StringBuilder sb = new StringBuilder();

        if (metadata != null) {
            for (Metadata value : metadata.getMetadata()) {
                String columnName = value.getValue();
                sb.append(columnName.toLowerCase());
                sb.append(",");
            }
            sb.deleteCharAt(sb.toString().length() - 1);
        }

        return sb.toString();
    }

    private String getFilterCondition(final FileJoinColumnMetadata metadata, final int eventId, final String dfName, final String colRenameCmd) {
        StringBuilder sb = new StringBuilder();

        if (metadata != null) {
            String firstColumn = "";
            List<Metadata> colMetadatas = metadata.getMetadata();
            if (colMetadatas != null && !colMetadatas.isEmpty()) {
                sb.append(dfName).append("=").append(dfName).append(".filter(\"");
                firstColumn = colMetadatas.get(0).getKey();
                firstColumn = firstColumn.replaceAll("a_[0-9]*_", "");
                int colCount = colMetadatas.size();
                int index = 1;
                for (Metadata md : colMetadatas) {
                    String key = md.getKey();
                    key = key.replaceAll("a_[0-9]*_", "").toLowerCase();
                    sb.append("(").append(key).append(" != ").
                            append("'").append(md.getValue()).append("'").append(")");

                    if (index < colCount) {
                        sb.append(" and ");
                    }

                    ++index;
                }
                sb.append("\").orderBy(\"").append(firstColumn.toLowerCase()).append("\")");
                sb.append("\n");
                sb.append(colRenameCmd);
            }
        }

        return sb.toString();
    }

    private double getRandomDoubleValueInRange(int start, int end) {
        if (end > 100) end = 100;

        String dv = String.format("%.2f", (start + (end - start) * random.nextDouble()));
        return Double.parseDouble(dv);
    }

    public int invokeCleanseJobOperation(final Projects project, final int runJobId, final QsFileContent tableAwsName, final String pysparkScriptS3FileLoc)
            throws Exception {
        log.info("Invoked the Livy Job for running this Cleanse Job...");

        Instant start = Instant.now();

        //String payload1 = "{\"file\":\""+pysparkScriptS3FileLoc+"\"}";
        final JsonObject payload = new JsonObject();
        payload.addProperty("file", pysparkScriptS3FileLoc);
        payload.addProperty("executorMemory", executorMemory);
        payload.addProperty("driverMemory", driverMemory);
        String jsonRes = livyClient.livyPostHandler(livyBaseBatchesUrl, payload.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        final JsonNode fromJson = mapper.readTree(jsonRes);
        final int batchJobId = fromJson.get("id").asInt();
        log.info(" Batch Job ID {}", batchJobId);
        RunJobStatus runJobStatus = updateJobEntry(runJobId, JobRunState.RUNNING.toString(), batchJobId, null, -1);

        final JsonNode batchResponse = livyClient.getApacheLivyBatchState(batchJobId, mapper);
        if (batchResponse != null) {
            String batchJobState = batchResponse.get("state").asText();
            Instant end = Instant.now();
            long elapsedTime = Duration.between(start, end).toMillis();

            List<String> logMsgs = livyClient.getApacheLivyBatchJobLog(batchJobId, mapper);
            String batchJobLog = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logMsgs);

            if ("success".equals(batchJobState)) {
                RunJobStatus runJob = updateJobEntry(runJobId, JobRunState.SUCCEEDED.toString(), batchJobId, batchJobLog, elapsedTime);

                // Update File Metadata AWSRef Table now..
                FileMetaDataAwsRef fileMetaDataAwsRef = awsAdapter.createEntryAwsRef(runJob, tableAwsName);

                log.info("Inserted a record in File Metadata AWSRef Table..");
                // Athena process starts here..
                String bucketName = project.getBucketName() + "-" + PROCESSED;
                awsAdapter.handleAthenaProcessForCleanseJob(bucketName, tableAwsName.getFolderName(), runJobId + "", project.getProcessedDb(), fileMetaDataAwsRef);
                log.info("Completed running the cleanse Job...");
            } else {
                updateJobEntry(runJobId, JobRunState.FAILED.toString(), batchJobId, batchJobLog, elapsedTime);
            }
        } else {
            updateJobEntry(runJobId, JobRunState.FAILED.toString(), batchJobId, "Batch job aborted, as the job execution time exceeded the threshold of 5mins. Couldn't capture the batch job log.", -1);
        }

        return batchJobId;
    }

    private RunJobStatus updateJobEntry(final int runJobId, final String status, final int batchJobId, final String batchJobLog,
                                        final long elapsedTime) throws SQLException {
        Optional<RunJobStatus> runJobOp = runJobService.getRunJob(runJobId);
        RunJobStatus runJob = null;
        log.info("Batch Job Id during update---> {}", batchJobId);

        if (runJobOp.isPresent()) {
            runJob = runJobOp.get();
            runJob.setStatus(status);
            runJob.setBatchJobId(batchJobId);
            runJob.setBatchJobLog(batchJobLog);
            runJob.setBatchJobDuration(elapsedTime);

            RunJobStatus runJobStatus = runJobService.saveRunJob(runJob);
            return runJobStatus;
        }

        return null;
    }

    private void updateAutoRunBatchScript(StringBuilder fileContents, int projectId, int eventId, String dfName) {
        String eventData = dfName + "_json_resp";

        StringBuilder sb = new StringBuilder();
        sb.append("{\"projectId\":");
        sb.append(projectId);
        sb.append(", \"eventId1\":");
        sb.append(eventId);
        sb.append(", \"eventData\": ");
        sb.append(eventData);
        sb.append("}");

        String data = sb.toString();

        String dataKey = "data" + eventId;
        String responseKey = "response" + eventId;

        fileContents.append(dataKey).append("=").append(data);
        fileContents.append("\n");
        fileContents.append(responseKey).append(" = requests.post(API_ENDPOINT, data=json.dumps("+dataKey+"), headers = headers1)");
        fileContents.append("\n");
        fileContents.append("print(").append(responseKey).append(".status_code)");
        fileContents.append("\n\n");
    }

    public BatchJobInfo invokeAutorunJobOp(final Projects project, final String pysparkScriptS3FileLoc, final List<String> udfFilePath)
            throws Exception {
        log.info("Invoked the Livy Job for running this Cleanse Job...");

        //String payload1 = "{\"file\":\""+pysparkScriptS3FileLoc+"\"}";

        final JsonObject payload = new JsonObject();
        payload.addProperty("file", pysparkScriptS3FileLoc);
        if (udfFilePath != null) {
            final JsonArray udfFiles = new JsonArray();
            for (String udfFile : udfFilePath) {
                udfFiles.add(udfFile);
            }
            payload.add("pyFiles", udfFiles);
        }
        payload.addProperty("executorMemory", executorMemory);
        payload.addProperty("driverMemory", driverMemory);
        log.info("Livy payload : {}", payload.toString());
        String jsonRes = livyClient.livyPostHandler(livyBaseBatchesUrl, payload.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        final JsonNode fromJson = mapper.readTree(jsonRes);
        final int batchJobId = fromJson.get("id").asInt();
        log.info(" Batch Job ID {}", batchJobId);

        BatchJobInfo batchJobInfo = new BatchJobInfo();
        batchJobInfo.setBatchJobId(batchJobId);
        String batchJobLog = null;
        final JsonNode batchResponse = livyClient.getApacheLivyBatchState(batchJobId, mapper);
        String jobStatus = JobRunState.FAILED.toString();
        List<String> logMsgs = livyClient.getApacheLivyBatchJobLog(batchJobId, mapper);
        if (batchResponse != null) {
            String batchJobState = batchResponse.get("state").asText();

            if ("success".equals(batchJobState)) {
                jobStatus = JobRunState.SUCCEEDED.toString();
                log.info("Completed running the cleanse Job...");
            } else {
                jobStatus = JobRunState.FAILED.toString();
            }

            batchJobLog = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logMsgs);
        } else {
            jobStatus = JobRunState.FAILED.toString();
            batchJobLog = "Batch job aborted, as the job execution time exceeded the threshold of 5mins. Couldn't capture the batch job log.";
        }

        batchJobInfo.setBatchJobStatus(jobStatus);
        batchJobInfo.setBatchJobLog(batchJobLog);

        return batchJobInfo;
    }

    private String getFileMetadata(final String file1MetadataJson, final int eventId) throws Exception {
        List<ColumnMetaData> colsMetadata = parseColumnMetadataToList(file1MetadataJson);
        if (colsMetadata != null && !colsMetadata.isEmpty()) {
            FileJoinColumnMetadata fileJoinColumnMetadata = new FileJoinColumnMetadata();
            fileJoinColumnMetadata.setEventId(eventId);
            fileJoinColumnMetadata.setMetadata(getMetadataForFileorJoin(colsMetadata, eventId));

            log.info("File Metadata is: {}", fileJoinColumnMetadata);

            return gson.toJson(fileJoinColumnMetadata);
        }

        return "";
    }

    private List<Metadata> getMetadataForFileorJoin(final List<ColumnMetaData> colsMetadata, final int eventId) {
        if (colsMetadata != null && !colsMetadata.isEmpty()) {

            List<Metadata> fileOrJoinMetadata = colsMetadata.stream()
                    .map(obj -> (new Metadata("a_" + eventId + "_" + obj.getColumnName(), obj.getColumnName())))
                    .collect(Collectors.toList());
            log.info("File or Join Metadata returned is: {}", fileOrJoinMetadata);

            return fileOrJoinMetadata;
        }

        return Collections.emptyList();
    }

    private String getFileColumnsList(List<Metadata> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        metadata.stream().forEach((datum) -> {
            sb.append("\"");
            sb.append(datum.getKey().toLowerCase());
            sb.append("\"");
            sb.append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");

        return sb.toString();
    }

    public List<ColumnMetaData> parseColumnMetadataToList(final String fileMetadataStr) throws Exception {
        List<ColumnMetaData> colsMetadata = new ArrayList<>();
        if (fileMetadataStr != null && !fileMetadataStr.isEmpty()) {
            JSONArray jsonArray = new JSONArray(fileMetadataStr);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                ColumnMetaData column = new ColumnMetaData();
                column.setColumnName(obj.getString("column_name"));
                column.setDataType(obj.getString("data_type"));

                colsMetadata.add(column);
            }
        }

        return colsMetadata;
    }

    public boolean deleteLivySession(String sessionName) throws Exception {
        boolean status = false;
        int sessionId = livyClient.getApacheLivySessionIdByName(sessionName);
        if (sessionId != -1) {
            log.info("Livy Session with name: {} exists. Deleting it.", sessionName);

            status = livyClient.deleteLivySession(sessionId);
        } else {
            log.info("Livy Session with name: {} is not present. Skipping the delete action.", sessionName);
        }

        return status;
    }

    public String cleanupLivySessionMemory(Projects projects, String payloadStr) {
        String response = null;
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("code", payloadStr);
            log.info("Inside cleanupLivySessionMemory() method and the Payload is: {}", payload.toString());
            response = livySessionHandler(projects, 0, 0, payload.toString());
        } catch (Exception e) {
            log.error("Exception occured while cleaning up the Livy Session: ", e.getCause());
        }
        return response;
    }

    public String checkColDatatypeCasting(Projects projects, String payloadStr) {
        String finalResult = null;
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("code", payloadStr);
            log.info("Inside cleanupLivySessionMemory() method and the Payload is: {}", payload.toString());
            String response = livySessionHandler(projects, 0, 0, payload.toString());
            finalResult = getOutputData(response);
        } catch (Exception e) {
            log.error("Exception occured while cleaning up the Livy Session: ", e.getCause());
        }

        return finalResult;
    }

    public static void main(String[] args) {
        String df = "JoinDF(df562,df563,\"a_562_name\",\"a_563_name\",\"inner\")";
        String ids = df.substring(df.indexOf("(") + 1, df.indexOf(",\""));
        System.out.println(ids);
        String[] idList = ids.split(",");
        for (int i = 0; i < idList.length; i++) {
            String oldId = idList[i].replace("df", "").trim();
            df = df.replace(oldId, "570");
        }
        System.out.println(df);
    }

    public String buildUdfFunction(UdfOperationRequest udfOpReq, String udfFnDef) {
        log.info("UDF ==========================>" + udfOpReq);
        Map<String, Integer> eventIds = udfOpReq.getEventIds();
        log.info("UDF function before replace with new ids --->" + udfFnDef);
	  /*
	  List<UdfReqColumn> udfArguments = udfOpReq.getArguments();//gson.fromJson(arguments, new TypeToken<ArrayList<UdfReqColumn>>(){}.getType());
	  String methodParams  = udfFnDef.substring(udfFnDef.indexOf("(")+1, udfFnDef.indexOf(")"));
	  String [] parms = methodParams.split(",");
	  int item = 0, j = 1;

	  for(UdfReqColumn udfReq: udfArguments) {
		  if("Data frame".equals(udfReq.getDataValue())) {
			  parms[item] = String.format("df%d", eventIds.get("eventId"+(item+1)));
		  }else if("Argument".equals(udfReq.getDataValue())) {
			  String arg = parms[item];
			  if(arg.indexOf("_") != -1) {
				  parms[item] = arg.replace(arg.split("_")[1], String.valueOf(eventIds.get("eventId"+j)));
			  }
			  j++;
		  }
		  item++;
	  }*/

        // changes for UDF job fail issue fix
        String ids = udfFnDef.substring(udfFnDef.indexOf("(") + 1, udfFnDef.indexOf(",\""));
        System.out.println(ids);
        String[] idList = ids.split(",");
        for (int i = 0; i < idList.length; i++) {
            String oldId = idList[i].replace("df", "").trim();
            udfFnDef = udfFnDef.replace(oldId, eventIds.get("eventId" + (i + 1)).toString());
        }
        log.info("UDF function after replace with new ids -->" + udfFnDef);

        //return udfFnDef.replace(methodParams, Arrays.toString(parms).replace("[", "").replace("]", ""));
        return udfFnDef;
    }
}
