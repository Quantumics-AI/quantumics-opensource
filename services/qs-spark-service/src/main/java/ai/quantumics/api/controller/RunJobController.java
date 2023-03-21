/*

 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import static ai.quantumics.api.constants.QsConstants.CODE;
import static ai.quantumics.api.constants.QsConstants.COL_ARRAY;
import static ai.quantumics.api.constants.QsConstants.CSV_FILE_NAME;
import static ai.quantumics.api.constants.QsConstants.DB_NAME;
import static ai.quantumics.api.constants.QsConstants.DB_TABLE;
import static ai.quantumics.api.constants.QsConstants.DEF;
import static ai.quantumics.api.constants.QsConstants.FILTER_CONDITION;
import static ai.quantumics.api.constants.QsConstants.PARTITION_NAME;
import static ai.quantumics.api.constants.QsConstants.PROCESSED;
import static ai.quantumics.api.constants.QsConstants.QS_LIVY_TEMPLATE_NAME;
import static ai.quantumics.api.constants.QsConstants.S3_OUT_PUT_PATH;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.exceptions.QsRecordNotFoundException;
import ai.quantumics.api.exceptions.SchemaChangeException;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.helper.RunJobHelper;
import ai.quantumics.api.livy.LivyActions;
import ai.quantumics.api.model.CleansingParam;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsPartition;
import ai.quantumics.api.model.RulesCatalogue;
import ai.quantumics.api.model.RunJobStatus;
import ai.quantumics.api.req.ColumnMetaData;
import ai.quantumics.api.service.CleansingRuleParamService;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.PartitionService;
import ai.quantumics.api.service.ProjectService;
import ai.quantumics.api.service.RulesCatalogueService;
import ai.quantumics.api.service.RunJobService;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.QsUtil;
import ai.quantumics.api.vo.CleanseJobRequest;
import ai.quantumics.api.vo.QsFileContent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@Api(value = "RunJob API ")
@RequestMapping("/api/v1/runjob")
public class RunJobController {

  final String comma = ",";
  final String singleQuote = "'";
  private final QsUtil qsUtil;
  private final DbSessionUtil dbUtil;
  private final AwsAdapter awsAdapter;
  private final ControllerHelper helper;
  private final FileService fileService;
  private final RunJobService runJobService;
  private final FolderService folderService;
  private final ProjectService projectService;
  private final RulesCatalogueService rulesCatalogueService;
  private final CleansingRuleParamService cleanseRuleParamService;
  private final RunJobHelper runJobHelper;
  private final LivyActions livyActions;
  private final PartitionService partitionService;
  

  @Value("${tmp.folder.location}")
  private String tmpFolderLocation;

  @Value("${qs.enable.local.write}")
  private boolean enableLocalStorage;

  @Value("${qs.glue.etl.output}")
  private String qsEtlScriptBucket;

  public RunJobController(
      QsUtil qsUtilCi,
      DbSessionUtil dbUtilCi,
      AwsAdapter awsAdapterCi,
      ControllerHelper helperCi,
      FileService fileServiceCi,
      RunJobService runJobServiceCi,
      FolderService folderServiceCi,
      ProjectService projectServiceCi,
      RulesCatalogueService rulesCatalogueServiceCi,
      CleansingRuleParamService cleanseRuleParamServiceCi,
      RunJobHelper runJobHelperCi,
      LivyActions livyActionsCi,
      PartitionService partitionServiceCi) {
    qsUtil = qsUtilCi;
    dbUtil = dbUtilCi;
    awsAdapter = awsAdapterCi;
    helper = helperCi;
    fileService = fileServiceCi;
    runJobService = runJobServiceCi;
    folderService = folderServiceCi;
    projectService = projectServiceCi;
    rulesCatalogueService = rulesCatalogueServiceCi;
    cleanseRuleParamService = cleanseRuleParamServiceCi;
    runJobHelper = runJobHelperCi;
    livyActions = livyActionsCi;
    partitionService = partitionServiceCi;
  }

  @SuppressWarnings("unused")
  private void enableWriteToLocal(final String temp, final String jobName) {
    OpenOption options = StandardOpenOption.CREATE_NEW;
    if (enableLocalStorage) {
      checkFolderPath();
      final Path newFileUri = Paths.get(String.format("%s%s.py", tmpFolderLocation, jobName));
      final File outputFile = new File(newFileUri.toUri());
      if (outputFile.exists()) {
        options = StandardOpenOption.TRUNCATE_EXISTING;
      }
      try {
        Files.write(newFileUri, temp.getBytes(StandardCharsets.UTF_8), options);
        log.info("Writing File into Local system -> Path - {}", newFileUri);
      } catch (final IOException e) {
        log.error("Error while writing file to storage " + e.getMessage());
      }
    } else {
      log.warn(" Request Ignored ! qs.enable.local.write to enable python script");
    }
  }

  @ApiOperation(value = "Get All previous job's details", response = Json.class)
  @GetMapping("/all/{userId}/{projectId}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "join completed"),
        @ApiResponse(code = 204, message = "Zero Jobs Found!"),
        @ApiResponse(code = 404, message = "join completed")
      })
  public ResponseEntity<Object> getAllRunJobs(
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "projectId") final int projectId) {
    List<RunJobStatus> allJobs;
    Map<String, Object> response;
    try {
      helper.getProjects(projectId);
      allJobs = runJobService.getAllJobs(userId, projectId);
      if (allJobs.isEmpty()) {
        response = helper.failureMessage(204, "Zero Jobs Found!");
      } else {
        response = helper.createSuccessMessage(200, "Run jobs retrieved", allJobs);
      }
    } catch (final SQLException e) {
      log.error("Error : {}", e.getMessage());
      response = helper.failureMessage(404, e.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }

  private List<ColumnMetaData> getMetadata(final String colMetadata)
      throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    List<ColumnMetaData> asList;
    asList = Arrays.asList(mapper.readValue(colMetadata, ColumnMetaData[].class));
    return asList;
  }

  @ApiOperation(value = "Get Cleanse job status", response = Json.class)
  @GetMapping("/job/{projectId}/{runJobId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully returned the status of the cleanse job")})
  public ResponseEntity<Object> getCleanseJobStatus(@PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "runJobId") final int runJobId) {
    Map<String, Object> response;
    try {
      helper.getProjects(projectId);
      Optional<RunJobStatus> runJob1 = runJobService.getRunJob(runJobId);
      response =
          runJob1
              .map(
                  runJobRecord ->
                      helper.createSuccessMessage(200, "Run jobs retrieved", runJobRecord))
              .orElse(helper.failureMessage(204, "Zero Records Found"));

    } catch (final SQLException e) {
      log.error("Error : {}", e.getMessage());
      response = helper.failureMessage(500, e.getMessage());
    }
    return ResponseEntity.ok().body(response);
  }
  
  @ApiOperation(value = "Delete a Cleanse Job.", response = Json.class)
  @DeleteMapping("/job/{projectId}/{userId}/{runJobId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Deleted the cleanse job.")})
  public ResponseEntity<Object> deleteCleanseJob(@PathVariable(value = "projectId") final int projectId,
      @PathVariable(value = "userId") final int userId,
      @PathVariable(value = "runJobId") final int runJobId) {
    Map<String, Object> response;
    try {
      Projects project = helper.getProjects(projectId);
      Optional<RunJobStatus> runJobOp = runJobService.getRunJob(runJobId);
      RunJobStatus runJob = null; 
      if(runJobOp.isPresent()) {
        runJob = runJobOp.get();
        QsFolders folder = folderService.getFolder(runJob.getFolderId());
        Optional<QsFiles> fileOp = fileService.getFileById(runJob.getFileId());
        QsFiles file = null;
        if(fileOp.isPresent()) {
          file = fileOp.get();
        }
        
        deleteAthenaPartition(runJobId, project, folder);
        
        // Finally delete the entry in the Postgres DB.     
        boolean status = deleteCleanseJobDetails(projectId, runJobId, folder);
		
  		if (status && file != null) {

  			String auditMessage = helper.getAuditMessage("cleanse-job", AuditEventTypeAction.DELETE.getAction());

  			String notificationMsg = helper.getAuditMessage("cleanse-job", AuditEventTypeAction.DELETE.getAction());

  			String userName = helper.getUserName(userId);

  			helper.recordAuditEvent(AuditEventType.CLEANSE, AuditEventTypeAction.DELETE,

  					String.format(auditMessage, file.getFileName()),

  					String.format(notificationMsg, file.getFileName(), userName), userId, project.getProjectId());

  		}
		response = helper.createSuccessMessage(200, "Deleted the cleanse job with Id: " + runJobId,
				"Deleted the cleanse job with Id: " + runJobId);
      }else {
        log.info("No Cleanse job found with id: {}", runJobId);
        response = helper.failureMessage(204, "Zero Jobs Found!");
      }
    } catch (final Exception e) {
      log.error("Error : {}", e.getMessage());
      response = helper.failureMessage(500, e.getMessage());
    }
    log.info("Cleanse job with id: {} is deleted successfully", runJobId);
    return ResponseEntity.status((Integer)response.get("code")).body(response);
  }
  
  
  private boolean deleteCleanseJobDetails(final int projectId, final int runJobId, QsFolders folder) {
		log.info("Deleting a cleanse job with id: {}", runJobId);
		boolean status = true;
		try {
			helper.getProjectAndChangeSchema(projectId);
			awsAdapter.deleteFileMetaDataAwsRef(String.valueOf(runJobId));
			runJobService.deleteCleanseJob(runJobId);
		} catch (QsRecordNotFoundException qsRecordNotFoundException) {
			log.error("Error while purging cleanse job[is:{}] entry due to  {}",runJobId, qsRecordNotFoundException.getMessage());
			status = false;
			throw qsRecordNotFoundException;
		} catch (SchemaChangeException schemaChangeException) {
			log.error("Error while purging cleanse job[is:{}] entry due to  {}",runJobId, schemaChangeException.getMessage());
			status = false;
			throw schemaChangeException;
		}
		return status;
	}

  private void deleteAthenaPartition(final int runJobId, Projects project, QsFolders folder) throws IOException {
		// Delete the Cleanse Job entry in AWS S3.
		String bucketName = project.getBucketName() + "-" + QsConstants.PROCESSED;
		log.info("Bucket Name: {}", bucketName);
		String objectKey = folder.getFolderName() + "/" + runJobId + "/";
		log.info("Object Key: {}", objectKey);

		try (S3Object cleanseJobS3Obj = awsAdapter.fetchObject(bucketName, objectKey);) {
			if (cleanseJobS3Obj != null) {
				log.info("Deleting the Cleanse Job Folder and its contents from S3 with key: {}",
						cleanseJobS3Obj.getKey());
				awsAdapter.deleteObject(bucketName, cleanseJobS3Obj.getKey());
				log.info("Deleted the Cleanse Job folder and its contents from S3.");
			} else {
				log.info("Cleanse job folder and its contents are not present in S3 location: {}/{}. Skipping...",
						bucketName, objectKey);
			}
		}
		// Delete the Cleanse Job partition in Athena
		log.info("Dropping the partition with Partition Name: '{}' associated with the Cleanse Job in Athena.",
				runJobId);
		try {
			awsAdapter.deletePartitionForTableInAthena(project.getProcessedDb(), folder.getFolderName(), runJobId + "");
			log.info("Dropped the Partition: {} in Table: {} from Athena.", runJobId, folder.getFolderName());

		} catch (Exception exception) {
			// Proceed in case the Athena table does not exist..
			log.error("Failed to delete the Partition in the Athena Table because: {}", exception.getMessage());
			throw exception;
		}
	}

  private String prepareColArrayDetails(final String colData) {
    final StringBuilder builder = new StringBuilder();
    try {
      final List<ColumnMetaData> getMetadata = getMetadata(colData);
      for (final ColumnMetaData columnMetaData : getMetadata) {
        builder.append(columnMetaData.getColumnName());
        builder.append(",");
      }
      builder.deleteCharAt(builder.lastIndexOf(","));
    } catch (final Exception e) {
      log.error("Error {}", e.getMessage());
    }
    return builder.toString();
  }

  private void prepareRuleDefinitions(
      final List<CleansingParam> rulesCatalogue,
      final StringBuilder cleanseRuleInvocation,
      final StringBuilder cleanseRuleDefinition) {
    dbUtil.changeSchema("public");
    final Instant start = Instant.now();
    log.info("Number of rules with param are {}", rulesCatalogue.size());
    rulesCatalogue.forEach(
        singleItem -> getRulesMapped(cleanseRuleInvocation, cleanseRuleDefinition, singleItem));
    final Instant end = Instant.now();
    log.info("Preparation of script took {}", Duration.between(start, end));
  }
  
  //ToDo: Need to refactor this method and get over the big switch/case that is defined.
  // Planned for Release 2.0.
  private void getRulesMapped(
      StringBuilder cleanseRuleInvocation,
      StringBuilder cleanseRuleDefinition,
      CleansingParam cleansingParam) {
    RulesCatalogue rulesDefinitionByName;
    String ruleName = cleansingParam.getRuleInputLogic();
    String subRuleName = cleansingParam.getRuleInputLogic1();
    
    String finalRuleName = ruleName;
    if(subRuleName != null && !subRuleName.isEmpty()) {
      finalRuleName = ruleName+subRuleName;
    }
    
    log.info("Rule - {} and RuleName is {}", cleansingParam.getRuleSequence(), finalRuleName);
    
    String ruleDelimiter = cleansingParam.getRuleDelimiter();
    ruleDelimiter = (ruleDelimiter == null)? ",":ruleDelimiter;
    final String ruleInputValues = cleansingParam.getRuleInputValues();
    final String ruleInputValues1 = cleansingParam.getRuleInputValues1();
    final String ruleInputValues2 = cleansingParam.getRuleInputValues2();
    final String ruleInputValues3 = cleansingParam.getRuleInputValues3();
    final String ruleInputValues4 = cleansingParam.getRuleInputValues4();
    final String ruleInputValues5 = cleansingParam.getRuleInputValues5();
    final String ruleOutputValues = cleansingParam.getRuleOutputValues();
    String ruleImpactedCols2 = cleansingParam.getRuleImpactedCols();
    if(ruleImpactedCols2 != null && !ruleImpactedCols2.isEmpty()) {
      ruleImpactedCols2 = ruleImpactedCols2.toLowerCase();
    }
    
    final String ruleInputNewcolumns = cleansingParam.getRuleInputNewcolumns();
    
    if(ruleInputValues4 != null && QsConstants.COLUMNS.equals(ruleInputValues4)) {
      if(QsConstants.FILTERROWS.equals(ruleName)) {
        if(QsConstants.TOP_ROWS.equals(subRuleName) || QsConstants.RANGE_ROWS.equals(subRuleName) || QsConstants.TOP_ROWS_AT_REG_INTERVAL.equals(subRuleName)) {
          finalRuleName = finalRuleName+QsConstants.BYCOLUMN;
        }
      }
    } 
    
    log.info("Final Rule name fetched from Rules Catalog is: {}", finalRuleName);
    rulesDefinitionByName = rulesCatalogueService.getRulesDefinitionByName(finalRuleName);
    final String ruleContents = rulesDefinitionByName.getRuleContents();
    
    if (!cleanseRuleDefinition.toString().contains(ruleContents)) {
      cleanseRuleDefinition.append(ruleContents);
    }
    
    cleanseRuleDefinition.append("\n");
    cleanseRuleDefinition.append("\n");
    cleanseRuleInvocation.append("\n");
    
    switch (ruleName) {
      case "findReplace":
        runJobHelper.qsReplaceCells(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1);
        break;
      case "patternReplace":
        runJobHelper.qsReplaceTextOrPattern(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1);
        break;
      case "replace cell":
        runJobHelper.qsReplaceCell(cleanseRuleInvocation, ruleInputValues, ruleOutputValues, ruleImpactedCols2);
        break;
      case "split2Rule":
        runJobHelper.qsSplitTwoRule(cleanseRuleInvocation, ruleDelimiter, ruleImpactedCols2);
        break;
      case "mergeRule":
        runJobHelper.qsMergeRule(cleanseRuleInvocation, ruleDelimiter, ruleInputValues, ruleImpactedCols2);
        break;
      case "groupBysum":
        runJobHelper.qsGroupBySum(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
        break;
      case "groupBymax":
        runJobHelper.qsGroupByMax(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
        break;
      case "groupBymin":
        runJobHelper.qsGroupByMin(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
        break;
      case "groupByavg":
        runJobHelper.qsGroupByAvg(cleanseRuleInvocation, ruleInputValues, ruleImpactedCols2);
        break;
      case "standardize":
        runJobHelper.qsStandardize(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1);
        break;
        
      case "removeDuplicateRows":
        runJobHelper.qsRemoveDuplicates(cleanseRuleInvocation);
        break;
      
      case "countMatch":
        switch(subRuleName) {
          case "Text":
            runJobHelper.qsCountMatchText(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues2);
            break;
          case "Delimiter":
            runJobHelper.qsCountMatchDelimeter(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues2);
            break;
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
      
        break;
      case "fillNull":
        switch(subRuleName) {
          case "WithCustomValue":
            runJobHelper.qsMissingWithCustomValues(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues3);
            break;
          case "WithLastValue":
            runJobHelper.qsMissingWithLastValue(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "WithNullValue":
            runJobHelper.qsMissingWithNullValue(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "WithAverageValue":
            runJobHelper.qsMissingWithAverageValue(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "WithSumValue":
            runJobHelper.qsMissingWithSumValue(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "WithModValue":
            runJobHelper.qsMissingWithModValue(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        
        break;
      case "split":
        switch(subRuleName) {
          case "ByDelimiter":
            runJobHelper.qsSplitByDelimiter(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
          
          case "BetweenTwoDelimiters":
            runJobHelper.qsSplitBetweenTwoDelimeters(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputNewcolumns);
            break;
          
          case "ByMultipleDelimiters":
            runJobHelper.qsSplitByMultipleDelimeters(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns); // comma separated list of delimiters
            break;
          
          case "AtPositions":
            runJobHelper.qsSplitAtPositions(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
            
          case "ByRegularInterval":
            runJobHelper.qsSplitByRegularInterval(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues2, ruleInputNewcolumns);
            break;

          case "BetweenTwoPositions":
            runJobHelper.qsSplitBetweenTwoPositions(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputNewcolumns);
            break;
            
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        break;
      case "extractColumnValues":
        switch(subRuleName) {
          case "FirstNChars":
            runJobHelper.qsExtractFirstNChars(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
          case "LastNChars":
            runJobHelper.qsExtractLastNChars(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
          case "TypeMismatched":
            runJobHelper.qsExtractTypeMismatched(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
          case "CharsInBetween":
            runJobHelper.qsExtractCharsInBetween(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputNewcolumns);
            break;
          case "QueryString":
            runJobHelper.qsExtractQueryString(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
          case "BetweenTwoDelimiters":
            runJobHelper.qsExtractBetweenTwoDelimiters(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues2, ruleInputNewcolumns);
            break;
          case "TextOrPattern":
            runJobHelper.qsExtractTextOrPattern(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues2, ruleInputValues3, ruleInputNewcolumns);
            break;
          case "Numbers":
            runJobHelper.qsExtractNumber(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputNewcolumns);
            break;
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        break;
      case "format":
        switch (subRuleName) {
          case "RemoveWhitespaces":
            runJobHelper.qsTrimWhitespace(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "RemoveAscents":
            runJobHelper.qsRemoveAscents(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "PadWithLeadingCharacters":
            runJobHelper.qsPaddingLeadingChar(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1);
            break;
          case "RemoveSpecialCharacters":
            runJobHelper.qsRemoveSpecialChars(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "TrimQuotes":
            runJobHelper.qsTrimQuotes(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "TrimWhitespace":
            runJobHelper.qsTrimWhitespaces(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "Upper":
            runJobHelper.qsFormatUpper(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "Lower":
            runJobHelper.qsFormatLower(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "Proper":
            runJobHelper.qsFormatProper(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "AddPrefix":
            runJobHelper.qsAddPrefix(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues);
            break;
          case "AddSuffix":
            runJobHelper.qsAddSuffix(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues);
            break;
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        break;
      
      case "manageColumns":
        switch(subRuleName) {
          case "Create":
            runJobHelper.qsManageColumnsCreate(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1);
            break;
          case "Drop":
            runJobHelper.qsManageColumnsDrop(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "Clone":
            runJobHelper.qsManageColumnsClone(cleanseRuleInvocation, ruleImpactedCols2);
            break;
          case "Rename":
            runJobHelper.qsManageColumnsRename(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues);
            break;
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        
        break;
      case "filterRows":
        switch (subRuleName) {
          case "TopRows":
            if(QsConstants.DEFAULT.equals(ruleInputValues4)) {
              runJobHelper.qsTopRows(cleanseRuleInvocation, ruleInputValues, ruleInputValues5);
            } else {
              runJobHelper.qsTopRowsByColumn(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            }
            
            break;
          case "TopRowsAtRegularInterval":
            if(QsConstants.DEFAULT.equals(ruleInputValues4)) {
              runJobHelper.qsTopRowsAtRegularInterval(cleanseRuleInvocation, ruleInputValues, ruleInputValues1, ruleInputValues5);
            }else {
              runJobHelper.qsTopRowsAtRegularIntervalByColumn(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues5);
            }
            
            break;
          case "RangeRows":
            if(QsConstants.DEFAULT.equals(ruleInputValues4)) {
              runJobHelper.qsRangeRows(cleanseRuleInvocation, ruleInputValues, ruleInputValues1, ruleInputValues5);
            } else {
              runJobHelper.qsRangeRowsByColumn(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues5);
            }
            
            break;
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        
        break;
      case "filterRowsByColumn":  
        switch(subRuleName) {  
          case "Missing":
            runJobHelper.qsIsMissing(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues5);
            break;
          case "Exactly":
            runJobHelper.qsIsExactly(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "NotEqualTo":
            runJobHelper.qsNotEqualTo(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "ValueIsOneOf":
            runJobHelper.qsColumnValueIsOneOf(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "LessThanOrEqualTo":
            runJobHelper.qsLessThanOrEqualTo(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "GreaterThanOrEqualTo":
            runJobHelper.qsGreaterThanOrEqualTo(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "Between":
            runJobHelper.qsIsBetween(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues1, ruleInputValues5);
            break;
          case "Contains":
            runJobHelper.qsIsContains(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "StartsWith":
            runJobHelper.qsStartsWith(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;
          case "EndsWith":
            runJobHelper.qsEndsWith(cleanseRuleInvocation, ruleImpactedCols2, ruleInputValues, ruleInputValues5);
            break;        
          default:
            log.error("This {}{} not supported yet! ", ruleName, subRuleName);
            break;
        }
        break;
      default:
        log.error("This {} not supported yet! ", ruleName);
        break;
    }
  }

  @ApiOperation(value = "runjob", response = Json.class)
  @PostMapping(value="/cleanse",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Cleanse Job completed Successfully !"),
        @ApiResponse(code = 404, message = "Specific Rule not found")
      })
  public ResponseEntity<Object> runJob(@RequestBody final CleanseJobRequest cleanseJobReq) {
    Map<String, Object> response;
    final StringBuilder fileContents = new StringBuilder();
    final StringBuilder cleanseRuleInvocation = new StringBuilder();
    final StringBuilder cleanseRuleDefinition = new StringBuilder();
    
    try {
      log.info("Received Cleansed Job Request with inputs: {}", cleanseJobReq.toString());
      
      dbUtil.changeSchema("public");
      final Projects project = projectService.getProject(cleanseJobReq.getProjectId());
      dbUtil.changeSchema(project.getDbSchemaName());
      Optional<QsFiles> files = fileService.getFileById(cleanseJobReq.getFileId());
      QsFiles qsFiles;
      if (files.isPresent()) {
        DateTime now = DateTime.now();
        log.info("-------------> Started preparing the ETL template for the rule execution...");
        
        qsFiles = files.get();
        QsFolders folder = folderService.getFolder(qsFiles.getFolderId());
        int folderId = folder.getFolderId();
        log.info("Files found by id {} and folders {}", qsFiles.toString(), folder.toString());
        List<CleansingParam> rulesBySequence =
            cleanseRuleParamService.getRulesOrderByRuleSequence(folderId, cleanseJobReq.getFileId());
        prepareRuleDefinitions(rulesBySequence, cleanseRuleInvocation, cleanseRuleDefinition);
        QsFileContent tableAwsName = getTableAwsName(project, qsFiles, folder);
        String tableName = tableAwsName.getTableName();
        String partitionName = "";
        Optional<QsPartition> partitionOp = partitionService.getPartitionByFileId(folderId, qsFiles.getFileId()); 
        if(partitionOp.isPresent()) {
          partitionName = partitionOp.get().getPartitionName();
        }
        
        checkFolderPath();
        readLinesFromTemplate(fileContents);
        
        log.info("------> Elapsed time in preparing the ETL template: {}", Seconds.secondsBetween(now, DateTime.now()));
        String temp =
            etlScriptVarsInit(
                project,
                folder.getFolderName(),
                qsFiles.getFileName(),
                partitionName,
                fileContents,
                cleanseRuleInvocation,
                cleanseRuleDefinition,
                tableName,
                cleanseJobReq.getRunJobId());
        
        temp = prepareArray(qsFiles, temp);
        final String jobName = getJobName(project);
        
        final URL s3ContentUpload = awsAdapter.s3ContentUpload(qsEtlScriptBucket, jobName, temp);
        final String scriptFilePath = String.format("%s%s", qsEtlScriptBucket, jobName);
        log.info("uploaded to - s3 Full Path - {} and to path {}", s3ContentUpload, scriptFilePath);
        
        // Invoke Apache Livy call here... 
        int batchId = livyActions.invokeCleanseJobOperation(project, cleanseJobReq.getRunJobId(), tableAwsName, scriptFilePath);
        log.info("Batch Id received after the Livy Job submission is: {}", batchId);
        
        response = helper.createSuccessMessage(200, "Job Running !", "");
      } else {
        response = helper.failureMessage(200, "File Information not found!");
      }
    } catch (final Exception e) {
      log.error(" ETL Template not found !" + e.getMessage());
      response = helper.failureMessage(500, e.getMessage());
    }
    
    return ResponseEntity.ok().body(response);
  }
  
  private QsFileContent getTableAwsName(Projects project, QsFiles qsFiles, QsFolders folder)
      throws SQLException {
    QsFileContent tableNameRef = new QsFileContent();
    tableNameRef.setFolderName(folder.getFolderName());
    tableNameRef.setFolderId(folder.getFolderId());
    tableNameRef.setFileName(qsFiles.getFileName());
    tableNameRef.setFileId(qsFiles.getFileId());
    tableNameRef.setProject(project);
    tableNameRef = qsUtil.decideTableName(tableNameRef, false);
    return tableNameRef;
  }

  private void readLinesFromTemplate(StringBuilder fileContents) throws Exception {
    File contentSource = ResourceUtils.getFile("./"+QS_LIVY_TEMPLATE_NAME);
    log.info("File in classpath Found {} : ", contentSource.exists());
    fileContents.append(new String(Files.readAllBytes(contentSource.toPath())));
  }

  private String getJobName(Projects project) {
    return String.format("%d-etl-%d.py", new Date().getTime(), project.getUserId());
  }
  
  private String getFilterCondition(String colsStr) {
    
    String[] colsArr = colsStr.split(",");
    List<String> values = Arrays.asList(colsArr);
    
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    int colCount = values.size();
    int index = 1;
    for(String col : values)
    {
      sb.append("(").append(col).append(" != ").
      append("'").append(col).append("'").append(")");
      
      if(index < colCount) {
        sb.append(" and ");
      }
      
      ++index;
    }
    
    sb.append("\"");
    sb.append("\n");
    
    return sb.toString();
  }

  private String prepareArray(QsFiles qsFiles, String temp) {
    final String colMetadata = qsFiles.getQsMetaData();
    String colsStr = prepareColArrayDetails(colMetadata);
    String filterCond = getFilterCondition(colsStr);
    colsStr = "'"+colsStr+"'";
    temp = temp.replace(COL_ARRAY, colsStr);
    temp = temp.replace(FILTER_CONDITION, filterCond);
    
    return temp;
  }

  private String etlScriptVarsInit(
      Projects project,
      String folderName,
      String fileName,
      String partitionName,
      StringBuilder fileContents,
      StringBuilder cleanseRuleInvocation,
      StringBuilder cleanseRuleDefinition,
      String tableName,
      int jobId) {
    
    String temp;
    StringBuilder sb = new StringBuilder();
    log.info("File Name: {}", fileName);
    
    sb.append(fileName.substring(0, fileName.lastIndexOf("."))).append("_processed").append(".csv");
    String fileNameProc = sb.toString();
    
    final String targetBucketName =
        String.format("s3://%s-%s/%s/%s", project.getBucketName(), PROCESSED, folderName, jobId);
    temp = fileContents.toString().replace(DB_NAME, String.format("'%s'", project.getRawDb()));
    temp = temp.replace(DB_TABLE, String.format("'%s'", tableName));
    temp = temp.replace(CSV_FILE_NAME, String.format("'%s'", fileNameProc));
    temp = temp.replace(PARTITION_NAME, String.format("'%s'", partitionName));
    temp = temp.replace(S3_OUT_PUT_PATH, String.format("'%s'", targetBucketName));
    temp = temp.replace(DEF, String.format("###$DEF;\n%s", cleanseRuleDefinition));
    temp = temp.replace(CODE, String.format("%s\n%s", CODE, cleanseRuleInvocation));
    return temp;
  }

  private void checkFolderPath() {
    final File file = new File(tmpFolderLocation);
    if (!file.exists() || !file.isDirectory()) {
      boolean mkdir = file.mkdir();
      log.info("Creating directory required to store data {}", mkdir);
    } else {
      log.info("Directory Path required to store download data is existed");
    }
  }
}
