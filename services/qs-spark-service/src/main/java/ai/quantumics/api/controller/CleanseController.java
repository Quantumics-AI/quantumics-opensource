/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.req.ApiIntegrationRequest;
import ai.quantumics.api.req.DatabaseFileRequest;
import ai.quantumics.api.req.FileColumnValidationRequest;
import ai.quantumics.api.req.UploadFileRequest;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.ProjectService;
import ai.quantumics.api.service.UserServiceV2;
import ai.quantumics.api.util.ApiIntegrationUtils;
import ai.quantumics.api.util.DbSessionUtil;
import ai.quantumics.api.util.QsUtil;
import ai.quantumics.api.vo.S3FileUploadResponse;
import com.amazonaws.AmazonServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@Api(value = "QuantumSpark Service API ")
@RequestMapping("/api/v1")
@Slf4j
public class CleanseController {

    private static final String XLS = ".xls";
    private static final String XLSX = ".xlsx";
    private static final String TXT = ".txt";
    private static final String CSV = ".csv";
    @Autowired
    private QsUtil qsUtil;
    @Autowired
    private DbSessionUtil dbUtil;
    @Autowired
    private AwsAdapter awsAdapter;
    @Autowired
    private UserServiceV2 userService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApiIntegrationUtils apiIntgUtils;
    @Autowired
    private ControllerHelper controllerHelper;

