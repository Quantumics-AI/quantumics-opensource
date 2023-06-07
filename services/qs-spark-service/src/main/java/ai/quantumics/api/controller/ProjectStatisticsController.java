/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import ai.quantumics.api.exceptions.QsRecordNotFoundException;
import ai.quantumics.api.exceptions.QuantumsparkUserNotFound;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.*;
import ai.quantumics.api.req.PreparedDataset;
import ai.quantumics.api.req.StatisticsResponse;
import ai.quantumics.api.req.UpdateKPIRequest;
import ai.quantumics.api.service.*;
import ai.quantumics.api.util.QsUtil;
import ai.quantumics.api.vo.DataVolumeOverview;
import ai.quantumics.api.vo.ProjectStatistics;
import ai.quantumics.api.vo.ProjectStatistics.ValueUnitPair;
import ai.quantumics.api.vo.StatisticsOverview;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.spring.web.json.Json;

import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Controller
public class ProjectStatisticsController {
	private final FileService fileService;
	private final ControllerHelper helper;
	private final FolderService folderService;
	private final EngFlowService engFlowService;
	private final RunJobService runJobService;
	private final EngFlowJobService engFlowJobService;
	private final UserProjectsService userProjectsService;
	private final FileMetaDataAwsService fileMetaDataAwsService;
	private final RedashDashboardService redashDashboardService;
	private final UserServiceV2 userServiceV2;
	private SubscriptionService subscriptionService;
	private final HomeKPIService homeKPIService;

	public ProjectStatisticsController(FolderService folderServiceCi, FileService fileServiceCi, ControllerHelper helperCi,
									   EngFlowService engFlowServiceCi, RunJobService runJobServiceCi, EngFlowJobService engFlowJobServiceCi,
									   FileMetaDataAwsService fileMetaDataAwsServiceCi, UserProjectsService userProjectsServiceCi,
									   RedashDashboardService redashDashboardServiceCi,
									   SubscriptionService subscriptionServiceCi, UserServiceV2 userServiceV2Ci, HomeKPIService homeKPIServiceCi) {
		folderService = folderServiceCi;
		fileService = fileServiceCi;
		helper = helperCi;
		engFlowService = engFlowServiceCi;
		runJobService = runJobServiceCi;
		engFlowJobService = engFlowJobServiceCi;
		fileMetaDataAwsService = fileMetaDataAwsServiceCi;
		userProjectsService = userProjectsServiceCi;
		subscriptionService = subscriptionServiceCi;
		redashDashboardService = redashDashboardServiceCi;
		userServiceV2 = userServiceV2Ci;
		homeKPIService = homeKPIServiceCi;
	}

