/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.adapter.AwsResourceHandler;
import ai.quantumics.api.constants.AuditEventType;
import ai.quantumics.api.constants.AuditEventTypeAction;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.*;
import ai.quantumics.api.req.CreateProjectRequest;
import ai.quantumics.api.req.UpdateProjectRequest;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.MapperUtil;
import ai.quantumics.api.util.RegexPatterns;
import ai.quantumics.api.vo.ProjectResponse;
import com.amazonaws.AmazonServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.validation.Valid;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.annotation.PostConstruct;

import static ai.quantumics.api.constants.QsConstants.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@Api(value = "QuantumSpark Service API ")
public class ProjectController {

    private final MapperUtil mapper;
    private final ControllerHelper helper;
    private final ProjectService projectService;
    private final UserServiceV2 userServiceV2;
    private final UserProjectsService userProjectsService;
    private final AwsResourceHandler awsResourceHandler;
    private final DatabaseSessionManager sessionManager;
    private FileService fileService;
    private FolderService folderService;
    private AwsAdapter awsAdapter;
    private SubscriptionService subscriptionService;

    @Value("${qs.param.org.name}")
    private String orgNameProp;

    @Value("${s3.images.project.bucketName}")
    private String imagesBucketName;

    @Value("${qs.project.restoration.threshold}")
    private String restorationThreshold;

    @Value("${qs.project.name}")
    private String configuredProjectName;

    public ProjectController(MapperUtil mapperCi, final ControllerHelper helperCi,
                             final ProjectService projectServiceCi, final UserServiceV2 userServiceV2Ci,
                             final UserProjectsService userProjectsServiceCi, final AwsResourceHandler awsResourceHandlerCi,
                             final DatabaseSessionManager sessionManagerCi,
                             FileService fileServiceCi, FolderService folderServiceCi,
                             AwsAdapter awsAdapterCi, SubscriptionService subscriptionServiceCi) {
        mapper = mapperCi;
        helper = helperCi;
        projectService = projectServiceCi;
        userServiceV2 = userServiceV2Ci;
        userProjectsService = userProjectsServiceCi;
        awsResourceHandler = awsResourceHandlerCi;
        sessionManager = sessionManagerCi;
        fileService = fileServiceCi;
        folderService = folderServiceCi;
        awsAdapter = awsAdapterCi;
        subscriptionService = subscriptionServiceCi;
    }

    public static String getNormalizedDbSchemaName(String projectName) {
        return (projectName != null) ? "qs_" + projectName.replaceAll("\\W", "_").toLowerCase() : "";
    }

    @GetMapping("/getLogs")
    public String getlogs() {
        log.debug("Debug logs enabled!");
        log.info("Info logs enabled!");
        log.error("Error logs enabled!");
        log.trace("Trace logs enabled");
        log.warn("Warning logs enabled!");
        return "Success!";
    }