    @ApiOperation(value = "bucketName", response = Json.class)
    @GetMapping("/bucketName/{projectId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get BucketName")})
    public ResponseEntity<Object> getBucketName(
            @PathVariable(value = "projectId") final int projectId) {
        final Map<String, Object> response = new HashMap<>();
        dbUtil.changeSchema("public");
        log.info("getBucketName of the Project= {}", projectId);
        response.put("code", 200);
        response.put("bucket_name", projectService.getProject(projectId).getBucketName());
        return ResponseEntity.ok().body(response);
    }

    @ApiOperation(value = "config", response = Json.class)
    @GetMapping("/config")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get Configuration")})
    public ResponseEntity<String> getConfig() {
        return ResponseEntity.ok().body("Yet to Complete!");
    }

    @ApiOperation(value = "health", response = String.class)
    @GetMapping("/health")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Health Check!")})
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok().body("It Works !");
    }

    @SuppressWarnings("unused")
    private void updateFileId(final int folderId, final QsFiles saveFileInfo) throws SQLException {
        final QsFolders folder2 = folderService.getFolder(folderId);
        folder2.setFileId(saveFileInfo.getFileId());
        folderService.saveFolders(folder2);
    }

    @GetMapping(
            value = "/validatefileupload/verify")
    public List<String> getPiiColumnInfo(final String inputFileWithPath) throws Exception {
        List<String> commands = new ArrayList<>();
        log.info("get PII column info :{}", inputFileWithPath);
        try {
            InputStream in = CleanseController.class.getClassLoader().getResourceAsStream(QsConstants.PII_COL_DETECTION_PYTHON_FILE_REL_LOC);
            URL url = getClass().getClassLoader().getResource(QsConstants.PII_COL_DETECTION_PYTHON_FILE_REL_LOC);
            String path = this.getClass().getClassLoader().getResource(QsConstants.PII_COL_DETECTION_PYTHON_FILE_REL_LOC).toExternalForm();
            log.info("get PII column info url:{}", path);
            log.info("get PII column info url.toURI:{}", url.toURI());

            URL resource = getClass().getClassLoader().getResource("/" + QsConstants.PII_COL_DETECTION_PYTHON_FILE_REL_LOC);
            log.info("========>resource : " + resource.getPath());
            log.info("========>" + new File(resource.toURI()).getAbsolutePath());
            File detectionPyFile = null;
            if (url != null) {
                try {
                    detectionPyFile = new File(url.toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Exception while converting URI..{}", e);
                }

            } else {
                log.info("Couldn't locate the Python script.");

                return null;
            }

            log.info("get PII column info detectionPyFile.getAbsolutePath :{}", detectionPyFile.getAbsolutePath());

            if (isWindows()) {
                commands.add("cmd.exe");
                commands.add("/c");
                commands.add("python");
            } else {
                commands.add("python3");
            }

            commands.add(detectionPyFile.getAbsolutePath());
            commands.add(inputFileWithPath);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception in getPiiColumnInfo.. {}", e);
        }

        return commands;
    }

    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }


    @ApiOperation(value = "File Upload validation", response = Json.class)
    @PostMapping(
            value = "/validatefileupload/{projectId}/{userId}/{folderId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "File received by Server !"),
                    @ApiResponse(code = 400, message = "Malformed or Invalid Content!")
            })
    public ResponseEntity<Object> validateFileUpload(
            @RequestPart(value = "file") final MultipartFile file,
            @PathVariable(value = "projectId") final int projectId,
            @PathVariable(value = "userId") final int userId,
            @PathVariable(value = "folderId") final int folderId) {
        log.info("Received projectId - {} and folderId {}", projectId, folderId);
        final Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> resultObj = new ArrayList<>();
        File tmpJsonFile = null;
        try {
            log.info("Received file upload validation request on: {}", QsConstants.getCurrentUtcDate());

            dbUtil.changeSchema("public");
            final Projects project = projectService.getProject(projectId, userId);
            if (project == null) {
                response.put("code", HttpStatus.BAD_REQUEST);
                response.put("message", "Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

                return ResponseEntity.ok().body(response);
            }

            final QsUserV2 userObj = userService.getUserById(userId);
            dbUtil.changeSchema(project.getDbSchemaName());

            final QsFolders folder = folderService.getFolder(folderId);
            Instant start = Instant.now();
            //do xlsx change, need to fetch header and one column
            log.info("Received file to upload. {}", file.getOriginalFilename());
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            log.info("fileExtension " + fileExtension);
            Map<String, String> validationErrorMsgMap = null;
            // xlsx file conversion changes
            if (CSV.equalsIgnoreCase(fileExtension) || TXT.equalsIgnoreCase(fileExtension))
                validationErrorMsgMap = qsUtil.validateFileBeforeUpload(file, folder, project, userObj, "validatefileupload", fileExtension);
            else if (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension))
                validationErrorMsgMap = qsUtil.validateXlsxFileBeforeUpload(file, folder, project, userObj, "validatefileupload", fileExtension);

            log.info("Elapsed time in validating the file before upload: {} msecs",
                    (Duration.between(start, Instant.now()).toMillis()));

            if (validationErrorMsgMap != null && !validationErrorMsgMap.isEmpty()) {
                response.put("code", 400);
                response.put("message", validationErrorMsgMap);
                log.error("Validation error before uploading the file. Detailed message is: {}",
                        validationErrorMsgMap.toString());

                return ResponseEntity.status(400).body(response);
            }
            log.info("going to process file.");
            // Perform the PII Validation now..
            start = Instant.now();
            String tmpdir = System.getProperty("java.io.tmpdir");
            String fileNameWithoutExt = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
            String tmpCsvFilePath = String.format("%s/%s/%s/%s/%s.csv", tmpdir, projectId, userId, folderId, fileNameWithoutExt);
            //convert xlsx to csv file here
            String tmpJsonFilePath = null;
            log.info("going to process file. ==>" + tmpCsvFilePath);
            log.info("going to process file.fileExtension ==>" + fileExtension);
            // xlsx file conversion changes
            if (CSV.equalsIgnoreCase(fileExtension) || TXT.equalsIgnoreCase(fileExtension))
                tmpJsonFilePath = qsUtil.checkPiiColumnsInFile(file, projectId, userId, folderId, fileExtension);
            else if (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension))
                tmpJsonFilePath = qsUtil.checkPiiColumnsInXlsxFile(file, projectId, userId, folderId, fileExtension);

            log.info("going to process file.1" + tmpJsonFilePath);
            tmpJsonFile = new File(tmpJsonFilePath);

            String piiColumnsInfo = awsAdapter.getPiiColumnInfo(tmpJsonFilePath);
            piiColumnsInfo = piiColumnsInfo.replaceAll("'", "\"");
            log.info("------------------->PII Columns information is: {}", piiColumnsInfo);
            log.info("Elapsed time in handling the PII validation is: {} msecs.",
                    (Duration.between(start, Instant.now()).toMillis()));

            Map<String, String> finalRes = new HashMap<>();
            finalRes.put("CSV_FILE_PATH", tmpCsvFilePath);
            finalRes.put("PII_COLUMNS_INFO", piiColumnsInfo);
            finalRes.put("code", "200");
            finalRes.put("message", "File validation completed!");
            response.put("Ptype", "FU");
            response.put("result", resultObj);
            resultObj.add(finalRes);

            return ResponseEntity.ok().body(response);
        } catch (final AmazonServiceException e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception {}", e.getErrorMessage());
            return ResponseEntity.status(500).body(response);

        } catch (final SQLException e) {
            response.put("code", 422);
            response.put("message", e.getMessage());
            log.error("Exception - partition {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } catch (final Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception Occurred {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } finally {
            if (tmpJsonFile != null && tmpJsonFile.exists()) {
                tmpJsonFile.delete();
            }
        }
    }

    @ApiOperation(value = "File Upload", response = Json.class)
    @PostMapping(
            value = "/upload/{projectId}/{userId}/{folderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "File uploaded successfully"),
                    @ApiResponse(code = 400, message = "Malformed or Invalid Content!")
            })
    public ResponseEntity<Object> upload(
            @PathVariable(value = "projectId") final int projectId,
            @PathVariable(value = "userId") final int userId,
            @PathVariable(value = "folderId") final int folderId, @RequestBody final UploadFileRequest uploadFileRequest) {
        log.info("Received projectId - {} and folderId {}", projectId, folderId);
        try {
            return uploadObject(uploadFileRequest, projectId, userId, folderId);

        } catch (final Exception e) {
            log.error("Exception Occurred {}", e.getMessage());
        }
        return null;
    }


    @ApiOperation(value = "Database File Upload", response = Json.class)
    @PostMapping(
            value = "/uploadexternaldata/{projectId}/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Database File uploaded successfully"),
                    @ApiResponse(code = 400, message = "Malformed or Invalid Content!")
            })
    public List<Object> uploadExternalData(
            @PathVariable(value = "projectId") final int projectId,
            @PathVariable(value = "userId") final int userId,
            @RequestBody final DatabaseFileRequest databaseFileRequest) {
        log.info("Received projectId - {} and folderId {}", projectId);
        List<Object> listObj = new ArrayList<>();
        try {
            List<UploadFileRequest> lstuploadFileRequest = databaseFileRequest.getDatabaseObjectRequest();
            lstuploadFileRequest.forEach(uploadFileRequest -> {
                ResponseEntity<Object> response = uploadObject(uploadFileRequest, projectId, userId, uploadFileRequest.getFolderId());
                listObj.add(response.getBody());
            });
        } catch (final Exception e) {
            log.error("Exception Occurred {}", e.getMessage());
        }
        return listObj;
    }


    public ResponseEntity<Object> uploadObject(final UploadFileRequest uploadFileRequest,
                                               final int projectId, final int userId, final int folderId) {
        final Map<String, Object> response = new HashMap<>();

        File csvFile = null;
        File csvFileTmp = null;
        BufferedWriter bw = null;
        try {
            log.info("Received file upload request on: {}", QsConstants.getCurrentUtcDate());

            dbUtil.changeSchema("public");
            final Projects project = projectService.getProject(projectId, userId);
            if (project == null) {
                response.put("code", HttpStatus.BAD_REQUEST);
                response.put("message", "Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

                return ResponseEntity.ok().body(response);
            }

            final QsUserV2 userObj = userService.getUserById(userId);
            dbUtil.changeSchema(project.getDbSchemaName());
            final QsFolders folder = folderService.getFolder(folderId);
            csvFile = new File(uploadFileRequest.getCsvFilePath());

            String path = csvFile.getParentFile().getAbsolutePath();
            String tmpCsvFileNameWithoutExt = csvFile.getName().substring(0, csvFile.getName().lastIndexOf("."));
            String tmpCsvFileName = String.format("%s/tmp/%s.csv", path, tmpCsvFileNameWithoutExt);
            csvFileTmp = new File(tmpCsvFileName);
            if (!csvFileTmp.getParentFile().exists()) {
                csvFileTmp.getParentFile().mkdirs();
            }

            Map<String, String> validationErrorMsgMap =
                    qsUtil.validateFileBeforeUpload(csvFile, folder, project, userObj, "upload", ".csv");

            if (validationErrorMsgMap != null && !validationErrorMsgMap.isEmpty()) {
                response.put("code", 400);
                response.put("message", validationErrorMsgMap);
                log.error("Validation error before uploading the file. Detailed message is: {}",
                        validationErrorMsgMap.toString());

                return ResponseEntity.status(400).body(response);

            }

            // Below tasks should be performed here...
            // 1. if there are any PII columns in the CSV file, they have to be encrypted.
            // 2. if User asks for dropping any specific column from the CSV file before uploading, then, thos cols must be dropped as well.

            controllerHelper.processFileBeforeUpload(uploadFileRequest, csvFile, csvFileTmp);

            // Upload the file now...
            final String pathSep = "/";
            String partitionRef = qsUtil.randomAlphaNumeric();
            partitionRef = partitionRef.toLowerCase();

            final String qsPartition = folder.getFolderName() + pathSep + partitionRef;
            final String fileName = qsPartition + pathSep + csvFile.getName();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
            fileExt = fileExt.toLowerCase();
            String finalFileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + fileExt;

            log.info("Received upload file request for {}", fileName);
            String userName = controllerHelper.getFullName(userObj.getQsUserProfile());

            final S3FileUploadResponse s3FileUploadRes = awsAdapter.storeObjectInS3Async(project.getBucketName() + "-raw", csvFileTmp,
                    partitionRef, project.getRawDb(),
                    folder.getFolderName(), finalFileName, "application/csv");

            URL destUrl = awsAdapter.updateFileMdAfterUpload(s3FileUploadRes, project, folder,
                    userName, partitionRef, userId, uploadFileRequest);

            // Update the Folder Last Updated Date and Last Updated by..
            folder.setModifiedDate(QsConstants.getCurrentUtcDate());
            folder.setModifiedBy(Integer.toString(userId));
            folderService.saveFolders(folder);

            response.put("code", 200);
            response.put("result", destUrl);
            response.put("message", "File uploaded successfully!");

            log.info("Completed file upload request on: {}", QsConstants.getCurrentUtcDate());

            String auditMessage = controllerHelper.getAuditMessage(AuditEventType.INGEST, AuditEventTypeAction.CREATE);
            String notification = controllerHelper.getNotificationMessage(AuditEventType.INGEST, AuditEventTypeAction.CREATE);
            controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.CREATE,
                    String.format(auditMessage, csvFile.getName()),
                    String.format(notification, csvFile.getName(), userName),
                    userObj.getUserId(), project.getProjectId());

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception Occurred {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }

            if (csvFileTmp != null && csvFileTmp.exists()) {
                csvFileTmp.delete();
            }

            if (csvFile != null && csvFile.exists()) {
                csvFile.delete();
            }
        }
    }

    @ApiOperation(value = "Incremental File Upload", response = Json.class)
    @PostMapping(
            value = "/incrementalupload/{projectId}/{userId}/{folderId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "File received by Server!"),
                    @ApiResponse(code = 400, message = "Malformed or Invalid Content!")
            })
    public ResponseEntity<Object> uploadFile(
            @RequestPart(value = "file") final MultipartFile file,
            @PathVariable(value = "projectId") final int projectId,
            @PathVariable(value = "userId") final int userId,
            @PathVariable(value = "folderId") final int folderId) {
        log.info("Received projectId - {} and folderId {}", projectId, folderId);
        final Map<String, Object> response = new HashMap<>();

        File csvFile = null;
        File csvFileTmp = null;
        BufferedWriter bw = null;
        String csvPath = null;
        File cfile = null;
        InputStream inputStream = null;
        try {
            log.info("Received file upload request on: {}", QsConstants.getCurrentUtcDate());

            dbUtil.changeSchema("public");
            final Projects project = projectService.getProject(projectId, userId);
            if (project == null) {
                response.put("code", HttpStatus.BAD_REQUEST);
                response.put("message", "Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

                return ResponseEntity.ok().body(response);
            }

            final QsUserV2 userObj = userService.getUserById(userId);
            dbUtil.changeSchema(project.getDbSchemaName());

            final QsFolders folder = folderService.getFolder(folderId);
            //modify it for xlsx
            Instant start = Instant.now();
            //do xlsx change, need to fetch header and one column
            log.info("Received file to upload. {}", file.getOriginalFilename());
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            log.info("fileExtension " + fileExtension);
            Map<String, String> validationErrorMsgMap = null;
            // xlsx file conversion changes
            if (CSV.equalsIgnoreCase(fileExtension) || TXT.equalsIgnoreCase(fileExtension))
                validationErrorMsgMap = qsUtil.validateFileBeforeUpload(file, folder, project, userObj, "incrementalupload", fileExtension);
            else if (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension))
                validationErrorMsgMap = qsUtil.validateXlsxFileBeforeUpload(file, folder, project, userObj, "incrementalupload", fileExtension);

            log.info("Elapsed time in validating the file before upload: {} msecs",
                    (Duration.between(start, Instant.now()).toMillis()));

            if (validationErrorMsgMap != null && !validationErrorMsgMap.isEmpty()) {
                response.put("code", 400);
                response.put("message", validationErrorMsgMap);
                log.error("Validation error before uploading the file. Detailed message is: {}",
                        validationErrorMsgMap.toString());

                return ResponseEntity.status(400).body(response);
            }

            //if file is xlsx then convert to csv
            if (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension)) {
                log.info("Service received xlsx file, going to convert xlsx to csv. userId :{}", userId);
                csvPath = qsUtil.writeXlsxFileToCsv(file, projectId, userId, folderId);
                cfile = new File(csvPath);
                inputStream = new FileInputStream(cfile);
            }

            dbUtil.changeSchema(project.getDbSchemaName());
            // Get the PII info and perform the Drop or Encrypt columns action before uploading...
            QsFiles oldestFile = fileService.getOldestFileInFolder(projectId, userId, folderId);
            UploadFileRequest uploadFileReq = null;
            if (oldestFile != null) {
                final ObjectMapper mapper = new ObjectMapper();
                if (oldestFile.getAdditionalInfo() != null) {
                    uploadFileReq = mapper.readValue(oldestFile.getAdditionalInfo(), UploadFileRequest.class);
                    if (uploadFileReq != null) {
                        if (uploadFileReq.getDropColumns() != null && uploadFileReq.getEncryptPiiColumns() != null) {
                            csvFile = new File(uploadFileReq.getCsvFilePath());

                            CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
                            CsvMapper csvMapper = new CsvMapper();
                            //modify it for xlsx file
                            List<Object> readAllLines = null;
                            log.info("Read file==>" + fileExtension);
                            String s = null;
                            int lineNo = 0;
                            String delimeter = null;
                            if (CSV.equalsIgnoreCase(fileExtension) || TXT.equalsIgnoreCase(fileExtension)) {
                                BufferedReader brCsv = new BufferedReader(new InputStreamReader(file.getInputStream()));
                                while ((s = brCsv.readLine()) != null) {
                                    if (lineNo == 0) {
                                        if (s.indexOf(QsConstants.DELIMITER) != -1) {
                                            delimeter = QsConstants.DELIMITER;
                                        } else if (s.indexOf("\t") != -1) {
                                            delimeter = "\t";
                                        } else if (s.indexOf("|") != -1) {
                                            delimeter = "|";
                                        }
                                        break;
                                    }
                                }
                                csvSchema = csvMapper.typedSchemaFor(Map.class)
                                        .withColumnSeparator(delimeter.charAt(0))
                                        .withHeader();
                                readAllLines = csvMapper.readerFor(Map.class).with(csvSchema)
                                        .readValues(new BufferedReader(new InputStreamReader(file.getInputStream()))).readAll();
                            } else if (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension)) {
                                try {
                                    log.info("Xlsx to csv conversion is done, Going to read csv file to upload. path :{}", csvPath);
                                    readAllLines = csvMapper.readerFor(Map.class).with(csvSchema)
                                            .readValues(new BufferedReader(new InputStreamReader(inputStream))).readAll();
                                } catch (Exception e) {
                                    log.error("Unable to read csv file from temp loaction.{}, error :{}", csvPath, e);
                                }
                            }

                            String dropColumns = uploadFileReq.getDropColumns();
                            // Drop the columns from the CSV file here..

                            controllerHelper.dropColumnsFromCsvFile(readAllLines, dropColumns);

                            String encryptPiiColumns = uploadFileReq.getEncryptPiiColumns();
                            // Encrypt the columns here..

                            controllerHelper.encryptColumnsOfCsvFile(readAllLines, encryptPiiColumns);

                            // Finally write the list of lines back to the CSV file and then upload...
                            String path = csvFile.getParentFile().getAbsolutePath();
                            String tmpCsvFileNameWithoutExt = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
                            String tmpCsvFileName = String.format("%s/tmp/%s.csv", path, tmpCsvFileNameWithoutExt);
                            csvFileTmp = new File(tmpCsvFileName);
                            if (!csvFileTmp.getParentFile().exists()) {
                                csvFileTmp.getParentFile().mkdirs();
                            }

                            log.info("Column validation done, write csv file started.");
                            CsvSchema.Builder schemaBuilder = CsvSchema.builder();
                            CsvSchema schema = null;
                            if (readAllLines != null && !readAllLines.isEmpty()) {
                                Object obj = readAllLines.get(0);
                                Map<String, String> firstItem = (Map<String, String>) obj;
                                Map<String, String> columnDetails = new HashMap<>();
                                List<Map<String, String>> allLines = new ArrayList<>();

                                for (String col : firstItem.keySet()) {
                                    String replacedColumn = col.replaceAll("\\W", "_").replaceAll("_{2,}", "_");
                                    columnDetails.put(col, replacedColumn);
                                    schemaBuilder.addColumn(replacedColumn);
                                }
                                for (Object object : readAllLines) {
                                    Map<String, String> allLinesMap = new HashMap<>();
                                    Map<String, String> item = (Map<String, String>) object;
                                    for (String key : item.keySet()) {
                                        allLinesMap.put(columnDetails.get(key), item.get(key));
                                    }
                                    allLines.add(allLinesMap);
                                }

                                schema = schemaBuilder.build().withLineSeparator("\r").withHeader();
                                bw = new BufferedWriter(new FileWriter(csvFileTmp));
                                csvMapper.writer(schema).writeValues(bw).writeAll(allLines);
                            }

                            bw.flush();
                        }
                    }
                }
            }
            log.info("CSV file write is done!");
            final String pathSep = "/";
            String partitionRef = qsUtil.randomAlphaNumeric();
            partitionRef = partitionRef.toLowerCase();

            final String qsPartition = folder.getFolderName() + pathSep + partitionRef;
            final String fileName = qsPartition + pathSep + file.getOriginalFilename();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
            fileExt = fileExt.toLowerCase();
            String finalFileName = null;

            if (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension) || TXT.contentEquals(fileExtension)) {
                finalFileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "csv";
            } else {
                finalFileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + fileExt;
            }


            log.info("Received upload file request for {}, finalFileName : {} , type : {}", fileName, finalFileName, file.getContentType());
            String userName = controllerHelper.getFullName(userObj.getQsUserProfile());
            //need modification for xlsx
            Object inFile = (XLSX.contentEquals(fileExtension) || XLS.contentEquals(fileExtension)) ? cfile : file;
            final S3FileUploadResponse s3FileUploadRes = awsAdapter.storeObjectInS3Async(project.getBucketName() + "-raw", (csvFileTmp != null ? csvFileTmp : inFile),
                    partitionRef, project.getRawDb(),
                    folder.getFolderName(), finalFileName, "text/csv");


            URL destUrl = awsAdapter.updateFileMdAfterUpload(s3FileUploadRes, project, folder, userName, partitionRef, userId, uploadFileReq);

            // Update the Folder Last Updated Date and Last Updated by..
            folder.setModifiedDate(QsConstants.getCurrentUtcDate());
            folder.setModifiedBy(Integer.toString(userId));

            folderService.saveFolders(folder);

            response.put("code", 200);
            response.put("result", destUrl);
            response.put("message", "Data uploaded Completed!");

            log.info("Completed file upload request on: {}", DateTime.now().toDate().toString());

            String auditMessage = controllerHelper.getAuditMessage(AuditEventType.INGEST, AuditEventTypeAction.CREATE);
            String notification = controllerHelper.getNotificationMessage(AuditEventType.INGEST, AuditEventTypeAction.CREATE);
            controllerHelper.recordAuditEvent(AuditEventType.INGEST, AuditEventTypeAction.CREATE,
                    String.format(auditMessage, file.getOriginalFilename()),
                    String.format(notification, file.getOriginalFilename(), userName),
                    userObj.getUserId(), project.getProjectId());

            return ResponseEntity.ok().body(response);
        } catch (final AmazonServiceException e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception {}", e.getErrorMessage());
            return ResponseEntity.status(500).body(response);

        } catch (final SQLException e) {
            response.put("code", 422);
            response.put("message", e.getMessage());
            log.error("Exception - partition {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } catch (final Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception Occurred {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }

            if (csvFileTmp != null && csvFileTmp.exists()) {
                csvFileTmp.delete();
            }

            if (csvFile != null && csvFile.exists()) {
                csvFile.delete();
            }
            if (cfile != null && cfile.exists()) {
                cfile.delete();
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @ApiOperation(value = "File Upload using API data", response = Json.class)
    @PostMapping(value = "/uploadapidataasfile")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Uploaded the data received from API Endpoint!"),
                    @ApiResponse(code = 400, message = "Malformed Content!")
            })
    public ResponseEntity<Object> uploadApiDataAsFile(@RequestBody ApiIntegrationRequest apiIntgRequest) {
        log.info("Received projectId - {}, folderId {} and REST End Point: {}", apiIntgRequest.getProjectId(), apiIntgRequest.getFolderId(), apiIntgRequest.getApiEndPoint());
        final Map<String, Object> response = new HashMap<>();
        try {
            dbUtil.changeSchema("public");
            final Projects project = projectService.getProject(apiIntgRequest.getProjectId());
            final QsUserV2 userById = userService.getUserById(project.getUserId());
            dbUtil.changeSchema(project.getDbSchemaName());

            final QsFolders folder = folderService.getFolder(apiIntgRequest.getFolderId());
            String partitionRef = qsUtil.randomAlphaNumeric();
            partitionRef = partitionRef.toLowerCase();

            String userName =
                    String.format(
                            "%s %s",
                            userById.getQsUserProfile().getUserFirstName(),
                            userById.getQsUserProfile().getUserLastName());

            ResponseEntity<String> singleResponse = null;
            ResponseEntity<String> pagingResponse = null;
            String resolvedUri = apiIntgUtils.resolveHubSpotApiParameters(apiIntgRequest);
            URI url = new URI(resolvedUri);
            log.info("Invoking api : " + url);
            singleResponse = restTemplate.getForEntity(url, String.class);
            HttpStatus statusCode = singleResponse.getStatusCode();
            if (statusCode.is2xxSuccessful()) {
                log.info("REST API call succeeded, processing the data now!!!");
                String responseBody = singleResponse.getBody();
                if (apiIntgUtils.hasNext(responseBody)) { // Limit is 100 per request. Hence invoke for next 100
                    JsonNode paging = apiIntgUtils.getHubspotEntityList(responseBody, "paging");
                    String nextPageUrl = paging.at("/next/link").asText();
                    String resolvedUrl = nextPageUrl.concat(String.format("&hapikey=%s", apiIntgRequest.getApiKey()));
                    pagingResponse = restTemplate.getForEntity(resolvedUrl, String.class);
                    if (!pagingResponse.getStatusCode().is2xxSuccessful()) {
                        pagingResponse = null;
                    }
                }
                // Get the CSV file based on the API response...
                File csvFile;
                String csvFileName = String.format("%s.csv", apiIntgRequest.getEntity());
                if (pagingResponse == null) {
                    csvFile = apiIntgUtils.processHubSpotApiResponse(csvFileName, responseBody);
                } else {
                    csvFile = apiIntgUtils.processHubSpotApiResponse(csvFileName, responseBody, pagingResponse.getBody());
                }


                // Validate the file before upload:
                Map<String, String> validationErrorMsgMap = qsUtil.validateFileBeforeUpload(csvFile, folder, project,
                        userById, "uploadapidataasfile", ".csv");
                if (validationErrorMsgMap != null && !validationErrorMsgMap.isEmpty()) {
                    response.put("code", 400);
                    response.put("message", validationErrorMsgMap);
                    log.error("Validation error before uploading the file. Detailed message is: {}", validationErrorMsgMap.toString());

                    return ResponseEntity.status(400).body(response);
                }

                log.info("CSV file is generated and its location is: {}", csvFile.getAbsolutePath());

                final String pathSep = "/";
                final String qsPartition = folder.getFolderName() + pathSep + partitionRef;
                final String fileName = qsPartition + pathSep + csvFile.getName();

                final S3FileUploadResponse s3FileUploadRes = awsAdapter.storeObjectInS3Async(project.getBucketName() + "-raw", csvFile,
                        partitionRef, project.getRawDb(),
                        folder.getFolderName(), fileName, "text/csv;charset=utf-8;");

                URL destUrl = awsAdapter.updateFileMdAfterUpload(s3FileUploadRes, project, folder, userName, partitionRef, project.getUserId(), null);

                response.put("code", 200);
                response.put("result", destUrl);
                response.put("message", "Data uploaded Completed!");
            } else {
                log.info("REST API call failed, no further processing!!!");

                if (statusCode.is4xxClientError()) {
                    response.put("code", 400);
                    response.put("result", "");
                    response.put("message", "Failed to fetch data from the REST Endpoint.");
                } else if (statusCode.is5xxServerError()) {
                    response.put("code", 500);
                    response.put("result", "");
                    response.put("message", "Failed to fetch data from the REST Endpoint. Internal server error.");
                }
            }

            return ResponseEntity.ok().body(response);
        } catch (final AmazonServiceException e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception {}", e.getErrorMessage());
            return ResponseEntity.status(500).body(response);

        } catch (final SQLException e) {
            response.put("code", 422);
            response.put("message", e.getMessage());
            log.error("Exception - partition {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } catch (final Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception Occurred {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }

    @ApiOperation(value = "Get EMR Metrics", response = Json.class)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Successfully returned EMR metrics"),
                    @ApiResponse(code = 400, message = "Failed to fetch the EMR metrics from AWS CloudWatch")
            })
    @GetMapping(value = "/ops/fetchemrmetrics/{userId}")
    public ResponseEntity<Object> getEmrMetricsFromAwsCloudWatch(
            @PathVariable(value = "userId") final int userId) {
        Map<String, Object> response = new HashMap<>();
        try {

            String metricsResponse = awsAdapter.getEmrMetrics();
            if (metricsResponse != null) {
                response.put("code", 200);
                response.put("result", metricsResponse);
                response.put("message", "EMR metrics fetched successfully from AWS CloudWatch.");
            } else {
                response.put("code", 200);
                response.put("result", null);
                response.put("message", "No EMR metrics found.");
            }
        } catch (final Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Error {} and {}", e.getCause(), e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @ApiOperation(value = "Check column datatype casting ", response = Json.class)
    @PostMapping(
            value = "/validatedatatype")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Checked column datatype casting successfully."),
                    @ApiResponse(code = 400, message = "Failed to check the column datatype casting.")
            })
    public ResponseEntity<Object> checkColDatatypeCasting(@RequestBody FileColumnValidationRequest fileColValReq) {
        final Map<String, Object> response = new HashMap<>();
        try {

            log.info("Received file upload request on: {}", QsConstants.getCurrentUtcDate());

            dbUtil.changeSchema("public");
            final Projects project = projectService.getProject(fileColValReq.getProjectId(), fileColValReq.getUserId());
            if (project == null) {
                response.put("code", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Requested project with Id: " + fileColValReq.getProjectId() + " for User with Id: " + fileColValReq.getUserId() + " not found.");

                return ResponseEntity.ok().body(response);
            }

            final QsUserV2 userObj = userService.getActiveUserById(fileColValReq.getUserId());
            dbUtil.changeSchema(project.getDbSchemaName());

            QsFolders folder = folderService.getActiveFolder(fileColValReq.getFolderId(), fileColValReq.getProjectId(), fileColValReq.getUserId());
            if (folder != null) {

                Optional<QsFiles> fileOp = fileService.getFileIdWithFolderId(fileColValReq.getFileId(), fileColValReq.getFolderId());
                if (fileOp.isPresent()) {
                    boolean allowed = awsAdapter.checkColDatatypeCasting(project.getRawDb(), folder.getFolderName(),
                            fileColValReq.getColumnName(), fileColValReq.getTargetDataType());
                    if (allowed) {
                        response.put("code", HttpStatus.OK.value());
                        response.put("message", "Column: '" + fileColValReq.getColumnName() + "' can be typecasted to the Target Datatype: '" + fileColValReq.getTargetDataType() + "'");
                        response.put("result", "True");
                    } else {
                        response.put("code", HttpStatus.BAD_REQUEST.value());
                        response.put("message", "Column: '" + fileColValReq.getColumnName() + "' cannot be typecasted to the Target Datatype: '" + fileColValReq.getTargetDataType() + "'");
                        response.put("result", "False");
                    }

                } else {
                    response.put("code", HttpStatus.BAD_REQUEST.value());
                    response.put("message", "File with Id: " + fileColValReq.getFileId() + " not found.");
                }
            } else {
                response.put("code", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Folder with Id: " + fileColValReq.getFolderId() + " for User with Id: " + fileColValReq.getUserId() + " not found.");
            }

            return ResponseEntity.ok().body(response);
        } catch (final SQLException e) {
            response.put("code", 422);
            response.put("message", e.getMessage());
            log.error("Exception - partition {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        } catch (final Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            log.error("Exception Occurred {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }
}
