/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.controller;

import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ai.quantumics.api.exceptions.QsRecordNotFoundException;
import ai.quantumics.api.exceptions.QuantumsparkUserNotFound;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.EngFlow;
import ai.quantumics.api.model.Projects;
import ai.quantumics.api.model.QsFiles;
import ai.quantumics.api.model.QsFolders;
import ai.quantumics.api.model.QsSubscription;
import ai.quantumics.api.model.QsUserProjects;
import ai.quantumics.api.req.PreparedDataset;
import ai.quantumics.api.req.StatisticsResponse;
import ai.quantumics.api.service.EngFlowJobService;
import ai.quantumics.api.service.EngFlowService;
import ai.quantumics.api.service.FileMetaDataAwsService;
import ai.quantumics.api.service.FileService;
import ai.quantumics.api.service.FolderService;
import ai.quantumics.api.service.RunJobService;
import ai.quantumics.api.service.SubscriptionService;
import ai.quantumics.api.service.UserProjectsService;
import ai.quantumics.api.util.QsUtil;
import ai.quantumics.api.vo.DataVolumeOverview;
import ai.quantumics.api.vo.ProjectStatistics;
import ai.quantumics.api.vo.ProjectStatistics.ValueUnitPair;
import ai.quantumics.api.vo.StatisticsOverview;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

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
	private SubscriptionService subscriptionService;

	public ProjectStatisticsController(FolderService folderServiceCi, FileService fileServiceCi, ControllerHelper helperCi,
									   EngFlowService engFlowServiceCi, RunJobService runJobServiceCi, EngFlowJobService engFlowJobServiceCi,
									   FileMetaDataAwsService fileMetaDataAwsServiceCi, UserProjectsService userProjectsServiceCi,
									   SubscriptionService subscriptionServiceCi) {
		folderService = folderServiceCi;
		fileService = fileServiceCi;
		helper = helperCi;
		engFlowService = engFlowServiceCi;
		runJobService = runJobServiceCi;
		engFlowJobService = engFlowJobServiceCi;
		fileMetaDataAwsService = fileMetaDataAwsServiceCi;
		userProjectsService = userProjectsServiceCi;
		subscriptionService = subscriptionServiceCi;
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
												@PathVariable(value = "userId") final int userId, @PathVariable(value = "projectId") final int projectId) {
		log.info("ProjectStatistics : getStatistics : started");
		List<QsFiles> activeFiles = null;
		List<QsFiles> sourceDataset = null;
		List<PreparedDataset> preparedDataSet = null;
		List<EngFlow> dataPipes = null;
		Map<String, Object> response = null;
		try {
			helper.swtichToPublicSchema();
			boolean isAdmin = helper.isAdmin(userId);
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
			activeFiles = fileService.getAllActiveFiles(projectId);
			if (isAdmin) {
				preparedDataSet = fileMetaDataAwsService.getFilesList(projectId);
			} else {
				preparedDataSet = fileMetaDataAwsService.getFilesList(projectId).stream()
						.filter((preparedDataset) -> userId == Integer.parseInt(preparedDataset.getCreatedBy().trim()))
						.sorted(Comparator.comparing(PreparedDataset::getCreatedDate).reversed())
						.collect(Collectors.toList());
			}

			sourceDataset = isAdmin ? activeFiles
					: activeFiles.stream().filter((qsfile) -> userId == qsfile.getUserId())
					.sorted(Comparator.comparing(QsFiles::getCreatedDate).reversed())
					.collect(Collectors.toList());

			dataPipes = isAdmin ? engFlowService.getFlowNames(projectId).stream()
					.filter((engflowP) -> engflowP.isActive()).collect(Collectors.toList())
					: engFlowService.getFlowNames(projectId, userId);

			ai.quantumics.api.vo.ProjectStatistics projectStatitcs = new ai.quantumics.api.vo.ProjectStatistics();
			projectStatitcs.setRequestType(requestType);

			if ("W".equalsIgnoreCase(requestType)) {
				Map<String, Long> weeklySummary = sourceDataset.stream().filter(
								(qsfile) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(qsfile.getCreatedDate())))
						.collect(Collectors.groupingBy((qsFile) -> QsUtil.getDay(qsFile.getCreatedDate()),
								Collectors.counting()));

				Long weeklyCountSourceDataset = QsUtil.sum(weeklySummary, 0L, Long::sum);
				StatisticsOverview sourceDataSetOverview = new StatisticsOverview();
				sourceDataSetOverview.setTotal(weeklyCountSourceDataset);
				sourceDataSetOverview.setSummary(QsUtil.getDaysOfWeeks(weeklySummary));

				projectStatitcs.setSourceDataset(sourceDataSetOverview);

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

				projectStatitcs.setPreparedDataset(preparedDatasetOverview);

				Map<String, Long> datapipesWeeklySummary = dataPipes.stream().filter(
								(engFlow) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(engFlow.getCreatedDate())))
						.collect(Collectors.groupingBy((engFlow) -> QsUtil.getDay(engFlow.getCreatedDate()),
								Collectors.counting()));
				StatisticsOverview dataPipesOverview = new StatisticsOverview();
				dataPipesOverview.setTotal(QsUtil.sum(datapipesWeeklySummary, 0L, Long::sum));
				dataPipesOverview.setSummary(QsUtil.getDaysOfWeeks(datapipesWeeklySummary));
				projectStatitcs.setDataPipes(dataPipesOverview);

				Map<String, Double> weeklyVolumeGroupByBytes = sourceDataset.stream().filter(
								(qsfile) -> QsUtil.isWithInWeeklyWindow(QsUtil.convertToLocalDate(qsfile.getCreatedDate())))
						.collect(Collectors.groupingBy((qsFile) -> QsUtil.getDay(qsFile.getCreatedDate()),
								Collectors.summingDouble((qsFile) -> Double.parseDouble(qsFile.getFileSize()))));

				Map<String, Double> OverAllDaysOfWeeks = Stream.of(DayOfWeek.values())
						.collect(LinkedHashMap<String, Double>::new, (m, v) -> m.put(StringUtils.capitalize(v.name().toLowerCase()), 0.00), (m, m2) -> {});
				OverAllDaysOfWeeks.putAll(weeklyVolumeGroupByBytes);

				DataVolumeOverview weeklyVolumeOverview = getDataVolumeOverviewV1(OverAllDaysOfWeeks);

				projectStatitcs.setDataVolume(weeklyVolumeOverview);

			} else if ("M".equalsIgnoreCase(requestType)) {
				LocalDate projectStartDateLocal = projectStartDate;
				LocalDate startDate = LocalDate.now().withDayOfMonth(1);
				LocalDate endDate = LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1);

				Map<String, Long> monthlySummary = sourceDataset.stream().filter((qsfile) -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsfile.getCreatedDate());
					//MonthDay firstDayofMonth = QsUtil.findFirstDateOfMonth(startDate, creationDate);
					return (creationDate.isEqual(startDate) || creationDate.isEqual(endDate))
							|| (creationDate.isBefore(endDate) && creationDate.isAfter(startDate));
					//return firstDayofMonth.compareTo(MonthDay.from(creationDate)) <= 0;
					//return ChronoUnit.DAYS.between(startDate, creationDate) <= -(LocalDate.now().lengthOfMonth());
				}).collect(Collectors.groupingBy((qsFile) -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsFile.getCreatedDate());
					int monthDay = MonthDay.from(creationDate).getDayOfMonth();
					return String.valueOf(monthDay);
				}, Collectors.counting()));

				Long monthlyCountSourceDataset = QsUtil.sum(monthlySummary, 0L, Long::sum);
				StatisticsOverview sourceDataSetOverview = new StatisticsOverview();
				sourceDataSetOverview.setTotal(monthlyCountSourceDataset);
				sourceDataSetOverview.setSummary(QsUtil.getDaysOfMonth(monthlySummary));

				projectStatitcs.setSourceDataset(sourceDataSetOverview);

				Map<String, Long> preparedDatasetMonthlySummary = preparedDataSet.stream()
						.filter((fileMetaDataAwsRef) -> {
							LocalDate creationDate = QsUtil.convertToLocalDate(fileMetaDataAwsRef.getCreatedDate());
							//MonthDay firstDayofMonth = QsUtil.findFirstDateOfMonth(projectStartDateLocal, creationDate);
							//return firstDayofMonth.compareTo(MonthDay.from(creationDate)) <= 0;
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

				projectStatitcs.setPreparedDataset(preparedDatasetOverview);

				Map<String, Long> monthlyDataPipesSummary = dataPipes.stream().filter((engFlow) -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(engFlow.getCreatedDate());
					//MonthDay firstDayofMonth = QsUtil.findFirstDateOfMonth(projectStartDateLocal, creationDate);
					//return firstDayofMonth.compareTo(MonthDay.from(creationDate)) <= 0;
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

				projectStatitcs.setDataPipes(dataPipesOverview);

				Map<String, Double> montlyVolumeGroupByBytes = sourceDataset.stream().filter((qsfile) -> {
					LocalDate creationDate = QsUtil.convertToLocalDate(qsfile.getCreatedDate());
					//MonthDay firstDayofMonth = QsUtil.findFirstDateOfMonth(projectStartDateLocal, creationDate);
					//return firstDayofMonth.compareTo(MonthDay.from(creationDate)) <= 0;
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

				projectStatitcs.setDataVolume(monthlyVolumeOverview);

			} else if ("Y".equalsIgnoreCase(requestType)) {

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

				projectStatitcs.setSourceDataset(sourceDataSetOverview);

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

				projectStatitcs.setPreparedDataset(preparedDatasetOverview);

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

				projectStatitcs.setDataPipes(dataPipesOverview);

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

				projectStatitcs.setDataVolume(yearlyVolumeOverview);

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
}