    /*@ApiOperation(value = "projects", response = Json.class, notes = "Create a project by sending request json in body")
    @PostMapping
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Project created successfully"),
            @ApiResponse(code = 400, message = "Error creating the project"),
            @ApiResponse(code = 409, message = "Data May already existed")})*/
    //@PostConstruct
    public ResponseEntity<Object> createProject() {
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setProjectName(configuredProjectName);
        projectRequest.setUserId(1);
        projectRequest.setOrgName("orgName");
        final Map<String, Object> createResponse = new HashMap<>();
        try {
            helper.swtichToPublicSchema();
            log.info("Project creation started");
            final int userId = projectRequest.getUserId();
            final QsUserV2 userObj = userServiceV2.getUserById(userId);
            final String projectNameMod = StringUtils.trimAllWhitespace(projectRequest.getProjectName())
                    .replaceAll("_", "-").replaceAll(RegexPatterns.SPECIAL_CHARS.getPattern(), "-").replaceAll("[-]+", "-");
            List<Projects> projectList = projectService.getProjectsForUser(userId);
            if (projectList != null && projectList.size() > 0) {
                log.info("Project already exist");
                createResponse.put("code", HttpStatus.SC_CONFLICT);
                createResponse.put("message", "Project already exists.");
                createResponse.put("result", "");
                createResponse.put("status", "");
                return ResponseEntity.status((Integer) createResponse.get("code")).body(createResponse);
            }
            //if(activeProjects != null && activeProjects.size() == 0) {
                Projects project = initializeProjectDetails(projectRequest, userObj);
                final boolean isProjectCreated = awsResourceHandler.createProjectSequence(project);
                if (isProjectCreated) {
                    project = projectService.saveProject(project);

                    // Now create a schema based on the Project Name...
                    EntityManager entityManager = null;
                    Object resultObj = null;
                    try {
                        entityManager = sessionManager.getEntityManager();
                        entityManager.setFlushMode(FlushModeType.COMMIT);

                        entityManager.getTransaction().begin();
                        resultObj = entityManager
                                .createNativeQuery("call create_project_schema_and_tables(:schemaName, :result)")
                                .setParameter("schemaName", project.getDbSchemaName()).setParameter("result", 0)
                                .getSingleResult();

                        entityManager.flush();
                        entityManager.getTransaction().commit();
                    } catch (Exception e) {
                        log.info("Exception occurred while creating the Project specific private schema: {}", e.getCause());

                        entityManager.flush();
                        if (entityManager.getTransaction() != null && entityManager.getTransaction().isActive()) {
                            entityManager.getTransaction().rollback();
                        }
                    } finally {
                        if (entityManager != null) {
                            entityManager.close();
                        }
                    }

                    Integer finalResult = null;
                    if (resultObj != null && (resultObj instanceof Integer)) {
                        finalResult = (Integer) resultObj;

                        if (finalResult == 0) {
                            log.info("Created a new Project specific schema by name: {}", project.getDbSchemaName());
                        } else {
                            log.info("Failed to create Project specific schema: {}", project.getDbSchemaName());
                            // Need to rollback the actions performed so far in S3 and Athena.
                        }
                    }

                    // Add a record in the mapping table as well..
                    QsUserProjects userProject = new QsUserProjects();
                    userProject.setUserId(projectRequest.getUserId());
                    userProject.setProjectId(project.getProjectId());

                    DateTime now = DateTime.now();
                    userProject.setCreationDate(now.toDate());
                    String userName = userObj.getQsUserProfile().getUserFirstName() + " "
                            + userObj.getQsUserProfile().getUserLastName();
                    userProject.setCreatedBy(userName);
                    userProject = userProjectsService.save(userProject);

                    createTrialSubscription(userObj, project);

                    helper.recordAuditEvent(AuditEventType.PROJECT, AuditEventTypeAction.CREATE,
                            String.format(helper.getAuditMessage(AuditEventType.PROJECT, AuditEventTypeAction.CREATE),
                                    project.getProjectDisplayName()),
                            null, userId, project.getProjectId());

                    createResponse.put("code", HttpStatus.SC_OK); // TODO 201
                    createResponse.put("message", "Project created successfully");
                    createResponse.put("result", project);
                    createResponse.put("status", new ProjectStatus());

                } else {
                    createResponse.put("code", HttpStatus.SC_OK);
                    createResponse.put("message", "Project Creation Failed");
                    createResponse.put("result", "");
                    createResponse.put("status", "");
                }
            //}
        } catch (final ClassCastException e) {
            createResponse.put("code", HttpStatus.SC_BAD_REQUEST);
            createResponse.put("message", "Bad request, please check request body !");
            log.error("Exception creating the project :: {}", e.getMessage());
        } catch (final AmazonServiceException e) {
            createResponse.put("code", HttpStatus.SC_CONFLICT);
            createResponse.put("message", "Project may be already existing, or " + e.getMessage());
            log.error("Exception creating the project :: {}", e.getMessage());
        } catch (final SQLException e) {
            createResponse.put("code", HttpStatus.SC_FAILED_DEPENDENCY);
            createResponse.put("message", "Project creation failed: " + e.getMessage());
            log.error("Exception creating the project :: {}", e.getMessage());
        }

        return ResponseEntity.status((Integer) createResponse.get("code")).body(createResponse);
    }