	@ApiOperation(value = "Get project level Statistics", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Statistics rendered"),
			@ApiResponse(code = 404, message = "Statistics found!") })
	@GetMapping(value = "/api/v1/project-stats/{userId}/{projectId}")
	public ResponseEntity<Object> getStatistics(@PathVariable(value = "userId") final int userId,
												@PathVariable(value = "projectId") final int projectId) {
		HashMap<String, Object> message = new HashMap<>();
		try {
			helper.swtichToPublicSchema();
			final Projects project = helper.getProjects(projectId, userId);
			if (project == null) {
				message.put("code", HttpStatus.BAD_REQUEST);
				message.put("message",
						"Requested project with Id: " + projectId + " for User with Id: " + userId + " not found.");

				return ResponseEntity.ok().body(message);
			}

			List<QsFolders> folders = folderService.allFolders(userId, projectId);
			int flowJobs = 0; // Completed engFlowJobs
			int numberFiles = 0; // Total Files
			int cleanseFiles = 0; // cleanseFiles
			int dbConnections = 0;
			for (QsFolders folder : folders) {
				numberFiles += fileService.getFiles(projectId, folder.getFolderId()).size();
				cleanseFiles += runJobService.getSucceededJobsOnlyInFolder(userId, projectId, folder.getFolderId());
			}
			int cleanseJobs = runJobService.getSucceededJobsOnly(userId, projectId);
			List<EngFlow> flowNames = engFlowService.getFlowNames(projectId, userId);

			for (EngFlow flow : flowNames) {
				List<EngFlow> childFlows = engFlowService.getChildEngFlows(projectId, flow.getEngFlowId());
				if (childFlows != null && !childFlows.isEmpty()) {
					List<Integer> childFlowIds = new ArrayList<>();
					childFlows.stream().forEach((childFlow) -> childFlowIds.add(childFlow.getEngFlowId()));
					flowJobs += engFlowJobService.countOfJobs(childFlowIds);
				}
			}
			// Starting the Livy Sesssion
			helper.initializeLivySilently(project.getProjectId());

			StatisticsResponse response = new StatisticsResponse();
			response.setEngJobsCount(flowJobs);
			response.setFilesCount(numberFiles);
			response.setFolderCount(folders.size());
			response.setDataBaseCount(dbConnections);
			response.setCleansingJobsCount(cleanseJobs);
			response.setEngFlowsCount(flowNames.size());
			response.setCleansedFilesCount(cleanseFiles);
			message = helper.createSuccessMessage(HttpStatus.OK.value(), "project level statistics", response);
		} catch (SQLException exception) {
			log.error("Error -- {}", exception.getMessage());
			message = helper.failureMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}

	@ApiOperation(value = "Get project level Statistics - V2", response = Json.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found") })
	@GetMapping(value = "/api/v2/project-stats/{requestType}/{userId}/{projectId}")
	public ResponseEntity<Object> getStatistics(@PathVariable("requestType") final String requestType,
												@PathVariable(value = "userId") final int userId, @PathVariable(value = "projectId") final int projectId,
												@RequestParam(value = "kpi", required = false) String kpi) {
		log.info("ProjectStatistics : getStatistics : started");
		List<QsFiles> activeFiles = null;
		List<QsFiles> sourceDataset = null;
		List<PreparedDataset> preparedDataSet = null;
		List<EngFlow> dataPipes = null;
		Map<String, Object> response = null;
		List<String> kpisList = new ArrayList<>();
		long yesterdayPreparedDSCount = 0;
		String averagePreparedDSRange = null;
		long yesterdaySourceDSCount = 0;
		String averageSourceDSRange = null;
		double yesterdayStorageInMB = 0;
		String averageStorageRange = null;
		long yesterdayDataPipeCount = 0;
		String averageDataPipeRange = null;
		List<QsFolders> allFolders = null;
		List<QsFolders> folders = null;
		long yesterdayFolderCount = 0;
		String averageFolderRange = null;
		List<QsFolders> pipelines = null;
		long yesterdayPipelineCount = 0;
		String averagePipelineRange = null;
		List<QsRedashDashboard> qsRedashDashboards = null;
		long yesterdayDashboardCount = 0;
		String averageDashboardRange = null;
		if(StringUtils.isNotEmpty(kpi)) {
			kpisList = Arrays.asList(kpi.split(","));
		} else {
			kpisList = Arrays.asList("Dataset", "PreparedDS", "DataFlow", "Storage");
		}
		try {
			helper.swtichToPublicSchema();
			boolean isAdmin = helper.isAdmin(userId);
			QsUserV2 qsUserV2 = userServiceV2.getUserById(userId);
			Predicate<QsUserProjects> findByUserId = (userProject) -> helper.isActiveUser(userProject.getUserId());
			List<QsUserProjects> userProjects = userProjectsService.getProjectUsersByProjectId(projectId);
			userProjects = userProjects.stream()
					.filter(findByUserId)
					.collect(Collectors.toList());
			int numberOfUsers = isAdmin
					? userProjects.stream().filter(findByUserId)
					.collect(Collectors.toList()).size()
					: 1;
			double otherDataOpsPrice = 1000;
			int subscriptionId = subscriptionService.getProjectSubscriptionDetails(projectId)
					.getSubscriptionPlanTypeId();
			QsSubscription qsSubscription = subscriptionService.getSubscriptionById(subscriptionId);
			Projects projects = helper.getProjects(projectId);
			LocalDate projectStartDate = null;
			if(isAdmin) {
				projectStartDate = QsUtil.convertToLocalDate(projects.getCreatedDate());
			} else {
				Optional<QsUserProjects> qsUserProjectsOptional = userProjects.stream()
						.filter((userProject) -> userId == userProject.getUserId())
						.findFirst();
				if(qsUserProjectsOptional.isPresent()) {
					projectStartDate = QsUtil.convertToLocalDate(qsUserProjectsOptional.get().getCreationDate());
				} else {
					throw new QsRecordNotFoundException("The user is not active");
				}
			}
			LocalDate yesterday = LocalDate.now().minusDays(1);
			LocalDate threeMonth = LocalDate.now().minusDays(90);

			activeFiles = fileService.getAllActiveFiles(projectId);

			if(kpisList.contains("PreparedDS")) {
				if (isAdmin) {
					preparedDataSet = fileMetaDataAwsService.getFilesList(projectId);
				} else {
					preparedDataSet = fileMetaDataAwsService.getFilesList(projectId).stream()
							.filter((preparedDataset) -> userId == Integer.parseInt(preparedDataset.getCreatedBy().trim()))
							.sorted(Comparator.comparing(PreparedDataset::getCreatedDate).reversed())
							.collect(Collectors.toList());
				}

				yesterdayPreparedDSCount = preparedDataSet.stream().filter(preparedDataset -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(preparedDataset.getCreatedDate());
					return (creationDate.isEqual(yesterday));
				}).count();

				long threeMonthPreparedDSCount = preparedDataSet.stream().filter(preparedDataset -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(preparedDataset.getCreatedDate());
					return (creationDate.isEqual(threeMonth) || creationDate.isAfter(threeMonth));
				}).count();

				int averagePreparedDSCount = (int) (threeMonthPreparedDSCount / 90);
				averagePreparedDSRange = averagePreparedDSCount + " - " + (averagePreparedDSCount + 1);
			}

			if(kpisList.contains("Dataset") || kpisList.contains("Storage")) {
				sourceDataset = isAdmin ? activeFiles
						: activeFiles.stream().filter((qsfile) -> userId == qsfile.getUserId())
						.sorted(Comparator.comparing(QsFiles::getCreatedDate).reversed())
						.collect(Collectors.toList());

				List<QsFiles> yesterdaySourceDS = sourceDataset.stream().filter(qsFiles -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFiles.getCreatedDate());
					return (creationDate.isEqual(yesterday));
				}).collect(Collectors.toList());

				List<QsFiles> threeMonthsSourceDS = sourceDataset.stream().filter(qsFiles -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFiles.getCreatedDate());
					return (creationDate.isEqual(threeMonth) || creationDate.isAfter(threeMonth));
				}).collect(Collectors.toList());

				yesterdaySourceDSCount = yesterdaySourceDS.size();
				long threeMonthSourceDSCount = threeMonthsSourceDS.size();

				double yesterdayStorageInBytes = yesterdaySourceDS.stream().collect(Collectors.summingDouble((qsFile) -> Double.parseDouble(qsFile.getFileSize())));
				yesterdayStorageInMB = QsUtil.convertBytesToMB(yesterdayStorageInBytes);

				double threeMonthStorageInBytes = threeMonthsSourceDS.stream().collect(Collectors.summingDouble((qsFile) -> Double.parseDouble(qsFile.getFileSize())));
				double threeMonthStorageInMB = QsUtil.convertBytesToMB(threeMonthStorageInBytes);
				double averageStorage = threeMonthStorageInMB / 90;
				averageStorageRange = averageStorage + " - " + (averageStorage + 1);

				int averageSourceDSCount = (int) (threeMonthSourceDSCount / 90);
				averageSourceDSRange = averageSourceDSCount + " - " + (averageSourceDSCount + 1);
			}

			if(kpisList.contains("DataFlow")) {
				dataPipes = isAdmin ? engFlowService.getFlowNames(projectId).stream()
						.filter((engflowP) -> engflowP.isActive()).collect(Collectors.toList())
						: engFlowService.getFlowNames(projectId, userId);

				yesterdayDataPipeCount = dataPipes.stream().filter(engFlow -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(engFlow.getCreatedDate());
					return (creationDate.isEqual(yesterday));
				}).count();

				long threeMonthDataPipeCount = dataPipes.stream().filter(engFlow -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(engFlow.getCreatedDate());
					return (creationDate.isEqual(threeMonth) || creationDate.isAfter(threeMonth));
				}).count();

				int averageDataPipeCount = (int) (threeMonthDataPipeCount / 90);
				averageDataPipeRange = averageDataPipeCount + " - " + (averageDataPipeCount + 1);
			}

			if(kpisList.contains("Folder") || kpisList.contains("Pipeline")) {
				allFolders = isAdmin ? folderService.getFolders(projectId) : folderService.allFolders(userId, projectId);
			}
			if(kpisList.contains("Folder")) {
				folders = allFolders.stream().filter(qsFolders -> !qsFolders.isExternal()).collect(Collectors.toList());

				yesterdayFolderCount = folders.stream().filter(qsFolders -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
					return (creationDate.isEqual(yesterday));
				}).count();

				long threeMonthFolderCount = folders.stream().filter(qsFolders -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
					return (creationDate.isEqual(threeMonth) || creationDate.isAfter(threeMonth));
				}).count();

				int averageFolderCount = (int) (threeMonthFolderCount / 90);
				averageFolderRange = averageFolderCount + " - " + (averageFolderCount + 1);
			}

			if(kpisList.contains("Pipeline")) {
				pipelines = allFolders.stream().filter(qsFolders -> qsFolders.isExternal()).collect(Collectors.toList());

				yesterdayPipelineCount = pipelines.stream().filter(qsFolders -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
					return (creationDate.isEqual(yesterday));
				}).count();

				long threeMonthPipelineCount = pipelines.stream().filter(qsFolders -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
					return (creationDate.isEqual(threeMonth) || creationDate.isAfter(threeMonth));
				}).count();

				int averagePipelineCount = (int) (threeMonthPipelineCount / 90);
				averagePipelineRange = averagePipelineCount + " - " + (averagePipelineCount + 1);
			}

			if(kpisList.contains("Dashboard")) {
				if (isAdmin) {
					qsRedashDashboards = redashDashboardService.getAllDashboards();
				} else {
					qsRedashDashboards = new ArrayList<>();
					if(qsUserV2 != null && StringUtils.isNotEmpty(qsUserV2.getUserRedashKey())) {
						qsRedashDashboards = redashDashboardService.getAllDashboardsForUserRedashKey(qsUserV2.getUserRedashKey());
					}
				}

				yesterdayDashboardCount = qsRedashDashboards.stream().filter(qsRedashDashboard -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsRedashDashboard.getCreationDate());
					return (creationDate.isEqual(yesterday));
				}).count();

				long threeMonthDashboardCount = qsRedashDashboards.stream().filter(qsRedashDashboard -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsRedashDashboard.getCreationDate());
					return (creationDate.isEqual(threeMonth) || creationDate.isAfter(threeMonth));
				}).count();

				int averageDashboardCount = (int) (threeMonthDashboardCount / 90);
				averageDashboardRange = averageDashboardCount + " - " + (averageDashboardCount + 1);
			}

			ai.quantumics.api.vo.ProjectStatistics projectStatitcs = new ai.quantumics.api.vo.ProjectStatistics();
			projectStatitcs.setRequestType(requestType);

			if ("W".equalsIgnoreCase(requestType)) {
				if(kpisList.contains("Dataset")) {
					StatisticsOverview sourceDataSetOverview = buildSourceDSOverviewWeekly(sourceDataset, yesterdaySourceDSCount, averageSourceDSRange);
					projectStatitcs.setSourceDataset(sourceDataSetOverview);
				}
				if(kpisList.contains("PreparedDS")) {
					StatisticsOverview preparedDatasetOverview = buildPreparedDSOverviewWeekly(preparedDataSet, yesterdayPreparedDSCount, averagePreparedDSRange);
					projectStatitcs.setPreparedDataset(preparedDatasetOverview);
				}

				if(kpisList.contains("DataFlow")) {
					StatisticsOverview dataPipesOverview = buildDataPipeOverviewWeekly(dataPipes, yesterdayDataPipeCount, averageDataPipeRange);
					projectStatitcs.setDataPipes(dataPipesOverview);
				}

				if(kpisList.contains("Folder")) {
					StatisticsOverview folderOverview = buildFoldersOrPipelineOverviewWeekly(folders, yesterdayFolderCount, averageFolderRange);
					projectStatitcs.setFolders(folderOverview);
				}

				if(kpisList.contains("Pipeline")) {
					StatisticsOverview pipelineOverview = buildFoldersOrPipelineOverviewWeekly(pipelines, yesterdayPipelineCount, averagePipelineRange);
					projectStatitcs.setPipeline(pipelineOverview);
				}

				if(kpisList.contains("Dashboard")) {
					StatisticsOverview dashboardOverview = buildDashboardOverviewWeekly(qsRedashDashboards, yesterdayDashboardCount, averageDashboardRange);
					projectStatitcs.setDashboard(dashboardOverview);
				}

				if(kpisList.contains("Storage")) {
					DataVolumeOverview weeklyVolumeOverview = buildVolumeOverviewWeekly(sourceDataset, yesterdayStorageInMB, averageStorageRange);
					projectStatitcs.setDataVolume(weeklyVolumeOverview);
				}

			} else if ("M".equalsIgnoreCase(requestType)) {
				LocalDate projectStartDateLocal = projectStartDate;
				LocalDate startDate = LocalDate.now().withDayOfMonth(1);
				LocalDate endDate = LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1);

				if(kpisList.contains("Dataset")) {
					StatisticsOverview sourceDataSetOverview = buildSourceDSMonthly(sourceDataset, yesterdaySourceDSCount, averageSourceDSRange, startDate, endDate);
					projectStatitcs.setSourceDataset(sourceDataSetOverview);
				}

				if(kpisList.contains("PreparedDS")) {
					StatisticsOverview preparedDatasetOverview = buildPreparedDSMonthly(preparedDataSet, yesterdayPreparedDSCount, averagePreparedDSRange, startDate, endDate);
					projectStatitcs.setPreparedDataset(preparedDatasetOverview);
				}

				if(kpisList.contains("DataFlow")) {
					StatisticsOverview dataPipesOverview = buildDataPipeOverviewMonthly(dataPipes, yesterdayDataPipeCount, averageDataPipeRange, startDate, endDate);
					projectStatitcs.setDataPipes(dataPipesOverview);
				}

				if(kpisList.contains("Folder")) {
					StatisticsOverview folderOverview = buildFolderOrPipelineOverviewMonthly(folders, yesterdayFolderCount, averageFolderRange, startDate, endDate);
					projectStatitcs.setFolders(folderOverview);
				}

				if(kpisList.contains("Pipeline")) {
					StatisticsOverview pipelineOverview = buildFolderOrPipelineOverviewMonthly(pipelines, yesterdayPipelineCount, averagePipelineRange, startDate, endDate);
					projectStatitcs.setPipeline(pipelineOverview);
				}

				if(kpisList.contains("Dashboard")) {
					StatisticsOverview dashboardOverview = buildDashboardOverviewMonthly(qsRedashDashboards, yesterdayDashboardCount, averageDashboardRange, startDate, endDate);
					projectStatitcs.setDashboard(dashboardOverview);
				}

				if(kpisList.contains("Storage")) {
					DataVolumeOverview monthlyVolumeOverview = buildVolumeOverviewMonthly(sourceDataset, yesterdayStorageInMB, averageStorageRange, startDate, endDate);
					projectStatitcs.setDataVolume(monthlyVolumeOverview);
				}

			} else if ("Y".equalsIgnoreCase(requestType)) {
				if(kpisList.contains("Dataset")) {
					StatisticsOverview sourceDataSetOverview = buildSourceDSOverviewYearly(sourceDataset, yesterdaySourceDSCount, averageSourceDSRange);
					projectStatitcs.setSourceDataset(sourceDataSetOverview);
				}
				if(kpisList.contains("PreparedDS")) {
					StatisticsOverview preparedDatasetOverview = buildPreparedDSOverviewYearly(preparedDataSet, yesterdayPreparedDSCount, averagePreparedDSRange);
					projectStatitcs.setPreparedDataset(preparedDatasetOverview);
				}
				if(kpisList.contains("DataFlow")) {
					StatisticsOverview dataPipesOverview = buildDataPipeOverviewYearly(dataPipes, yesterdayDataPipeCount, averageDataPipeRange);
					projectStatitcs.setDataPipes(dataPipesOverview);
				}
				if(kpisList.contains("Folder")) {
					StatisticsOverview folderOverview = buildFolderOrPipelineOverviewYearly(folders, yesterdayFolderCount, averageFolderRange);
					projectStatitcs.setFolders(folderOverview);
				}
				if(kpisList.contains("Pipeline")) {
					StatisticsOverview pipelineOverview = buildFolderOrPipelineOverviewYearly(pipelines, yesterdayPipelineCount, averagePipelineRange);
					projectStatitcs.setPipeline(pipelineOverview);
				}
				if(kpisList.contains("Dashboard")) {
					StatisticsOverview dashboardOverview = buildDashboardOverviewYearly(qsRedashDashboards, yesterdayDashboardCount, averageDashboardRange);
					projectStatitcs.setDashboard(dashboardOverview);
				}
				if(kpisList.contains("Storage")) {
					DataVolumeOverview yearlyVolumeOverview = buildVolumeOverviewYearly(sourceDataset, yesterdayStorageInMB, averageStorageRange);
					projectStatitcs.setDataVolume(yearlyVolumeOverview);
				}

			} else {
				response = helper.createSuccessMessage(HttpStatus.BAD_REQUEST.value(), "Invalid requestType",
						projectStatitcs);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			ai.quantumics.api.vo.ProjectStatistics.Savings savings = projectStatitcs.new Savings();

			ai.quantumics.api.vo.ProjectStatistics.Effort effort = projectStatitcs.new Effort();

			if ("w".equalsIgnoreCase(requestType)) {
				int noOfDaysInWeek = QsUtil.findNumberOfBusinessDaysInAWeek(projectStartDate);
				effort.setManual(noOfDaysInWeek * 8);
				effort.setQsai(noOfDaysInWeek * 2);
			} else if ("m".equalsIgnoreCase(requestType)) {
				int noOfDaysInMonth = QsUtil.findNumberOfBusinessDaysInMonth(projectStartDate);
				effort.setManual(noOfDaysInMonth * 8);
				effort.setQsai(noOfDaysInMonth * 2);
			} else {
				int noOfDaysInYear = QsUtil.findNumberOfBusinessDaysByYear(projectStartDate);
				effort.setManual(noOfDaysInYear * 8);
				effort.setQsai(noOfDaysInYear* 2);
			}

			effort.setUnit("HOURS");

			double quantamicsPrice = qsSubscription.getPlanTypePrice();
			ai.quantumics.api.vo.ProjectStatistics.ValueUnitPair time = projectStatitcs.new ValueUnitPair();
			if ("w".equalsIgnoreCase(requestType)) {
				time.setValue(QsUtil.findNumberOfBusinessDaysInAWeek(projectStartDate) * 6 * numberOfUsers);
			} else if ("m".equalsIgnoreCase(requestType)) {
				int noOfDaysInMonth = QsUtil.findNumberOfBusinessDaysInMonth(projectStartDate);
				time.setValue(noOfDaysInMonth * 6 * numberOfUsers);
			} else {
				int noOfDaysInYear = QsUtil.findNumberOfBusinessDaysByYear(projectStartDate);;
				time.setValue(noOfDaysInYear * 6 * numberOfUsers);
			}

			time.setUnit("HOURS");

			ai.quantumics.api.vo.ProjectStatistics.ValueUnitPair money = projectStatitcs.new ValueUnitPair();
			if ("w".equalsIgnoreCase(requestType)) {
				money.setValue((otherDataOpsPrice * numberOfUsers * QsUtil.findNumberOfBusinessDaysInAWeek(projectStartDate)) - quantamicsPrice);
			} else if ("m".equalsIgnoreCase(requestType)) {
				int noOfDaysInMonth = QsUtil.findNumberOfBusinessDaysInMonth(projectStartDate);
				money.setValue((otherDataOpsPrice * numberOfUsers * noOfDaysInMonth) - quantamicsPrice);
			} else {
				int noOfDaysInYear = QsUtil.findNumberOfBusinessDaysByYear(projectStartDate);
				money.setValue((otherDataOpsPrice * numberOfUsers * noOfDaysInYear) - quantamicsPrice);
			}
			money.setUnit("USD");

			savings.setEffort(effort);
			savings.setTime(time);
			savings.setMoney(money);

			projectStatitcs.setSavings(savings);

			// Starting the Livy Sesssion
			helper.initializeLivySilently(projects.getProjectId());

			response = helper.createSuccessMessage(HttpStatus.OK.value(), "OK", projectStatitcs);

		} catch (QuantumsparkUserNotFound quantumsparkUserNotFound) {
			response = helper.failureMessage(HttpStatus.NOT_FOUND.value(), quantumsparkUserNotFound.getMessage());
		} catch (Exception exception) {
			response = helper.failureMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
		} finally {
			log.info("ProjectStatistics : getStatistics : completed");
		}
		return ResponseEntity.status((Integer) response.get("code")).body(response);
	}

	private DataVolumeOverview buildVolumeOverviewYearly(List<QsFiles> sourceDataset, double yesterdayStorageInMB, String averageStorageRange) {
		Map<String, Double> yearlyVolumeGroupByBytes = sourceDataset.stream()
				.filter((qsfile) -> Year.now().getValue() == QsUtil
						.convertToLocalDate(qsfile.getCreatedDate()).getYear())
				.collect(Collectors.groupingBy((qsFile) -> {
					LocalDate localDate = QsUtil.convertToLocalDate(qsFile.getCreatedDate());
					return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
				}, Collectors.summingDouble((qsFile) -> Double.parseDouble(qsFile.getFileSize()))));

		Map<String, Double> OverAllMonthsOfYear = Stream.of(new DateFormatSymbols().getMonths()).filter(item-> !item.isEmpty())
				.collect(LinkedHashMap<String, Double>::new, (m, v) -> m.put(v, 0.00), (m, m2) -> {});
		OverAllMonthsOfYear.putAll(yearlyVolumeGroupByBytes);

		DataVolumeOverview yearlyVolumeOverview = getDataVolumeOverviewV1(OverAllMonthsOfYear);
		yearlyVolumeOverview.setYesterdayCount(yesterdayStorageInMB);
		yearlyVolumeOverview.setAverageRange(averageStorageRange);
		return yearlyVolumeOverview;
	}

	private DataVolumeOverview buildVolumeOverviewMonthly(List<QsFiles> sourceDataset, double yesterdayStorageInMB, String averageStorageRange, LocalDate startDate, LocalDate endDate) {
		Map<String, Double> montlyVolumeGroupByBytes = sourceDataset.stream().filter((qsfile) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsfile.getCreatedDate());
			return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
					|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));
		}).collect(Collectors.groupingBy((qsFile) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsFile.getCreatedDate());
			int monthDay = MonthDay.from(creationDate).getDayOfMonth();
			return String.valueOf(monthDay);
		}, Collectors.summingDouble((qsFile) -> Double.parseDouble(qsFile.getFileSize()))));


		LocalDate start = LocalDate.now().withDayOfMonth(1);
		LocalDate end = LocalDate.now().plusMonths(1).withDayOfMonth(1);
		Map<String, Double> ovarAllDaysOfMonth = IntStream.range(1, (Stream.iterate(start, date -> date.plusDays(1))
						.limit(ChronoUnit.DAYS.between(start, end))
						.collect(Collectors.toList()).size()+1))
				.collect(LinkedHashMap<String, Double>::new, (m, v) -> m.put(String.valueOf(v), 0.00), (m, m2) -> {});
		ovarAllDaysOfMonth.putAll(montlyVolumeGroupByBytes);

		DataVolumeOverview monthlyVolumeOverview = getDataVolumeOverviewV1(ovarAllDaysOfMonth);
		monthlyVolumeOverview.setYesterdayCount(yesterdayStorageInMB);
		monthlyVolumeOverview.setAverageRange(averageStorageRange);
		return monthlyVolumeOverview;
	}

	private DataVolumeOverview buildVolumeOverviewWeekly(List<QsFiles> sourceDataset, double yesterdayStorageInMB, String averageStorageRange) {
		Map<String, Double> weeklyVolumeGroupByBytes = sourceDataset.stream().filter(
						(qsfile) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(qsfile.getCreatedDate())))
				.collect(Collectors.groupingBy((qsFile) -> QsUtil.getDay(qsFile.getCreatedDate()),
						Collectors.summingDouble((qsFile) -> Double.parseDouble(qsFile.getFileSize()))));

		Map<String, Double> OverAllDaysOfWeeks = Stream.of(DayOfWeek.values())
				.collect(LinkedHashMap<String, Double>::new, (m, v) -> m.put(StringUtils.capitalize(v.name().toLowerCase()), 0.00), (m, m2) -> {});
		OverAllDaysOfWeeks.putAll(weeklyVolumeGroupByBytes);

		DataVolumeOverview weeklyVolumeOverview = getDataVolumeOverviewV1(OverAllDaysOfWeeks);
		weeklyVolumeOverview.setYesterdayCount(yesterdayStorageInMB);
		weeklyVolumeOverview.setAverageRange(averageStorageRange);
		return weeklyVolumeOverview;
	}

	private static StatisticsOverview buildDashboardOverviewWeekly(List<QsRedashDashboard> qsRedashDashboards, long yesterdayDashboardCount, String averageDashboardRange) {
		Map<String, Long> weeklySummary = qsRedashDashboards.stream().filter(
						(qsRedashDashboard) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(qsRedashDashboard.getCreationDate())))
				.collect(Collectors.groupingBy((qsRedashDashboard) -> QsUtil.getDay(qsRedashDashboard.getCreationDate()),
						Collectors.counting()));

		Long weeklyCountFolders = QsUtil.sum(weeklySummary, 0L, Long::sum);
		StatisticsOverview dashboardOverview = new StatisticsOverview();
		dashboardOverview.setTotal(weeklyCountFolders);
		dashboardOverview.setSummary(QsUtil.getDaysOfWeeks(weeklySummary));
		dashboardOverview.setYesterdayCount(yesterdayDashboardCount);
		dashboardOverview.setAverageRange(averageDashboardRange);
		return dashboardOverview;
	}

	private static StatisticsOverview buildFoldersOrPipelineOverviewWeekly(List<QsFolders> folders, long yesterdayFolderCount, String averageFolderRange) {
		Map<String, Long> weeklySummary = folders.stream().filter(
						(qsFolders) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(qsFolders.getCreatedDate())))
				.collect(Collectors.groupingBy((qsFolders) -> QsUtil.getDay(qsFolders.getCreatedDate()),
						Collectors.counting()));

		Long weeklyCountFolders = QsUtil.sum(weeklySummary, 0L, Long::sum);
		StatisticsOverview foldersOrPipelineOverview = new StatisticsOverview();
		foldersOrPipelineOverview.setTotal(weeklyCountFolders);
		foldersOrPipelineOverview.setSummary(QsUtil.getDaysOfWeeks(weeklySummary));
		foldersOrPipelineOverview.setYesterdayCount(yesterdayFolderCount);
		foldersOrPipelineOverview.setAverageRange(averageFolderRange);
		return foldersOrPipelineOverview;
	}

	private static StatisticsOverview buildFolderOrPipelineOverviewYearly(List<QsFolders> folders, long yesterdayFolderCount, String averageFolderRange) {
		Map<String, Long> YearlySummary = folders.stream()
				.filter((qsFolders) -> Year.now().getValue() == QsUtil
						.convertToLocalDate(qsFolders.getCreatedDate()).getYear())
				.collect(Collectors.groupingBy((qsFolders) -> {
					LocalDate localDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
					return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
				}, Collectors.counting()));
		StatisticsOverview folderOrPipelineOverview = new StatisticsOverview();
		folderOrPipelineOverview.setTotal(QsUtil.sum(YearlySummary, 0L, Long::sum));
		folderOrPipelineOverview.setSummary(QsUtil.getMonthsOfYear(YearlySummary));
		folderOrPipelineOverview.setYesterdayCount(yesterdayFolderCount);
		folderOrPipelineOverview.setAverageRange(averageFolderRange);
		return folderOrPipelineOverview;
	}

	private static StatisticsOverview buildDashboardOverviewYearly(List<QsRedashDashboard> qsRedashDashboards, long yesterdayDashboardCount, String averageDashboardRange) {
		Map<String, Long> YearlySummary = qsRedashDashboards.stream()
				.filter((qsRedashDashboard) -> Year.now().getValue() == QsUtil
						.convertToLocalDate(qsRedashDashboard.getCreationDate()).getYear())
				.collect(Collectors.groupingBy((qsRedashDashboard) -> {
					LocalDate localDate = QsUtil.convertToLocalDate(qsRedashDashboard.getCreationDate());
					return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
				}, Collectors.counting()));
		StatisticsOverview dashboardOverview = new StatisticsOverview();
		dashboardOverview.setTotal(QsUtil.sum(YearlySummary, 0L, Long::sum));
		dashboardOverview.setSummary(QsUtil.getMonthsOfYear(YearlySummary));
		dashboardOverview.setYesterdayCount(yesterdayDashboardCount);
		dashboardOverview.setAverageRange(averageDashboardRange);
		return dashboardOverview;
	}

	private static StatisticsOverview buildDashboardOverviewMonthly(List<QsRedashDashboard> qsRedashDashboards, long yesterdayDashboardCount, String averageDashboardRange, LocalDate startDate, LocalDate endDate) {
		Map<String, Long> monthlySummary = qsRedashDashboards.stream().filter((qsRedashDashboard) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsRedashDashboard.getCreationDate());
			return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
					|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));
		}).collect(Collectors.groupingBy((qsRedashDashboard) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsRedashDashboard.getCreationDate());
			int monthDay = MonthDay.from(creationDate).getDayOfMonth();
			return String.valueOf(monthDay);
		}, Collectors.counting()));

		StatisticsOverview dashboardOverview = new StatisticsOverview();
		dashboardOverview.setTotal(QsUtil.sum(monthlySummary, 0L, Long::sum));
		dashboardOverview.setSummary(QsUtil.getDaysOfMonth(monthlySummary));
		dashboardOverview.setYesterdayCount(yesterdayDashboardCount);
		dashboardOverview.setAverageRange(averageDashboardRange);
		return dashboardOverview;
	}

	private static StatisticsOverview buildFolderOrPipelineOverviewMonthly(List<QsFolders> folders, long yesterdayFolderCount, String averageFolderRange, LocalDate startDate, LocalDate endDate) {
		Map<String, Long> monthlySummary = folders.stream().filter((qsFolders) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
			return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
					|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));
		}).collect(Collectors.groupingBy((qsFolders) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsFolders.getCreatedDate());
			int monthDay = MonthDay.from(creationDate).getDayOfMonth();
			return String.valueOf(monthDay);
		}, Collectors.counting()));

		StatisticsOverview folderOrPipelineOverview = new StatisticsOverview();
		folderOrPipelineOverview.setTotal(QsUtil.sum(monthlySummary, 0L, Long::sum));
		folderOrPipelineOverview.setSummary(QsUtil.getDaysOfMonth(monthlySummary));
		folderOrPipelineOverview.setYesterdayCount(yesterdayFolderCount);
		folderOrPipelineOverview.setAverageRange(averageFolderRange);
		return folderOrPipelineOverview;
	}


	private static StatisticsOverview buildDataPipeOverviewYearly(List<EngFlow> dataPipes, long yesterdayDataPipeCount, String averageDataPipeRange) {
		Map<String, Long> dataPipesYearlySummary = dataPipes.stream()
				.filter((engFlow) -> Year.now().getValue() == QsUtil
						.convertToLocalDate(engFlow.getCreatedDate()).getYear())
				.collect(Collectors.groupingBy((engFlow) -> {
					LocalDate localDate = QsUtil.convertToLocalDate(engFlow.getCreatedDate());
					return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
				}, Collectors.counting()));
		StatisticsOverview dataPipesOverview = new StatisticsOverview();
		dataPipesOverview.setTotal(QsUtil.sum(dataPipesYearlySummary, 0L, Long::sum));
		dataPipesOverview.setSummary(QsUtil.getMonthsOfYear(dataPipesYearlySummary));
		dataPipesOverview.setYesterdayCount(yesterdayDataPipeCount);
		dataPipesOverview.setAverageRange(averageDataPipeRange);
		return dataPipesOverview;
	}

	private static StatisticsOverview buildDataPipeOverviewMonthly(List<EngFlow> dataPipes, long yesterdayDataPipeCount, String averageDataPipeRange, LocalDate startDate, LocalDate endDate) {
		Map<String, Long> monthlyDataPipesSummary = dataPipes.stream().filter((engFlow) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(engFlow.getCreatedDate());
			return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
					|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));
		}).collect(Collectors.groupingBy((engFlow) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(engFlow.getCreatedDate());
			int monthDay = MonthDay.from(creationDate).getDayOfMonth();
			return String.valueOf(monthDay);
		}, Collectors.counting()));

		StatisticsOverview dataPipesOverview = new StatisticsOverview();
		dataPipesOverview.setTotal(QsUtil.sum(monthlyDataPipesSummary, 0L, Long::sum));
		dataPipesOverview.setSummary(QsUtil.getDaysOfMonth(monthlyDataPipesSummary));
		dataPipesOverview.setYesterdayCount(yesterdayDataPipeCount);
		dataPipesOverview.setAverageRange(averageDataPipeRange);
		return dataPipesOverview;
	}

	private static StatisticsOverview buildDataPipeOverviewWeekly(List<EngFlow> dataPipes, long yesterdayDataPipeCount, String averageDataPipeRange) {
		Map<String, Long> datapipesWeeklySummary = dataPipes.stream().filter(
						(engFlow) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(engFlow.getCreatedDate())))
				.collect(Collectors.groupingBy((engFlow) -> QsUtil.getDay(engFlow.getCreatedDate()),
						Collectors.counting()));
		StatisticsOverview dataPipesOverview = new StatisticsOverview();
		dataPipesOverview.setTotal(QsUtil.sum(datapipesWeeklySummary, 0L, Long::sum));
		dataPipesOverview.setSummary(QsUtil.getDaysOfWeeks(datapipesWeeklySummary));
		dataPipesOverview.setYesterdayCount(yesterdayDataPipeCount);
		dataPipesOverview.setAverageRange(averageDataPipeRange);
		return dataPipesOverview;
	}

	private static StatisticsOverview buildPreparedDSOverviewYearly(List<PreparedDataset> preparedDataSet, long yesterdayPreparedDSCount, String averagePreparedDSRange) {
		Map<String, Long> preparedDatasetYearlySummary = preparedDataSet.stream()
				.filter((fileMetaDataAwsRef) -> Year.now().getValue() == QsUtil
						.convertToLocalDate(fileMetaDataAwsRef.getCreatedDate()).getYear())
				.collect(Collectors.groupingBy((fileMetaDataAwsRef) -> {
					LocalDate localDate = QsUtil.convertToLocalDate(fileMetaDataAwsRef.getCreatedDate());
					return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
				}, Collectors.counting()));
		Long yearlyCountPreparedDataSet = QsUtil.sum(preparedDatasetYearlySummary, 0L, Long::sum);
		StatisticsOverview preparedDatasetOverview = new StatisticsOverview();
		preparedDatasetOverview.setTotal(yearlyCountPreparedDataSet);
		preparedDatasetOverview.setSummary(QsUtil.getMonthsOfYear(preparedDatasetYearlySummary));
		preparedDatasetOverview.setYesterdayCount(yesterdayPreparedDSCount);
		preparedDatasetOverview.setAverageRange(averagePreparedDSRange);
		return preparedDatasetOverview;
	}

	private static StatisticsOverview buildPreparedDSMonthly(List<PreparedDataset> preparedDataSet, long yesterdayPreparedDSCount, String averagePreparedDSRange, LocalDate startDate, LocalDate endDate) {
		Map<String, Long> preparedDatasetMonthlySummary = preparedDataSet.stream()
				.filter((fileMetaDataAwsRef) -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(fileMetaDataAwsRef.getCreatedDate());
					return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
							|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));
				}).collect(Collectors.groupingBy((fileMetaDataAwsRef) -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(fileMetaDataAwsRef.getCreatedDate());
					int monthDay = MonthDay.from(creationDate).getDayOfMonth();
					return String.valueOf(monthDay);
				}, Collectors.counting()));

		Long monthlyCountPrepardDataset = QsUtil.sum(preparedDatasetMonthlySummary, 0L, Long::sum);
		StatisticsOverview preparedDatasetOverview = new StatisticsOverview();
		preparedDatasetOverview.setTotal(monthlyCountPrepardDataset);
		preparedDatasetOverview.setSummary(QsUtil.getDaysOfMonth(preparedDatasetMonthlySummary));
		preparedDatasetOverview.setYesterdayCount(yesterdayPreparedDSCount);
		preparedDatasetOverview.setAverageRange(averagePreparedDSRange);
		return preparedDatasetOverview;
	}

	private static StatisticsOverview buildPreparedDSOverviewWeekly(List<PreparedDataset> preparedDataSet, long yesterdayPreparedDSCount, String averagePreparedDSRange) {
		Map<String, Long> preparedDataWeeklySummary = preparedDataSet.stream()
				.filter((fileMetaDataAwsRef) -> QsUtil
						.isWithInWeeklyWindow(QsUtil.convertToLocalDate(fileMetaDataAwsRef.getCreatedDate())))
				.collect(Collectors.groupingBy(
						(fileMetaDataAwsRef) -> QsUtil.getDay(fileMetaDataAwsRef.getCreatedDate()),
						Collectors.counting()));
		Long weeklyCountPreparedDataset = QsUtil.sum(preparedDataWeeklySummary, 0L, Long::sum);
		StatisticsOverview preparedDatasetOverview = new StatisticsOverview();
		preparedDatasetOverview.setTotal(weeklyCountPreparedDataset);
		preparedDatasetOverview.setSummary(QsUtil.getDaysOfWeeks(preparedDataWeeklySummary));
		preparedDatasetOverview.setYesterdayCount(yesterdayPreparedDSCount);
		preparedDatasetOverview.setAverageRange(averagePreparedDSRange);
		return preparedDatasetOverview;
	}

	private static StatisticsOverview buildSourceDSOverviewYearly(List<QsFiles> sourceDataset, long yestSourceDataSetCount, String averageSourceDataSet) {
		Map<String, Long> yearlySummary = sourceDataset.stream()
				.filter((qsfile) -> Year.now().getValue() == QsUtil
						.convertToLocalDate(qsfile.getCreatedDate()).getYear())
				.collect(Collectors.groupingBy((qsFile) -> {
					LocalDate localDate = QsUtil.convertToLocalDate(qsFile.getCreatedDate());
					return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
				}, Collectors.counting()));
		Long yearlyCountSourceDataset = QsUtil.sum(yearlySummary, 0L, Long::sum);
		StatisticsOverview sourceDataSetOverview = new StatisticsOverview();
		sourceDataSetOverview.setTotal(yearlyCountSourceDataset);
		sourceDataSetOverview.setSummary(QsUtil.getMonthsOfYear(yearlySummary));
		sourceDataSetOverview.setYesterdayCount(yestSourceDataSetCount);
		sourceDataSetOverview.setAverageRange(averageSourceDataSet);
		return sourceDataSetOverview;
	}

	private static StatisticsOverview buildSourceDSMonthly(List<QsFiles> sourceDataset, long yestSourceDataSetCount, String averageSourceDataSet, LocalDate startDate, LocalDate endDate) {
		Map<String, Long> monthlySummary = sourceDataset.stream().filter((qsfile) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsfile.getCreatedDate());

			return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
					|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));

		}).collect(Collectors.groupingBy((qsFile) -> {
			LocalDate creationDate = QsUtil.convertToLocalDate(qsFile.getCreatedDate());
			int monthDay = MonthDay.from(creationDate).getDayOfMonth();
			return String.valueOf(monthDay);
		}, Collectors.counting()));

		Long monthlyCountSourceDataset = QsUtil.sum(monthlySummary, 0L, Long::sum);
		StatisticsOverview sourceDataSetOverview = new StatisticsOverview();
		sourceDataSetOverview.setTotal(monthlyCountSourceDataset);
		sourceDataSetOverview.setSummary(QsUtil.getDaysOfMonth(monthlySummary));
		sourceDataSetOverview.setYesterdayCount(yestSourceDataSetCount);
		sourceDataSetOverview.setAverageRange(averageSourceDataSet);
		return sourceDataSetOverview;
	}

	private static StatisticsOverview buildSourceDSOverviewWeekly(List<QsFiles> sourceDataset, long yestSourceDataSetCount, String averageSourceDataSet) {
		Map<String, Long> weeklySummary = sourceDataset.stream().filter(
						(qsfile) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(qsfile.getCreatedDate())))
				.collect(Collectors.groupingBy((qsFile) -> QsUtil.getDay(qsFile.getCreatedDate()),
						Collectors.counting()));

		Long weeklyCountSourceDataset = QsUtil.sum(weeklySummary, 0L, Long::sum);
		StatisticsOverview sourceDataSetOverview = new StatisticsOverview();
		sourceDataSetOverview.setTotal(weeklyCountSourceDataset);
		sourceDataSetOverview.setSummary(QsUtil.getDaysOfWeeks(weeklySummary));
		sourceDataSetOverview.setYesterdayCount(yestSourceDataSetCount);
		sourceDataSetOverview.setAverageRange(averageSourceDataSet);
		return sourceDataSetOverview;
	}

	/**
	 * This prepares the data volume object with unit
	 * @param dataVolumeGroupByBytes
	 * @return
	 */
	@SuppressWarnings("unused")
	private DataVolumeOverview getDataVolumeOverviewV2(Map<String, Double> dataVolumeGroupByBytes) {
		ValueUnitPair totalVolume = getTotalVolume(dataVolumeGroupByBytes);

		Map<String, ValueUnitPair> weeklyVolumeSummary = getVolumeSummary(dataVolumeGroupByBytes);

		DataVolumeOverview weeklyVolumeOverview = prepareDataVolumeOverview(totalVolume, weeklyVolumeSummary);
		return weeklyVolumeOverview;
	}

	/**
	 * This prepares the data volume in MB
	 * @param dataVolumeGroupByBytes
	 * @return
	 */
	private DataVolumeOverview getDataVolumeOverviewV1(Map<String, Double> dataVolumeGroupByBytes) {
		ValueUnitPair totalVolume = getTotalVolumeInMB(dataVolumeGroupByBytes);

		Map<String, Double> dataVolumeSummary = getVolumeSummaryInMB(dataVolumeGroupByBytes);

		DataVolumeOverview weeklyVolumeOverview = prepareDataVolumeOverview(totalVolume, dataVolumeSummary);
		return weeklyVolumeOverview;
	}


	private DataVolumeOverview prepareDataVolumeOverview(ValueUnitPair totalVolume,
														 Map<String, ? extends Object> dataVolumeSummary) {
		DataVolumeOverview dataVolumeOverview = new DataVolumeOverview();
		dataVolumeOverview.setTotal(totalVolume.getValue());
		dataVolumeOverview.setUnit(totalVolume.getUnit());
		dataVolumeOverview.setSummary(dataVolumeSummary);
		return dataVolumeOverview;
	}

	private Map<String, ValueUnitPair> getVolumeSummary(Map<String, Double> dataVolumeGroupByBytes) {
		return dataVolumeGroupByBytes.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, (entryMap) -> QsUtil.calculateVolume(entryMap.getValue())));
	}

	private Map<String, Double> getVolumeSummaryInMB(Map<String, Double> dataVolumeGroupByBytes) {
		return dataVolumeGroupByBytes.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, (entryMap) -> QsUtil.convertBytesToMB(entryMap.getValue()), (k, v) -> k, LinkedHashMap<String, Double>::new));
	}

	private ValueUnitPair getTotalVolume(Map<String, Double> dataVolumeGroupByBytes) {
		double totalVolumeInBytes = dataVolumeGroupByBytes.values().stream().reduce(0.00, Double::sum);

		ValueUnitPair totalVolume = QsUtil.calculateVolume(totalVolumeInBytes);
		return totalVolume;
	}

	private ValueUnitPair getTotalVolumeInMB(Map<String, Double> dataVolumeGroupByBytes) {
		double totalVolumeInBytes = dataVolumeGroupByBytes.values().stream().reduce(0.00, Double::sum);
		double totalVoulumeInMB = QsUtil.convertBytesToMB(totalVolumeInBytes);
		ValueUnitPair valueUnitPair = new ProjectStatistics().new ValueUnitPair();
		valueUnitPair.setValue(totalVoulumeInMB);
		valueUnitPair.setUnit("MB");
		return valueUnitPair;
	}

	@ApiOperation(value = "Update KPI", response = Json.class, notes = "Update the kpi by sending request json in body")
	@PutMapping("/api/v1/project-stats/kpi")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "KPI Updation successfully"),
			@ApiResponse(code = 400, message = "Error updating the kpi")})
	public ResponseEntity<Object> updateHomeKPI(@RequestBody final UpdateKPIRequest request) {
		ResponseEntity<Object> response = null;
		try {
			helper.swtichToPublicSchema();
			QsUserV2 qsUserV2 = userServiceV2.getActiveUserById(request.getUserId());

			if(qsUserV2 == null) {
				log.info("No active user found for userId :"  + request.getUserId());
				response = returnResInstance(HttpStatus.BAD_REQUEST, "No active user found for userId :"  + request.getUserId(), null);
				return response;
			}

			Projects projects = helper.getProjects(request.getProjectId());

			if(projects == null) {
				log.info("No project found for projectId :"  + request.getProjectId());
				response = returnResInstance(HttpStatus.BAD_REQUEST, "No project for projectId :"  + request.getProjectId(), null);
				return response;
			}


			QsHomeKPI qsHomeKPI = homeKPIService.getHomeKPIForUser(request.getUserId());
			if(qsHomeKPI == null) {
				qsHomeKPI = new QsHomeKPI();
			}
			qsHomeKPI.setKpiDetails(request.getKpiDetails());
			qsHomeKPI.setUserId(request.getUserId());
			homeKPIService.updateHomeKPI(qsHomeKPI);
			log.info("KPI updated for projectId :"  + request.getProjectId());
			response = returnResInstance(HttpStatus.OK, "KPI Updated successfully.", null);
		} catch (Exception e) {
			response = returnResInstance(HttpStatus.BAD_REQUEST, "KPI updation failed.", null);
			log.error("Exception while updating the kpis {}", e.getMessage());
		}
		return response;

	}

	@ApiOperation(value = "Get KPIs", response = Json.class, notes = "Fetch kpis for projectId and userId")
	@GetMapping("/api/v1/project-stats/kpi/{userId}/{projectId}")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "KPI Fetched successfully"),
			@ApiResponse(code = 400, message = "Error Fetching KPI")})
	public ResponseEntity<Object> getHomeKPI(@PathVariable(value = "userId") final int userId,
											 @PathVariable(value = "projectId") final int projectId) {
		ResponseEntity<Object> response = null;
		try {
			helper.swtichToPublicSchema();
			QsUserV2 qsUserV2 = userServiceV2.getActiveUserById(userId);

			if(qsUserV2 == null) {
				log.info("No active user found for userId :"  + userId);
				response = returnResInstance(HttpStatus.BAD_REQUEST, "No active user found for userId :"  + userId, null);
				return response;
			}

			Projects projects = helper.getProjects(projectId);

			if(projects == null) {
				log.info("No project found for projectId :"  + projectId);
				response = returnResInstance(HttpStatus.BAD_REQUEST, "No project for projectId :"  + projectId, null);
				return response;
			}


			QsHomeKPI qsHomeKPI = homeKPIService.getHomeKPIForUser(userId);
			if(qsHomeKPI == null || StringUtils.isEmpty(qsHomeKPI.getKpiDetails())) {
				qsHomeKPI = new QsHomeKPI();
				qsHomeKPI.setKpiDetails("Dashboard,Storage,Dataset,DataFlow");
				qsHomeKPI.setUserId(userId);
			}

			log.info("KPI fetched for projectId :"  + projectId);
			response = returnResInstance(HttpStatus.OK, "KPI Fetched successfully.", qsHomeKPI);
		} catch (Exception e) {
			response = returnResInstance(HttpStatus.BAD_REQUEST, "KPI Fetching failed.", null);
			log.error("Exception while fetching the kpis {}", e.getMessage());
		}
		return response;

	}

	private ResponseEntity<Object> returnResInstance(HttpStatus code, String message, Object result) {
		HashMap<String, Object> genericResponse = new HashMap<>();
		genericResponse.put("code", code.value());
		genericResponse.put("message", message);

		if (result != null) {
			genericResponse.put("result", result);
		}
		return ResponseEntity.status(code).body(genericResponse);
	}
}
