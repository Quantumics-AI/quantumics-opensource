/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.helper;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.livy.LivyActions;
import ai.quantumics.api.model.*;
import ai.quantumics.api.req.*;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.JobRunState;
import ai.quantumics.api.vo.BatchJobInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class EngHelper {
    private final AwsAdapter awsAdapter;
    private final ControllerHelper helper;
    private final LivyActions livyActions;
    private final EngFlowJobService engFlowJobService;
    private final EngFlowMetaDataService engFlowMetaData;
    private final EngFlowEventService engFlowEventService;
    private final EngFlowService engFlowService;
    private final DatabaseSessionManager sessionManager;
    private final RedashViewInfoService redashViewInfoService;

    @Value("${qs.eng.etl.output}")
    private String qsEtlScriptBucket;

  /*@Value("${qs.bitbucket.api.url}")
  private String bitBucketApiUrl;

  @Value("${qs.bitbucket.api.repo}")
  private String bitBucketRepository;

  @Value("${qs.bitbucket.api.key}")
  private String bitBucketApiKey;

  @Value("${qs.bitbucket.api.username}")
  private String bitBucketUserName;

  @Value("${qs.bitbucket.api.branch}")
  private String bitBucketBranch;*/


    public EngHelper(
            LivyActions livyActionsCi,
            AwsAdapter awsAdapterCi,
            ControllerHelper helperCi,
            EngFlowJobService engFlowJobServiceCi,
            EngFlowMetaDataService engFlowMetaDataCi,
            EngFlowEventService engFlowEventServiceCi,
            EngFlowService engFlowServiceCi,
            DatabaseSessionManager sessionManagerCi,
            RedashViewInfoService redashViewInfoServiceCi) {
        livyActions = livyActionsCi;
        awsAdapter = awsAdapterCi;
        helper = helperCi;
        engFlowJobService = engFlowJobServiceCi;
        engFlowMetaData = engFlowMetaDataCi;
        engFlowEventService = engFlowEventServiceCi;
        engFlowService = engFlowServiceCi;
        sessionManager = sessionManagerCi;
        redashViewInfoService = redashViewInfoServiceCi;
    }

    public BatchJobInfo saveEngResult(EngFinalRequest engFinalRequest, boolean saveFinalEventDatatoDb,
                                      StringBuilder fileContents, int engFlowId, EngFlowConfigRequest engFlowConfigRequest, List<String> udfFilePath) throws Exception {
        log.info("Engineering Result Initiated");

        String jobStatus = JobRunState.RUNNING.toString();
        BatchJobInfo batchJobInfo = null;
        Projects project = helper.getProjects(engFinalRequest.getProjectId());
        EngFlowJob flowJob = getEngFlowIdFromJob(engFinalRequest.getJobId());
        //this removed to fix id undefined issue.
        //String dfName = "df" + (engFinalRequest.getEventId1());
        String dfName = fileContents.substring(fileContents.lastIndexOf("print(response"), fileContents.lastIndexOf(".status_code)"));
        dfName = "df" + dfName.replace("print(response", "");
        log.info("fileId---------------->" + dfName);

        Optional<EngFlowEvent> engFlowEventOp = engFlowEventService.getFlowEvent(engFinalRequest.getEventId1());
        String engFlowName = null;
        String parentEngFlowName = null;
        EngFlowEvent engFlowEvent = null;
        if (engFlowEventOp.isPresent()) {
            engFlowEvent = engFlowEventOp.get();
            engFlowName = getEngFlowName(project.getProjectId(), engFlowId);
            parentEngFlowName = getParentEngFlowName(project.getProjectId(), engFlowId);

            log.info("Parent Engineering Flow Name is: {}", parentEngFlowName);
        }

        if (fileContents != null) {
            String targetBucket = project.getBucketName() + "-" + QsConstants.ENG;
            String s3OutputPath = String.format("s3://%s/%s/%s", targetBucket, parentEngFlowName, engFlowName);

            fileContents.append("\n");
            fileContents.append("dfNew=").append(dfName);
            fileContents.append("\n");
            fileContents.append("dfNew.repartition(1).write.format('csv').options(header='true', mode='overWrite').csv('").append(s3OutputPath).append("')");
            fileContents.append("\n\n");

            // Add the dataframes cleanup logic as a final step...
            if (engFlowConfigRequest != null) {
                List<DataFrameRequest> selectedFiles = engFlowConfigRequest.getSelectedFiles();
                if (selectedFiles != null && !selectedFiles.isEmpty()) {
                    selectedFiles.stream().forEach((fileEvent) -> {
                        fileContents.append("df").append(fileEvent.getEventId()).append(" = None").append("\n");
                    });
                }

                List<JoinOperationRequest> selectedJoins = engFlowConfigRequest.getSelectedJoins();
                if (selectedJoins != null && !selectedJoins.isEmpty()) {
                    selectedJoins.stream().forEach((joinEvent) -> {
                        fileContents.append("df").append(joinEvent.getEventId()).append(" = None").append("\n");
                    });
                }

                List<AggregateRequest> aggregates = engFlowConfigRequest.getSelectedAggregates();
                if (aggregates != null && !aggregates.isEmpty()) {
                    aggregates.stream().forEach((aggEvent) -> {
                        fileContents.append("df").append(aggEvent.getEventId()).append(" = None").append("\n");
                    });
                }

                List<UdfOperationRequest> selectedUdf = engFlowConfigRequest.getSelectedUdfs();
                if (selectedUdf != null && !selectedUdf.isEmpty()) {

                    selectedUdf.stream().forEach((udfEvent) -> {
                        Map<String, Integer> eventIds = udfEvent.getEventIds();
                        fileContents.append("df").append(eventIds.get("eventId")).append(" = None").append("\n");
                    });
                }

                fileContents.append("\n").append("spark.sparkContext._jvm.System.gc()\n");
            }

            // Commented below lines of code as we ran into issue with Pandas approach of writing a Dataframe to S3. Reverted back to Spark approach
            // of writing the Dataframe to S3 bucket.

      /*
      fileContents.append("output_df = pd.DataFrame(collData, columns= ").append(dfName).append(".columns)");
      fileContents.append("\n");
      fileContents.append("s3Path='");
      fileContents.append(s3OutputPath).append("'");
      fileContents.append("\n");
      //fileContents.append("output_df.drop(output_df.tail(1).index,inplace=True)");
      //fileContents.append("\n");

      fileContents.append("output_df.to_csv('").append(s3OutputPath).append("', mode = 'w', index=False, encoding='utf-8', escapechar='\\\\')");
      */

            // Upload the prepared Python script to temporary location in S3.
            final URL s3ContentUpload = awsAdapter.s3ContentUpload(qsEtlScriptBucket, engFlowName + ".py", fileContents.toString());
      /*String bitBucketUrl = BitBucketUtil.createFileInRepository(bitBucketApiUrl, bitBucketUserName, bitBucketApiKey,
    		  bitBucketRepository, bitBucketBranch, engFlowName+".py", fileContents.toString());*/

            final String scriptFilePath = String.format("%s%s", qsEtlScriptBucket, engFlowName + ".py");
            log.info("Uploaded to - s3 Full Path - {} and to path {}. Invoking the Livy Batch Job now.", s3ContentUpload, scriptFilePath);
            //log.info("Uploaded to - s3 Full Path - {} and to path {}. Invoking the Livy Batch Job now.", bitBucketUrl, scriptFilePath);

            Instant start = Instant.now();
            batchJobInfo = livyActions.invokeAutorunJobOp(project, scriptFilePath, udfFilePath);
            //batchJobInfo = livyActions.invokeAutorunJobOp(project, bitBucketUrl, udfFilePath);
            jobStatus = batchJobInfo.getBatchJobStatus();
            Instant end = Instant.now();
            batchJobInfo.setBatchJobDuration(Duration.between(start, end).toMillis());

            log.info("Engineering Job status after the batch execution: {}", jobStatus);
            if (JobRunState.SUCCEEDED.toString().equals(jobStatus)) {
                List<String> finalData = livyActions.writeToEngResultBucket(project, dfName, engFinalRequest.getJobId(), parentEngFlowName, engFlowName);
                EngFlowMetaDataAwsRef engFlowMetadataAwsRef = createEngFlowMetadataEntry(project, flowJob, parentEngFlowName, engFlowName);

                String headerLine = finalData.get(0);
                List<String> colHeaders = awsAdapter.getModifiedColHeaders(headerLine);
                List<ColumnMetaData> colMetadata = new ArrayList<>();
                colHeaders.stream().forEach((colHeader) -> colMetadata.add(new ColumnMetaData(colHeader, "string")));
                // Update the metadata of the final event...

                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                String finalEventColMetaJson = mapper.writeValueAsString(colMetadata);
                log.info("----> Final event column metadata JSON is: {}", finalEventColMetaJson);

                engFlowMetadataAwsRef.setEngFlowMetaData(finalEventColMetaJson);
                engFlowMetaData.save(engFlowMetadataAwsRef);

                if (saveFinalEventDatatoDb) {
                    writeEngFlowEventDataToTable(project.getDbSchemaName(), parentEngFlowName, engFlowName, finalData);
                    log.info("Saved the final event data to database as the DB event is enabled in the Engineering Flow...");
                }
            } else {
                log.info("Engineering Job Failed...");
                jobStatus = JobRunState.FAILED.toString();
            }
        }

        return batchJobInfo;
    }

    private String getParentEngFlowName(int projectId, int engFlowId) throws SQLException {
        String parentEngFlowName = "";
        Optional<EngFlow> engFlowOp = engFlowService.getFlowsForProjectAndId(projectId, engFlowId);
        EngFlow engFlow = null;
        if (engFlowOp.isPresent()) {
            engFlow = engFlowOp.get();

            if (engFlow.getParentEngFlowId() == 0) {
                parentEngFlowName = engFlow.getEngFlowName();
            } else {
                return getParentEngFlowName(projectId, engFlow.getParentEngFlowId());
            }
        }

        return parentEngFlowName;
    }

    private String getEngFlowName(int projectId, int engFlowId) throws SQLException {
        Optional<EngFlow> engFlowOp = engFlowService.getFlowsForProjectAndId(projectId, engFlowId);
        EngFlow engFlow = null;
        if (engFlowOp.isPresent()) {
            engFlow = engFlowOp.get();

            return engFlow.getEngFlowName();
        }

        return "";
    }

    private EngFlowMetaDataAwsRef createEngFlowMetadataEntry(Projects project, EngFlowJob flowJob, String tableName, String partitionName) throws SQLException {
        EngFlowMetaDataAwsRef flowMetaDataAwsRef = new EngFlowMetaDataAwsRef();
        flowMetaDataAwsRef.setAthenaTable(tableName);
        flowMetaDataAwsRef.setCreatedDate(new Date());
        flowMetaDataAwsRef.setEngFlowId(flowJob.getEngFlowId());
        flowMetaDataAwsRef.setCreatedBy(helper.getUserName(project.getUserId(), project.getDbSchemaName()));
        flowMetaDataAwsRef.setFlowType("eng");
        flowMetaDataAwsRef.setTablePartition(partitionName);
        flowMetaDataAwsRef.setEngFlowMetaData(null); // TODO METADATA PENDING
        helper.getProjectAndChangeSchema(project.getProjectId());
        return engFlowMetaData.save(flowMetaDataAwsRef);
    }

    private EngFlowJob getEngFlowIdFromJob(int jobId) {
        Optional<EngFlowJob> thisJob = engFlowJobService.getThisJob(jobId);
        return thisJob.orElse(null);
    }

    private void writeEngFlowEventDataToTable(String schemaName, String parentEngFlowName, String engFlowName, List<String> dataLines) throws Exception {
        log.info("DB Schema Name and Engineering Flow Name are: {} and {}.", schemaName, parentEngFlowName);

        // Prepare the Create query:
        String tableName = QsConstants.ENG_FLOW_FINAL_EVENT_RESULT_TBL_PREFIX + parentEngFlowName;
        String primaryKeyName = QsConstants.ENG_FLOW_FINAL_EVENT_RESULT_TBL_PK;
        List<String> modifiedColHeaders = awsAdapter.getModifiedColHeaders(dataLines.get(0));

        EntityManager entityManager = null;
        try {

            entityManager = sessionManager.getEntityManager();
            entityManager.setFlushMode(FlushModeType.COMMIT);
            entityManager.getTransaction().begin();

            // Check whether the table is present in the Schema or not...
            String selectQuery = "SELECT * FROM information_schema.tables where table_schema='" + schemaName + "' and table_name='" + tableName + "'";
            List selResultList = entityManager.createNativeQuery(selectQuery).getResultList();
            boolean tablePresent = false;
            if (selResultList != null && !selResultList.isEmpty()) {
                log.info("Table is already present. No need to create a new one again.");
                tablePresent = true;
            }

            String createTableQuery = prepareCreateOrInsertQuery(schemaName, tableName, primaryKeyName, modifiedColHeaders, tablePresent);
            log.info("\n\nCreate Query is: {}", createTableQuery);

            String viewName = QsConstants.ENG_FLOW_FINAL_EVENT_RESULT_TBL_PREFIX + parentEngFlowName + QsConstants.VIEW;

            String createViewQuery = prepareCreateViewQuery(schemaName, tableName, viewName, modifiedColHeaders);
            log.info("\n\nCreate View Query is: {}", createViewQuery);

            // Execute the Create Query to create the table.
            Object resultObj = entityManager.createNativeQuery("call public.create_eng_flow_job_result_table(:createtablequery, :schemaname, :tablename, :primarykeyname, :viewname, :viewcreatequery, :result)")
                    .setParameter("createtablequery", createTableQuery)
                    .setParameter("schemaname", schemaName)
                    .setParameter("tablename", tableName)
                    .setParameter("primarykeyname", primaryKeyName)
                    .setParameter("viewname", viewName)
                    .setParameter("viewcreatequery", createViewQuery)
                    .setParameter("result", 0)
                    .getSingleResult();

            log.info("Return value after executing the CREATE TABLE Query: {}.", resultObj);

            // Insert the records into the newly created table.
            String insertTableQuery = prepareCreateOrInsertQuery(schemaName, tableName, primaryKeyName, modifiedColHeaders, true);
            log.info("Insert Query is: {}", insertTableQuery);

            DateTime now = DateTime.now();
            RedashViewInfo redashViewInfo = new RedashViewInfo();
            redashViewInfo.setSchemaName(schemaName);
            redashViewInfo.setViewName(viewName);
            redashViewInfo.setActive(true);
            redashViewInfo.setCreationDate(now.toDate());
            redashViewInfoService.save(redashViewInfo);

            Query insertQuery = entityManager.createNativeQuery("call public.insert_eng_flow_job_result_rows(:insertquery, :result)");

            int rowCount = 0;
            List<String> dataLinesWithoutHeader = dataLines.subList(1, dataLines.size());
            for (String dataLine : dataLinesWithoutHeader) {
                String insertTableQueryTmp = insertTableQuery;
                String[] datum = dataLine.split(",");

                insertTableQueryTmp = addParamValue(insertTableQueryTmp, now.toDate().toString());
                insertTableQueryTmp = addParamValue(insertTableQueryTmp, engFlowName);

                for (String data : datum) {
                    insertTableQueryTmp = addParamValue(insertTableQueryTmp, data);
                }

                Object insertRes = insertQuery.setParameter("insertquery", insertTableQueryTmp).setParameter("result", 0).getSingleResult();

                ++rowCount;
            }

            entityManager.flush();
            entityManager.getTransaction().commit();

            log.info("Inserted {} rows into the table: {}.{}", rowCount, schemaName, tableName);
            log.info("Completed saving the final event data into the table: {}.{}", schemaName, tableName);
        } catch (Exception e) {
            log.info("Exception occurred while saving the final event data into the database: {}", e.getMessage());

            entityManager.flush();
            if (entityManager.getTransaction() != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private String addParamValue(String insertQuery, String paramValue) {
        insertQuery = insertQuery.replaceFirst("\\?", (paramValue != null && !paramValue.isEmpty()) ? "'" + paramValue + "'" : null);

        return insertQuery;
    }

    private String prepareCreateViewQuery(String schemaName, String tableName, String viewName, List<String> modifiedColHeaders) {
        log.info("Modified column headers: {}", modifiedColHeaders);

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE VIEW ");
        sb.append(schemaName);
        sb.append(".");
        sb.append(viewName);
        sb.append(" AS ");
        sb.append(" SELECT ");

        for (String colHeader : modifiedColHeaders) {
            sb.append(colHeader);
            sb.append(",");
        }

        sb.deleteCharAt(sb.length() - 1);

        sb.append(" FROM ");
        sb.append(schemaName);
        sb.append(".");
        sb.append(tableName);

        return sb.toString();
    }

    private String prepareCreateOrInsertQuery(String schemaName, String tableName, String primaryKeyName, List<String> modifiedColHeaders, boolean isInsertFlag) {
        log.info("Modified column headers: {}", modifiedColHeaders);

        StringBuilder sb = new StringBuilder();
        if (!isInsertFlag) {
            sb.append("CREATE TABLE IF NOT EXISTS ");
        } else {
            sb.append("INSERT INTO ");
        }

        sb.append(schemaName);
        sb.append(".");
        sb.append(tableName);
        sb.append(" ( ");

        if (!isInsertFlag) {
            sb.append(primaryKeyName);
            sb.append(" integer GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1), creation_date timestamp without time zone, ");
            sb.append(" eng_flow_name character varying(255), ");
        } else {
            sb.append(" creation_date, eng_flow_name, ");
        }

        for (String colHeader : modifiedColHeaders) {
            sb.append(colHeader);

            if (!isInsertFlag) {
                // Append the datatype only if it is a CREATE Query...
                sb.append(" character varying(255) ");
            }

            sb.append(",");
        }

        sb.deleteCharAt(sb.length() - 1); // Delete the last , character
        sb.append(" ) ");

        if (isInsertFlag) {
            sb.append(" VALUES (");
            sb.append("?,?,"); // two place holders for two columns (creation_date, eng_flow_name)
            for (String colHeader : modifiedColHeaders) {
                sb.append("?,");
            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ) ");
        }

        return sb.toString();
    }

    public List<Integer> getEngineeringFlowIds(int projectId, int engFlowId) throws SQLException {
        List<Integer> flowIds = new ArrayList<>();
        flowIds.add(engFlowId);

        // Invoke the below utility method to fetch the eng flow id's recursively..
        getEngFlowIdsRecursively(projectId, flowIds, engFlowId);

        return flowIds;
    }

    private void getEngFlowIdsRecursively(int projectId, List<Integer> flowIds, int engFlowId) throws SQLException {

        // We are interested in the Engineered files used while creating an Engineering flow.
        List<EngFlowEvent> engTypeEvents = engFlowEventService.getAllTypesOfFlows(engFlowId, QsConstants.ENG_OP);
        if (engTypeEvents != null && !engTypeEvents.isEmpty()) {
            engTypeEvents.stream().forEach((event) -> {
                try {
                    int completedEngFlowId = getLatestEngAutoRunId(projectId, event.getFileId());
                    if (completedEngFlowId != -1) {
                        flowIds.add(completedEngFlowId);
                        getEngFlowIdsRecursively(projectId, flowIds, completedEngFlowId);
                    }
                } catch (SQLException e) {
                    log.error("Exception occured while fetching the Engineering flow Job instance..");
                }

            });

            log.info("Final list of Engineering flow Ids used for fetching the Data Lineage information is: {}", flowIds);

        } else {
            log.info("There are no events of type 'eng' for the Engineering Flow Id: {}", engFlowId);
        }
    }

    private int getLatestEngAutoRunId(int projectId, int parentEngFlowId) throws SQLException {
        List<EngFlow> childEngFlows = engFlowService.getRecentChildEngFlow(projectId, parentEngFlowId);

        if (childEngFlows != null && !childEngFlows.isEmpty()) {
            List<Integer> childFlowIds = new ArrayList<>();
            childEngFlows.stream().forEach((flow) -> {
                childFlowIds.add(flow.getEngFlowId());
            });

            List<EngFlowJob> engFlowJobs = engFlowJobService.getCompletedJobsByEngFlowIds(childFlowIds);
            if (engFlowJobs != null && !engFlowJobs.isEmpty()) {
                return engFlowJobs.get(0).getEngFlowId();
            }
        } else {
            log.info("There are no Autorun Engineering flows for the parent engineering flow id: {}", parentEngFlowId);
        }

        return -1;
    }

    public String getDataLineageQuery(String schemaName) {
        StringBuilder sb = new StringBuilder();

        sb.append(" select col, cast (file_id as varchar(50)) as file_id, source, case when target is not null then target ");
        sb.append("     else (select ef.eng_flow_name from ").append(schemaName).append(".qsp_eng_flow ef ");
        sb.append("     where eng_flow_id = (select parent_eng_flow_id from  ").append(schemaName).append(".qsp_eng_flow ");
        sb.append("         where eng_flow_id in (?))) end as target, Category, metaData, fileType, Rules ");
        sb.append(" from ( ");
        sb.append("         select 1 as col, c.file_id, a.folder_display_name as Source, c.file_name as Target, 'Folder' as Category ");
        sb.append(" , '' as metaData, ''  as fileType, '' as Rules ");
        sb.append("         from  ").append(schemaName).append(".qsp_folder a ");
        sb.append("         inner join  ").append(schemaName).append(".qsp_eng_flow_event b  on a.folder_id = b.folder_id ");
        sb.append("         inner join  ").append(schemaName).append(".qsp_file c on c.file_id = b.file_id ");
        sb.append("         where b.eng_flow_id in (?) ");
        sb.append("         union ");
        sb.append("         select 2 as col, f.file_id, f.file_name as Source, CONCAT(f.file_name , ' (Cleansed)') as Target ");
        sb.append("     ,'Cleansed' as Category, '' as  metaData, 'Cleansed'  as fileType, ");
        sb.append("     string_agg(CP.rule_input_logic, CONCAT(': ', CP.rule_input_logic1, ', ')) as Rules ");
        sb.append("         from  ").append(schemaName).append(".qsp_eng_flow_event e  ");
        sb.append("         inner join  ").append(schemaName).append(".qsp_file f on f.file_id = e.file_id ");
        sb.append("         inner join  ").append(schemaName).append(".qsp_cleansing_param CP on CP.file_id = f.file_id ");
        sb.append("         inner join  ").append(schemaName).append(".qsp_run_job_status js on js.file_id = cast(CP.file_id as varchar(50)) ");
        sb.append("         where e.eng_flow_id in (?) and e.file_type = 'processed' group by f.file_name, f.file_id, e.file_type ");
        sb.append("         union ");
        sb.append("         select 3 as col, a.file_id, case when b.event_type='file' then (select case when a.file_type ='processed' then CONCAT(qf.file_name ,' (Cleansed)') ");
        sb.append("                                             else qf.file_name end as f  ");
        sb.append("                                             from  ").append(schemaName).append(".qsp_file qf where qf.file_id=a.file_id) ");
        sb.append("                      when b.event_type='eng' then (select ef.eng_flow_name from  ").append(schemaName).append(".qsp_eng_flow ef where eng_flow_id=a.file_id) ");
        sb.append("                      else b.event_type end as source, (select c.event_type from  ").append(schemaName).append(".qsp_eng_flow_datalineage c where c.event_id = b.event_mapping_id) as target, a.event_type as Category, f.qs_meta_data as metaData, a.file_type as fileType, '' Rules ");
        sb.append("            from   ").append(schemaName).append(".qsp_eng_flow_event a ");
        sb.append("            inner join  ").append(schemaName).append(".qsp_eng_flow_datalineage b on a.eng_flow_event_id=b.event_id ");
        sb.append("            left join  ").append(schemaName).append(".qsp_file f on f.file_id = a.file_id ");
        sb.append("            where a.eng_flow_id  in (?) ");
        sb.append("       ) d order by col asc ");

        return sb.toString();
    }

}