    /**
     * @param userObj
     * @throws SQLException
     * @throws StripeException
     */
    private void createTrialSubscription(final QsUserV2 userObj, final Projects project)
            throws SQLException {

        ProjectSubscriptionDetails projectSubscriptionDetails = new ProjectSubscriptionDetails();
        projectSubscriptionDetails.setUserId(project.getUserId());
        projectSubscriptionDetails.setProjectId(project.getProjectId());
        projectSubscriptionDetails.setSubscriptionId(UUID.randomUUID().toString());
        // Stripe status is 'trialing' for Trial subscription. But set as active in
        // QSAI.
        projectSubscriptionDetails.setSubscriptionStatus("active");
        projectSubscriptionDetails.setInvoiceId(null);
        Date from = Calendar.getInstance(TimeZone.getDefault()).getTime();
        projectSubscriptionDetails
                .setSubscriptionValidFrom(DateTime.now().toDate());
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.YEAR,10);
        Date validTo = calendar.getTime();
        projectSubscriptionDetails.setSubscriptionValidTo(DateTime.now().plusYears(10).toDate());
        QsSubscription qsubscription = subscriptionService.getSubscriptionByPlanTypeId("price_1IlIB1CXjfdxZzBVsDPYAgZv");
        projectSubscriptionDetails.setSubscriptionType(qsubscription.getName());
        projectSubscriptionDetails.setSubscriptionPlanTypeId(qsubscription.getSubscriptionId());
        projectSubscriptionDetails.setSubscriptionPlanType(qsubscription.getPlanType());

        projectSubscriptionDetails.setChargeStatus("free");
        projectSubscriptionDetails.setCreationDate(DateTime.now().toDate());
        projectSubscriptionDetails.setActive(true);
        projectSubscriptionDetails.setSubTotal(Double.valueOf(0));
        projectSubscriptionDetails.setTax(Double.valueOf(0));
        projectSubscriptionDetails.setTotal(Double.valueOf(0));

        subscriptionService.saveProjectSubscriptionDetails(projectSubscriptionDetails);
        log.info(String.format("Created Trial subscription for the project '%1$s'",
                project.getProjectDisplayName()));

    }


    @ApiOperation(value = "Delete a project", response = Json.class, notes = "Delete a project")
    @DeleteMapping("/{projectId}/{userId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Project deleted successfully"),
            @ApiResponse(code = 404, message = "Project not found")})
    public ResponseEntity<Object> deleteProject(@PathVariable(value = "projectId") final int projectId,
                                                @PathVariable(value = "userId") final int userId) {
        final Map<String, Object> genericResponse = new HashMap<>();
        Projects projects = helper.getProjects(projectId);
        // Projects project = projectService.getProject(projectId);
        // Delete all Aws resources associated with it
        // 1. Delete Crawlers
        // 2. Delete Buckets
        // 3. Databases for crawlers
        // 4. Delete all the folders in S3 and Postgres
        // 5. Delete all the files in S3 and Postgres
        try {
            helper.getProjectAndChangeSchema(projects.getProjectId());
            List<QsFolders> folders = folderService.getFolders(projectId);
            List<Integer> folderIds = new ArrayList<>();
            folders.forEach(folder -> {
                folder.setActive(false);
                folderIds.add(folder.getFolderId());
            });
            folderService.saveFolderCollection(folders);

            List<QsFiles> filesForFolderIds = fileService.getFilesForFolderIds(projectId, folderIds);
            filesForFolderIds.forEach(file -> file.setActive(false));
            fileService.saveFilesCollection(filesForFolderIds);

            DateTime now = DateTime.now();
            log.info("Files information fetched from service {}", filesForFolderIds.size());
            helper.swtichToPublicSchema();
            projects.setActive(false);
            projects.setModifiedBy(projects.getCreatedBy()); // TODO: We need to update this with the Logged in User.
            projects.setModifiedDate(now.toDate());
            projects = projectService.saveProject(projects);

            helper.recordAuditEvent(AuditEventType.PROJECT, AuditEventTypeAction.DELETE,
                    String.format(helper.getAuditMessage(AuditEventType.PROJECT, AuditEventTypeAction.DELETE),
                            projects.getProjectDisplayName()),
                    helper.getNotificationMessage(AuditEventType.PROJECT, AuditEventTypeAction.DELETE), userId,
                    projects.getProjectId());

            genericResponse.put("code", HttpStatus.SC_OK);
            genericResponse.put("message", String.format("All Project ' %s ' Folders/Files are marked Delete ",
                    projects.getProjectDisplayName()));
        } catch (SQLException exception) {
            genericResponse.put("code", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            genericResponse.put("message", "Error while Deleting the project " + exception.getMessage());
        }

        return ResponseEntity.status(HttpStatus.SC_OK).body(genericResponse);
    }

    @ApiOperation(value = "projects", response = Json.class)
    @GetMapping("/users/{userId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List All Project for QsUser ID"),
            @ApiResponse(code = 400, message = "No projects found for user")})
    public ResponseEntity<Object> getProjectsOfUser(@PathVariable(value = "userId") final int userId) {
        Map<String, Object> projectResponse;

        try {
            helper.swtichToPublicSchema();
            QsUserV2 user = userServiceV2.getActiveUserById(userId);
            if (user == null) {
                projectResponse = helper.createSuccessMessage(HttpStatus.SC_BAD_REQUEST,
                        "User with Id: " + userId + " not found.", null);

                return ResponseEntity.badRequest().body(projectResponse);
            }
            List<Projects> activeProjects = null;

            if (QsConstants.ADMIN.equals(user.getUserRole())) {
                activeProjects = projectService.getProjectsForUser(userId);
            } else {
                List<QsUserProjects> subUserProjects = userProjectsService.getProjectUsersByUserId(userId);
                if (subUserProjects != null && !subUserProjects.isEmpty()) {
                    List<Projects> subUserActiveProjects = new ArrayList<>();
                    subUserProjects.stream().forEach((subUserProj) -> {
                        subUserActiveProjects.add(projectService.getProject(subUserProj.getProjectId()));
                    });

                    activeProjects = new ArrayList<>();
                    activeProjects.addAll(subUserActiveProjects);
                }
            }

            List<ProjectResponse> projectResponses = new ArrayList<>();
            if (activeProjects != null && !activeProjects.isEmpty()) {
                for (Projects project : activeProjects) {
                    List<String> userInfos = new ArrayList<>();
                    userInfos.add(user.getUserEmail());
                    List<QsUserProjects> userProjects = userProjectsService
                            .getProjectUsersByProjectId(project.getProjectId());
                    if (userProjects != null && !userProjects.isEmpty()) {
                        userProjects.stream().forEach((userProj) -> {
                            try {
                                QsUserV2 userObj = userServiceV2.getUserById(userProj.getUserId());
                                if (userObj.isActive() && !QsConstants.ADMIN.equals(userObj.getUserRole())) {
                                    userInfos.add(userObj.getUserEmail());
                                }
                            } catch (SQLException e) {
                                log.error("Error while fetching project users:" + e.getMessage());
                            }
                        });
                    }

                    // Fetch subscription details

                    ProjectSubscriptionDetails projectSubscriptionDetails = subscriptionService
                            .getProjectSubscriptionByStatus(project.getProjectId(), project.getUserId(), "active");

                    ProjectResponse projResp = new ProjectResponse();
                    projResp.setProjectId(project.getProjectId());
                    projResp.setCreatedBy(project.getCreatedBy());
                    projResp.setCreatedDate(project.getCreatedDate());
                    projResp.setProjectName(project.getProjectName());
                    projResp.setProjectDesc(project.getProjectDesc());
                    projResp.setProjectDisplayName(project.getProjectDisplayName());
                    projResp.setActive(project.isActive());
                    projResp.setMarkAsDefault(project.isMarkAsDefault());
                    projResp.setDeleted(project.isDeleted());
                    // set project members
                    //Collections.sort(userInfos, String.CASE_INSENSITIVE_ORDER);
                    projResp.setProjectMembers(userInfos);
                    projResp.setProjectLogo(project.getProjectLogo());
                    // set project subscription details
                    if (projectSubscriptionDetails != null) {
                        projResp.setSubscriptionType(projectSubscriptionDetails.getSubscriptionType());
                        projResp.setSubscriptionPlanType(projectSubscriptionDetails.getSubscriptionPlanType());
                        projResp.setSubscriptionPlanTypeId(projectSubscriptionDetails.getSubscriptionPlanTypeId());
                        projResp.setSubscriptionStatus(projectSubscriptionDetails.getSubscriptionStatus());
                        projResp.setValidDays(
                                calculateNumberOfDays(projectSubscriptionDetails.getSubscriptionValidTo()));
                    }
                    projectResponses.add(projResp);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("User {} & Number of projects {} ", userId, activeProjects.size());
            }

            projectResponse = helper.createSuccessMessage(HttpStatus.SC_OK, "Projects for user", projectResponses);
        } catch (final SQLException ex) {
            projectResponse = helper.failureMessage(HttpStatus.SC_NOT_FOUND, ex.getMessage());
        }
        return ResponseEntity.ok().body(projectResponse);
    }

    /**
     * @param user
     * @return
     */
    @SuppressWarnings("unused")
    private int calculateValidity(QsUserV2 user) {
        Date subValidTo = user.getUserSubscriptionValidTo();
        int validDays = calculateNumberOfDays(subValidTo);
        return validDays;
    }

    /**
     * @param subValidTo
     * @return
     */
    private int calculateNumberOfDays(Date subValidTo) {
        String str = subValidTo.toString();
        str = str.substring(0, str.indexOf(" "));
        LocalDate dt1 = DateTime.now().toLocalDate();
        DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate dt2 = DateTime.parse(str, df).toLocalDate();
        int validDays = Days.daysBetween(dt1, dt2).getDays();
        return validDays;
    }

    @ApiOperation(value = "projects", response = Json.class)
    @GetMapping("/{projectId}/{userId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get a project for the given Project Id."),
            @ApiResponse(code = 400, message = "Project not found.")})
    public ResponseEntity<Object> getProject(@PathVariable(value = "projectId") final int projectId,
                                             @PathVariable(value = "userId") final int userId) {
        Map<String, Object> projectResponse;
        try {
            helper.swtichToPublicSchema();
            final Projects project = projectService.getProject(projectId, userId);

            projectResponse = helper.createSuccessMessage(HttpStatus.SC_OK, "Project for given project id.", project);
        } catch (final Exception ex) {
            projectResponse = helper.failureMessage(HttpStatus.SC_NOT_FOUND, ex.getMessage());
        }
        return ResponseEntity.ok().body(projectResponse);
    }

    private Projects initializeProjectDetails(final CreateProjectRequest projectRequest, final QsUserV2 userObj)
            throws SQLException {
        final String orgName = orgNameProp; // projectRequest.getOrgName();
        final String projectNameMod = StringUtils.trimAllWhitespace(projectRequest.getProjectName())
                .replaceAll("_", "-").replaceAll(RegexPatterns.SPECIAL_CHARS.getPattern(), "-").replaceAll("[-]+", "-");
        final String dbNameMod = StringUtils.trimAllWhitespace(projectRequest.getProjectName())
                .replaceAll(RegexPatterns.SPECIAL_CHARS.getPattern(), "_").replaceAll("[_]+", "_");
        final int userId = userObj.getUserId();

        final String projectNameSmall = projectNameMod.toLowerCase();
        final String dbNameModSmall = dbNameMod.toLowerCase();
        final String bucketName = String.format("%s-%s", orgName, projectNameSmall);
        final String rawDb = String.format("%s_%s%s%d", orgName, dbNameModSmall, RAW_DB, userId);
        final String processedDb = String.format("%s_%s%s%d", orgName, dbNameModSmall, PROCESSED_DB, userId);

        final String engDb = String.format("%s_%s%s%d", orgName, dbNameModSmall, ENG_DB, userId);

        final String outCome = "futureUse";

        final Projects project = new Projects();
        project.setProjectDesc(projectRequest.getProjectDesc());
        project.setProjectAutomate("notInUse");
        project.setProjectDataset("notInUse");
        project.setProjectEngineer("notInUse");
        project.setProjectName(projectNameMod);
        project.setUserId(userId);
        project.setOrgName(orgName);
        project.setBucketName(bucketName);
        project.setRawDb(rawDb);
        project.setProcessedDb(processedDb);
        project.setProjectOutcome(outCome);
        project.setDbSchemaName(getNormalizedDbSchemaName(projectNameMod));
        project.setProjectDisplayName(projectRequest.getProjectName());
        DateTime now = DateTime.now();

        project.setCreatedBy(
                userObj.getQsUserProfile().getUserFirstName() + " " + userObj.getQsUserProfile().getUserLastName());
        project.setCreatedDate(now.toDate());
        project.setActive(true);
        project.setEngDb(engDb);
        project.setMarkAsDefault(false);
        return project;
    }

    @SuppressWarnings("unused")
    private String joinListWithComma(final ArrayList<String> tempData) {
        return String.join(",", tempData);
    }

    @ApiOperation(value = "update a project", response = Json.class, notes = "update a project")
    @PutMapping("/updateproject/{projectId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Project updated successfully"),
            @ApiResponse(code = 422, message = "Error updating project")})
    public ResponseEntity<Object> updateProject(@RequestBody final UpdateProjectRequest updateProject,
                                                @PathVariable(value = "projectId") final int projectId) {
        Map<String, Object> updateResponse = new HashMap<>();
        try {
            helper.swtichToPublicSchema();
            final Projects project = projectService.getProject(projectId, updateProject.getUserId());
            if (project == null) {
                updateResponse.put("code", HttpStatus.SC_BAD_REQUEST);
                updateResponse.put("message", "Requested project with Id: " + projectId + " for User with Id: "
                        + updateProject.getUserId() + " not found.");

                return ResponseEntity.ok().body(updateResponse);
            }

            final QsUserV2 user = userServiceV2.getUserById(updateProject.getUserId());

            final String userName = user.getQsUserProfile().getUserFirstName() + " "
                    + user.getQsUserProfile().getUserLastName();
            final String userEmail = user.getUserEmail();
            final DateTime now = DateTime.now();

            project.setProjectDisplayName(updateProject.getDisplayName());
            project.setProjectDesc(updateProject.getDescription());
            project.setModifiedBy(userName);
            project.setModifiedDate(now.toDate());

            if (updateProject.isMarkAsDefault()) {
                Projects existingDefaultProject = projectService.getDefaultProjectForUser(updateProject.getUserId());
                if (existingDefaultProject != null) {
                    existingDefaultProject.setMarkAsDefault(false);
                    projectService.saveProject(existingDefaultProject);
                }
                project.setMarkAsDefault(true);
            } else {
                project.setMarkAsDefault(false);
            }

            final Projects updatedProject = projectService.saveProject(project);

            // Send email notification about Project Update Action..
            /*Optional<EmailTemplate> emailTemplateOp = emailTemplateService.getEmailTemplate(QsConstants.UPDATE_PROJECT);

            if (emailTemplateOp.isPresent()) {
                EmailTemplate emailTemplate = emailTemplateOp.get();
                String emailBodyTempl = emailTemplate.getBody();
                log.info("Update Project Email Template Body is: \n{}", emailBodyTempl);
                String emailBody = String.format(emailBodyTempl, project.getProjectDisplayName(), userName, userEmail,
                        now.toDate().toString());
                log.info("Update Project Email Body is: \n{}", emailBody);

                log.info("Successfully sent 'Project Update Notification' to the User '{}'.", userEmail);
            } else {
                log.error(
                        "Failed to send 'Project Update Notification' to User as the Email Template with Action '{}' is not present in the database public.QS_EMAIL_TEMPLATES table.",
                        QsConstants.UPDATE_PROJECT);
            }*/

            String auditMessage = helper.getAuditMessage(AuditEventType.PROJECT, AuditEventTypeAction.UPDATE);
            helper.recordAuditEvent(AuditEventType.PROJECT, AuditEventTypeAction.UPDATE,
                    String.format(auditMessage, project.getProjectDisplayName()), null, user.getUserId(),
                    project.getProjectId());

            log.info("project updated with request data ! {}", updatedProject);
            updateResponse = helper.createSuccessMessage(HttpStatus.SC_OK, "Project updated successfully",
                    updatedProject);
        } catch (final Exception exc) {
            updateResponse = helper.failureMessage(HttpStatus.SC_UNPROCESSABLE_ENTITY, exc.getMessage());
            log.error("ALERT - updating project failed {}", exc.getMessage());
        }
        return ResponseEntity.ok().body(updateResponse);
    }

    @ApiOperation(value = "Deactivate Project", response = Json.class, notes = "deactivates a project")
    @PutMapping("/deactivate/{projectId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Deactivation is successful"),
            @ApiResponse(code = 500, message = "Error while deactivating the project")})
    public ResponseEntity<Object> deactivateProject(@PathVariable(value = "projectId") final int projectId) {
        Map<String, Object> response = null;
        try {
            helper.swtichToPublicSchema();
            final Projects project = projectService.getProject(projectId);
            project.setDeleted(true);
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime deletionDateTime = currentDateTime.plusDays(Integer.valueOf(restorationThreshold));
            project.setDeletionDate(deletionDateTime.toDate());
            project.setModifiedBy(project.getCreatedBy()); // TODO: We need to update this with the Logged in User.
            project.setModifiedDate(currentDateTime.toDate());
            projectService.saveProject(project);
            response = helper.createSuccessMessage(HttpStatus.SC_OK, String.format(
                    "Project '%1$s' is deactivated successfully. Access to the folders/files will be permitted only for %2$s days",
                    project.getProjectDisplayName(), restorationThreshold), null);
        } catch (final Exception exception) {
            response = helper.failureMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    String.format("Error while deactivating the project : %1$s", exception.getMessage()));
        }

        return ResponseEntity.status((Integer) response.get("code")).body(response);
    }

    @ApiOperation(value = "Reinstate Project", response = Json.class, notes = "reinstates the project")
    @PutMapping("/reinstate/{projectId}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Deactivation is successful"),
            @ApiResponse(code = 500, message = "Error while deactivating the project")})
    public ResponseEntity<Object> reinstateProject(@PathVariable(value = "projectId") final int projectId) {
        Map<String, Object> response = null;
        try {
            helper.swtichToPublicSchema();
            final Projects project = projectService.getProject(projectId);
            if (isEligibleForReinstate(project.getDeletionDate())) {
                project.setDeleted(false);
                project.setDeletionDate(null);
                project.setModifiedBy(project.getCreatedBy()); // TODO: We need to update this with the Logged in User.
                project.setModifiedDate(LocalDateTime.now().toDate());
                projectService.saveProject(project);
                response = helper.createSuccessMessage(HttpStatus.SC_OK,
                        String.format("Project '%1$s' is reinstated successfully", project.getProjectDisplayName()),
                        null);
            } else {
                response = helper.createSuccessMessage(HttpStatus.SC_CONFLICT,
                        String.format("Project '%1$s' can not reinstated which is already been terminated",
                                project.getProjectDisplayName()),
                        null);
            }
        } catch (final Exception exception) {
            response = helper.failureMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    String.format("Error while deactivating the project : %1$s", exception.getMessage()));
        }

        return ResponseEntity.status((Integer) response.get("code")).body(response);
    }

    private boolean isEligibleForReinstate(Date deletionDate) {
        boolean eligible = false;
        eligible = !LocalDateTime.now().toDate().after(deletionDate);
        return eligible;
    }

    @ApiOperation(value = "Upload a project logo", response = Json.class, notes = "Upload a project logo")
    @PostMapping(value = "/uploadImage/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Project updated successfully"),
            @ApiResponse(code = 422, message = "Error updating project")})
    public ResponseEntity<Object> uploadProjectLogo(@PathVariable(value = "projectId") final int projectId,
                                                    @RequestPart(value = "file") final MultipartFile projectLogo) {
        Map<String, Object> uploadResponse = null;
        try {
            helper.swtichToPublicSchema();
            final Projects project = projectService.getProject(projectId);
            final DateTime now = DateTime.now();
            project.setModifiedDate(now.toDate());

            String prjLogo = "";
            if (projectLogo != null) {
                prjLogo = awsAdapter.storeImageInS3Async(imagesBucketName, projectLogo,
                        projectId + QsConstants.PATH_SEP + projectLogo.getOriginalFilename(),
                        projectLogo.getContentType());
            }

            project.setProjectLogo(prjLogo);

            final Projects updatedProject = projectService.saveProject(project);
            uploadResponse = helper.createSuccessMessage(HttpStatus.SC_OK, "Project logo uploaded successfully",
                    updatedProject);

        } catch (final Exception exc) {
            uploadResponse = helper.failureMessage(HttpStatus.SC_UNPROCESSABLE_ENTITY, exc.getMessage());
            log.error("ALERT - Uploading project logo failed {}", exc.getMessage());
        }
        return ResponseEntity.ok().body(uploadResponse);
    }

    @ApiOperation(value = "Cleanup the deactivated projects", response = Json.class, notes = "Cleanup the deactivated projects")
    @PostMapping("/cleanprojects")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Project(s) cleaned successfully"),
            @ApiResponse(code = 404, message = "Project not found")})
    public ResponseEntity<Object> deleteProjectsInternal() {
        final Map<String, Object> genericResponse = new HashMap<>();
        try {
            helper.swtichToPublicSchema();
            Instant now = Instant.now();

            List<Projects> deletedProjects = projectService.getDeletedProjects();
            log.info("Number of projects to be cleaned and deleted are: {}", deletedProjects.size());

            if (deletedProjects != null && !deletedProjects.isEmpty()) {

                awsAdapter.cleanProjectsInternal(deletedProjects);
                deletedProjects.stream().forEach((deletedProj) -> {

                    // Delete Files associated with these projects..
                    helper.getProjectAndChangeSchema(deletedProj.getProjectId());
                    log.info("Started deleting the files associated with these Project: {} and Project Id: {}",
                            deletedProj.getProjectDisplayName(), deletedProj.getProjectId());
                    List<QsFiles> filesToDelete = fileService.getFiles(deletedProj.getProjectId());
                    if (filesToDelete != null && !filesToDelete.isEmpty()) {
                        fileService.deleteFiles(filesToDelete);
                    }

                    log.info("Completed deleting the files associated with these project..");

                    // Delete Folders associated with these projects..
                    log.info("Started deleting the folders associated with these Project: {} and Project Id: {}",
                            deletedProj.getProjectDisplayName(), deletedProj.getProjectId());
                    List<QsFolders> folderToDelete = folderService.getFoldersToDelete(deletedProj.getProjectId());
                    if (folderToDelete != null && !folderToDelete.isEmpty()) {
                        folderService.deleteFolders(folderToDelete);
                    }
                    log.info("Completed deleting the folders associated with these Project: {} and Project Id: {}",
                            deletedProj.getProjectDisplayName(), deletedProj.getProjectId());

                    // Finally Delete the Project(s) itself..
                    helper.swtichToPublicSchema();
                    log.info("Started deleting the Project: {} and Project Id: {}", deletedProj.getProjectDisplayName(),
                            deletedProj.getProjectId());
                    projectService.deleteProjects(deletedProjects);
                    log.info("Completed deleting the project(s)..");
                });
            }

            log.info("Elapsed time in cleaning the deactivated projects is: {} msecs",
                    (Duration.between(now, Instant.now()).toMillis()));

            genericResponse.put("code", HttpStatus.SC_OK);
            genericResponse.put("message", "Cleaned up the projects..");
        } catch (Exception exception) {
            genericResponse.put("code", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            genericResponse.put("message", "Error while cleaning the project(s) " + exception.getMessage());
        }

        return ResponseEntity.status(HttpStatus.SC_OK).body(genericResponse);
    }

    @GetMapping("/datafix")
    public ResponseEntity<Object> dataFix() {
        List<ProjectResponse> projectResponses = new ArrayList<>();
        try {
            @SuppressWarnings("unused")
            List<Projects> projectsForUser = projectService.getProjectsForUser(149);
            String userName = helper.getUserName(149);
            log.info("Success {}", projectResponses.size());
            Projects project = projectService.getProject(3701);
            ProjectResponse projectResponse = mapper.mapObject(project, userName);
            projectResponses.add(projectResponse);
        } catch (SQLException exception) {
            log.error("ALERT - {} ", exception.getMessage());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(projectResponses);
    }
}
