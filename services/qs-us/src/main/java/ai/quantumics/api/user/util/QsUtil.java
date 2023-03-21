/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.util;

import ai.quantumics.api.user.constants.QsConstants;
import ai.quantumics.api.user.model.*;
import ai.quantumics.api.user.service.FolderService;
import ai.quantumics.api.user.service.PartitionService;
import ai.quantumics.api.user.service.ProjectCumulativeSizeService;
import ai.quantumics.api.user.service.SubscriptionService;
import ai.quantumics.api.user.vo.ProjectStatistics;
import ai.quantumics.api.user.vo.ProjectStatistics.ValueUnitPair;
import ai.quantumics.api.user.vo.QsFileContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@Slf4j
public class QsUtil {

	private final Random random = new Random();
	private final DbSessionUtil dbUtil;
	private final PartitionService partitionService;
	private final FolderService folderService;
	private final SubscriptionService subscriptionService;
	private final ProjectCumulativeSizeService projectSizeService;

	// private static final TemporalField workWeek = WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek();

	public QsUtil(
			DbSessionUtil dbUtilCi,
			PartitionService partitionServiceCi,
			FolderService folderServiceCi,
			SubscriptionService subscriptionServiceCi,
			ProjectCumulativeSizeService projectSizeServiceCi) {
		dbUtil = dbUtilCi;
		partitionService = partitionServiceCi;
		folderService = folderServiceCi;
		subscriptionService = subscriptionServiceCi;
		projectSizeService = projectSizeServiceCi;
	}

	public QsFileContent decideTableName(
			final QsFileContent tableDetails, final boolean isProcessedDB) throws SQLException {
		String tableName = null;
		//boolean isTabPresent = false;
		//List<String> awsTables = null;
		//boolean isPartPresent = false;
		String partitionRelName = null;
		QsPartition relatedPartition = null;
		final String fileName = tableDetails.getFileName();
		final String folderName = tableDetails.getFolderName();

		dbUtil.changeSchema(tableDetails.getProject().getDbSchemaName());
		if (!isProcessedDB) {
			Optional<QsPartition> partitionByFileId = partitionService.getPartitionByFileId(tableDetails.getFolderId(), tableDetails.getFileId());
			if(partitionByFileId.isPresent()){
				relatedPartition = partitionByFileId.get();
				partitionRelName = relatedPartition.getPartitionName();
			}
		}
		tableName = nameCheck(folderName, fileName);

		//awsTables = getTableNamesList(tableDetails, isProcessedDB);
		//isTabPresent = awsTables.contains(tableName);
		//if (partitionRelName != null) {
		//  isPartPresent = awsTables.contains(partitionRelName.toLowerCase());
		//}

		tableDetails.setTableName(tableName);
		tableDetails.setPartition(partitionRelName);

		/*if (isTabPresent) {
      tableDetails.setTableName(tableName);
      tableDetails.setPartition(partitionRelName);
      log.info("Table {} and partition {}", tableName, partitionRelName);
    } else if (isPartPresent) {
      tableDetails.setTableName(partitionRelName.toLowerCase());
      tableDetails.setPartition(null);
      log.info("Partition & TableName {}", partitionRelName.toLowerCase());
    } else {
      log.warn("UnExpected Scenario tableName/partition were not found in tables");
      tableDetails.setTableName(null);
      tableDetails.setPartition(null);
    }*/
		log.debug("Table Details {}", tableDetails.toString());
		return tableDetails;
	}

	public String nameCheck(final String folderName, final String fileName) {
		String tableName;
		if (Objects.isNull(folderName) || StringUtils.isEmpty(folderName.trim())) {
			tableName = fileName.replace(".csv", "_csv");
		} else if (folderName.contains(" ")) {
			tableName = folderName.trim().replace(" ", "_");
		} else {
			tableName = folderName;
		}
		return tableName.toLowerCase();
	}

	public String randomAlphaNumeric() {
		final int lowerLimit = 48;
		final int upperLimit = 122;
		final int desiredLength = 5;

		return random
				.ints(lowerLimit, upperLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(desiredLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	public String generateRandomGuid() {
		return UUID.randomUUID().toString();
	}

	public Map<String, String> validateFileBeforeUpload(Object fileObj, QsFolders folder,
			Projects project, QsUserV2 user, String action) throws Exception {
		InputStream is = null;
		long fileSize = 0l;
		if (fileObj instanceof MultipartFile) {
			MultipartFile multipartFile = (MultipartFile) fileObj;
			fileSize = multipartFile.getSize();
			is = multipartFile.getInputStream();
			log.info("File is a MultipartFile Instance with name: {}",
					multipartFile.getOriginalFilename());
		} else if (fileObj instanceof File) {
			File normalFile = (File) fileObj;
			fileSize = normalFile.length();
			log.info("File is a regular file Instance with name: {} and path: {}", normalFile.getName(),
					normalFile.getAbsolutePath());
			is = new FileInputStream(normalFile);
		}

		// Check whether the column headers are having any duplicates...
		StringBuilder sb = new StringBuilder();
		Map<String, String> responseMsgMap = new HashMap<>();

		dbUtil.changeSchema("public");

		// Check the file size based on the subscription before performing any other check or uploading the file.
		ProjectSubscriptionDetails projSubDetails = subscriptionService.getProjectSubscriptionByStatus(project.getProjectId(), 
				(user.getUserParentId() == 0)?user.getUserId():user.getUserParentId(), 
						QsConstants.SUBSCRIPTION_STATUS_ACTIVE);
		if(projSubDetails != null) {
			QsSubscription subscription = subscriptionService.getSubscriptionByNameAndPlanType(projSubDetails.getSubscriptionType(), 
					projSubDetails.getSubscriptionPlanType());
			if(subscription != null) {
				log.info("Subsciption Type is: {} and Plan Type is: {}", subscription.getName(), subscription.getPlanType());
				String planTypeSettings = subscription.getPlanTypeSettings();
				log.info("Subscription Plan Type settings are: {}", planTypeSettings);

				ObjectMapper mapper = new ObjectMapper();
				JsonNode settingsNode = mapper.readValue(planTypeSettings, JsonNode.class);
				String maxFilesizeBytesStr = settingsNode.get(QsConstants.SUBSCRIPTION_MAX_FILE_SIZE_BYTES_PROP).asText();
				long maxFilesizeBytes = Long.parseLong(maxFilesizeBytesStr);

				if(fileSize > maxFilesizeBytes) {
					sb.append("File size: ").append(fileSize).append(" bytes exceeds max allowed size: ").
					append(maxFilesizeBytes).append(" bytes for the current subscription: ").append(subscription.getName()).append("\n");

					responseMsgMap.put("ERROR_MSG", sb.toString());
					return responseMsgMap;
				}

				String cumulativeSizeBytesStr = settingsNode.get(QsConstants.SUBSCRIPTION_CUMULATIVE_SIZE_BYTES_PROP).asText();
				long cumulativeSizeBytes = Long.parseLong(cumulativeSizeBytesStr);

				ProjectCumulativeSizeInfo projectSizeInfo = projectSizeService.getSizeInfoForProject(project.getProjectId(), user.getUserId());
				if(projectSizeInfo != null) {
					long currentProjSize = fileSize+projectSizeInfo.getCumulativeSize();

					if(currentProjSize > cumulativeSizeBytes) {
						sb.delete(0, sb.length());

						sb.append("File cannot be uploaded as cumulative size of the project : ").append(currentProjSize).append(" bytes exceeded the allocated size of: ").
						append(cumulativeSizeBytes).append(" bytes for the current subscription: ").append(subscription.getName()).append("\n");

						responseMsgMap.put("ERROR_MSG", sb.toString());
						return responseMsgMap;
					}
				}
			}
		} else {
			sb.append("File cannot be uploaded as Project Subscription is not found for the Project Id: ").append(project.getProjectId()).append(" and User Id: ").
			append(user.getUserId()).append("\n");

			responseMsgMap.put("ERROR_MSG", sb.toString());
			return responseMsgMap;
		}

		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			// Limiting to only reading two lines, with the assumption that
			// the first line is a header line, followed by data line.
			br.lines().limit(2).forEach(lines::add);
			if (lines.size() == 2) {
				String columnHeadersStr = lines.get(0);

				if (columnHeadersStr == null || columnHeadersStr.isEmpty()) {
					log.info("File is empty, there are no column headers to validate.");
					return Collections.emptyMap();
				}

				List<String> columnHeaders = null;
				if (columnHeadersStr.indexOf(QsConstants.DELIMITER) != -1) {
					String[] colHeadersArr = columnHeadersStr.split(QsConstants.DELIMITER_SPLIT_PATTERN);
					columnHeaders = Arrays.asList(colHeadersArr);
				}
				else {
					columnHeaders = new ArrayList<>();
					columnHeaders.add(columnHeadersStr);
				}

				// Check whether the first data record has values or empty record. If so, stop from uploading the file.
				String dataRecord = lines.get(1);
				log.info("First data record in the CSV file is: {}", dataRecord);
				if(dataRecord.matches("^[, ]*$")) {

					sb.delete(0, sb.length());
					sb.append(
							"First record in the file is invalid and cannot be uploaded. ");
					sb.append(" Data record is: \n");
					sb.append(dataRecord);

					responseMsgMap.put("ERROR_MSG", sb.toString());
					return responseMsgMap;
				}

				Set<String> duplicateCols = validateColHeadersForDups(columnHeaders);
				if(duplicateCols != null && !duplicateCols.isEmpty()) {
					String dcs = duplicateCols.toString();
					dcs = dcs.substring(1, dcs.length()-1);

					sb.delete(0, sb.length());
					// Duplicate column headers are present...
					sb.append(
							"File has duplicate column headers and cannot be uplaoded. ");
					sb.append(" Column names are: ");

					responseMsgMap.put("ERROR_MSG", sb.toString());
					responseMsgMap.put("INVALID_COLS", dcs);

					return responseMsgMap;
				}

				List<String> invalidColHeaders = validateColumnHeaders(columnHeaders);
				if (invalidColHeaders != null && !invalidColHeaders.isEmpty()) {
					String ichs = invalidColHeaders.toString();
					ichs = ichs.substring(1, ichs.length()-1);

					sb.delete(0, sb.length());
					// Invalid column headers are present...
					sb.append(
							"File has invalid column headers and cannot be uploaded. Only Alpha-numeric characters are supported in the column header.");
					sb.append(" \nInvalid column headers are: \n");
					sb.append(ichs);

					responseMsgMap.put("ERROR_MSG", sb.toString());
					responseMsgMap.put("INVALID_COLS", ichs);

					return responseMsgMap;
				}

				// Validate the Column headers now....
				String colHeadersStr = columnHeaders.toString();
				colHeadersStr = colHeadersStr.substring(1, colHeadersStr.length()-1);
				String origColumnHeaders = folder.getColumnHeaders();
				if (origColumnHeaders == null || origColumnHeaders.isEmpty()) {
					// This is the first time user is trying to upload a file to this Folder, hence
					// no validation is needed.
					// We have to update the column metadata of this file in QSP_FOLDER table. This
					// column header info is used
					// for subsequent uploads to this folder.
					if(!"validatefileupload".equals(action)){
						dbUtil.changeSchema(project.getDbSchemaName());

						folder.setColumnHeaders(colHeadersStr);
						folderService.saveFolders(folder);
					}

				} else if (!colHeadersStr.equals(origColumnHeaders)) {
					sb.delete(0, sb.length());
					sb.append(
							"File column headers are not matching with the Original file column headers which was uploaded when the folder is created.");

					responseMsgMap.put("ERROR_MSG", sb.toString());
					responseMsgMap.put("ORIGINAL_COL_HEADERS", origColumnHeaders);
					responseMsgMap.put("CURRENT_FILE_COL_HEADERS", colHeadersStr);

					return responseMsgMap;
				}
			}
			else {
				responseMsgMap.put("ERROR_MSG", "File has no records and cannot be uploaded.");

				return responseMsgMap;
			}
		} catch (Exception e) {
			log.error("Failed to read the file to perform validation. Exception is: {}", e.getMessage());
			//return e.getMessage();
			throw new IOException(e.getMessage());
		}

		return Collections.emptyMap();
	}

	public String checkPiiColumnsInFile(Object fileObj, int projectId, int userId, int folderId) throws IOException{

		File tmpCsvFile = null;
		File tmpJsonFile = null;
		BufferedWriter bwCsv = null;
		BufferedReader brCsv = null;
		BufferedWriter bwJson = null;
		String fileNameWithoutExt = null;
		File normalFile = null;
		try {
			InputStream is = null;
			MultipartFile multipartFile = null;
			boolean bNormalFile = true;
			if (fileObj instanceof MultipartFile) {
				 multipartFile = (MultipartFile) fileObj;
				is = multipartFile.getInputStream();
				log.info("File is a MultipartFile Instance with name: {}", multipartFile.getOriginalFilename());
				fileNameWithoutExt = multipartFile.getOriginalFilename().substring(0, multipartFile.getOriginalFilename().lastIndexOf("."));
				bNormalFile = false;
			} else if (fileObj instanceof File) {
				normalFile = (File) fileObj;
				log.info("File is a regular file Instance with name: {} and path: {}", normalFile.getName(),
						normalFile.getAbsolutePath());
				is = new FileInputStream(normalFile);
				fileNameWithoutExt = normalFile.getName().substring(0, normalFile.getName().lastIndexOf("."));
			}
			String tmpdir = System.getProperty("java.io.tmpdir");
			String tmpCsvFilePath = String.format("%s/%s/%s/%s/%s.csv", tmpdir, projectId, userId, folderId, fileNameWithoutExt);
			if(!bNormalFile) {
				tmpCsvFile = new File(tmpCsvFilePath);
				if(!tmpCsvFile.getParentFile().exists()) {
					tmpCsvFile.getParentFile().mkdirs();
				}

				bwCsv = new BufferedWriter(new FileWriter(tmpCsvFile));
				brCsv = new BufferedReader(new InputStreamReader(is));

				String s = null;
				while((s = brCsv.readLine()) != null) {
					bwCsv.write(s);
					bwCsv.newLine();
				}

				bwCsv.flush();
			}

			final ObjectMapper mapper = new ObjectMapper();
			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			List<Object> readAllLines =  null;
			if(multipartFile != null && !bNormalFile) {
				readAllLines = csvMapper.readerFor(Map.class).with(csvSchema).readValues(multipartFile.getInputStream()).readAll();
			}else {
		        readAllLines = csvMapper.readerFor(Map.class).with(csvSchema).readValues(is).readAll();
			
			}
			String op = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAllLines);
			String tmpFilePath = String.format("%s/%s/%s/%s/%s.json", tmpdir, projectId, userId, folderId, fileNameWithoutExt);
			tmpJsonFile = new File(tmpFilePath);

			if(!tmpJsonFile.getParentFile().exists()) {
				tmpJsonFile.getParentFile().mkdirs();
			}

			log.info("JSON file: {}", tmpJsonFile.getAbsolutePath());
			bwJson = new BufferedWriter(new FileWriter(tmpJsonFile));
			bwJson.write(op);
			bwJson.flush();

		}catch(IOException ioe) {
			log.info("Exception occurred while processing the PII Columns check.", ioe.getMessage());
		}finally{
			try {
				if(bwJson != null) bwJson.close();
				if(bwCsv != null) bwCsv.close();
				if(brCsv != null) brCsv.close();
			}catch(IOException e) {
				// do nothing
			}
		}

		return tmpJsonFile.getAbsolutePath();
	}

	private List<String> validateColumnHeaders(List<String> columnHeaders) {
		List<String> invalidColHeaders = new ArrayList<>();

		if (columnHeaders != null && !columnHeaders.isEmpty()) {
			columnHeaders.stream().forEach((columnHeader) -> {
				boolean flag = validateColHeader(columnHeader);
				if (!flag) {
					invalidColHeaders.add(columnHeader);
				}
			});
		}

		return invalidColHeaders;
	}

	private boolean validateColHeader(String columnHeader) {
		return RegexUtils.isAlphaNumeric(columnHeader);
	}

	private Set<String> validateColHeadersForDups(List<String> columnHeaders){
		if(columnHeaders != null && !columnHeaders.isEmpty() && (columnHeaders.size() > 1)) {
			Set<String> nonUniques = new HashSet<>();
			Set<String> uniques = new HashSet<>();
			columnHeaders.stream().forEach((col) -> {
				boolean flag = uniques.add(col.toLowerCase());
				if(!flag) {
					nonUniques.add(col);
				}
			});

			return nonUniques;
		}

		return Collections.emptySet();
	}


	public static Date epochToDate(Long value) {
		if(value == null || value == 0l) {
			return null;
		}
		return new Date(value * 1000l);
	}

	public static String setRuleArgs(String ruleDefFormat, Object... args) {
		return (ruleDefFormat != null && !ruleDefFormat.isEmpty()) ? String.format(ruleDefFormat, args)
				: "";
	}

	public static Date findDateTimeByDays(int noOfDays) {
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime then = now.minusDays(noOfDays);  
		return convertToDateViaInstantTime(then);  
	}

	public static LocalDate findDateByDays(int noOfDays, String beforeOrAfter) {
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate now = LocalDate.now();
		LocalDate then = null;
		if("+".equals(beforeOrAfter)) {
			then = now.plusDays(noOfDays);  
		} else {
			then = now.minusDays(noOfDays);  
		}
		return then;  
	}

	public static Date convertToDateViaInstantTime(LocalDateTime dateToConvert) {
		return java.util.Date
				.from(dateToConvert.atZone(ZoneId.systemDefault())
						.toInstant());
	}

	public static Date convertToDateViaInstant(LocalDate dateToConvert) {
		return java.util.Date.from(dateToConvert.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

	public static LocalDate convertToLocalDate(Date dateToConvert) {
		return dateToConvert.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	public static boolean isWithInWeeklyWindow(LocalDate dateIn) {
		TemporalField startOfTheWeek = WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek();
		LocalDate today = LocalDate.now();
		LocalDate weekStartDate = today.with(startOfTheWeek, 1);
		LocalDate weekEndDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		return getBusinessDays(weekStartDate, weekEndDate, true).contains(dateIn);
	}

	public static String getDay(Date dateIn) {
		return convertToLocalDate(dateIn)
				.getDayOfWeek()
				.getDisplayName(TextStyle.FULL, Locale.getDefault());
	}

	public static DayOfWeek findNthDay() {
		LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
		return currentDate.getDayOfWeek();
	}

	/*
	 * 
	 */
	public static int findNumberOfBusinessDaysInAWeek(LocalDate projectDate) {
		int inclusive = 1;
		LocalDate projectLocalDate = projectDate;
		LocalDate currentDate = LocalDate.now();
		boolean sameWeek = isSameWeek(projectLocalDate, currentDate);

		int currentDayOfWeek = currentDate.getDayOfWeek().getValue() > 5 ? 5 : currentDate.getDayOfWeek().getValue();
		int numberOfBusinessDays = !sameWeek ? currentDayOfWeek
				: (currentDayOfWeek - projectLocalDate.getDayOfWeek().getValue()) + inclusive;
		return numberOfBusinessDays;
	}

	private static boolean isSameWeek(LocalDate date1, LocalDate date2) {
		WeekFields workWeek = WeekFields.of(DayOfWeek.MONDAY, 1);// TODO : Check with time travel
		boolean sameWeek = date1.query( temporal -> {
			boolean sameWeekLocal = false;
			// WeekFields weekOfTheYear = workWeek.
			int weekInTheYear = date1.get(workWeek.weekOfWeekBasedYear());
			int currentWeekInTheYear = date2.get(workWeek.weekOfWeekBasedYear());
			sameWeekLocal = weekInTheYear == currentWeekInTheYear
					&& date1.get(workWeek.weekBasedYear()) == date2.get(workWeek.weekBasedYear());
			return sameWeekLocal;
		});
		return sameWeek;
	}

	public static int findNumberOfBusinessDaysInAWeek(Date projectDate) {
		return findNumberOfBusinessDaysInAWeek(convertToLocalDate(projectDate));
	}

	public static int findNumberOfBusinessDaysInMonth(LocalDate projectDate) {
		LocalDate currentDate = LocalDate.now();
		LocalDate startDate = null;

		if(YearMonth.from(projectDate).equals(YearMonth.from(currentDate))) {
			startDate = projectDate;
		} else {
			startDate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1);
		}
		return getBusinessDays(startDate, currentDate, false).size();
	}


	public static MonthDay findFirstDateOfMonth(LocalDate projectStartDate, LocalDate datasetCreatedDate) {
		MonthDay startDate = MonthDay.of(datasetCreatedDate.getMonthValue(), 1);
		if(YearMonth.from(projectStartDate).equals(YearMonth.from(datasetCreatedDate))) {
			startDate = MonthDay.of(projectStartDate.getMonthValue(), projectStartDate.getDayOfMonth());
		} 
		return startDate;
	}

	public static int findNumberOfBusinessDaysByYear(LocalDate projectDate) {
		LocalDate currentDate = LocalDate.now();
		LocalDate startDate = projectDate;
		if(currentDate.getYear() > projectDate.getYear()) {
			startDate = LocalDate.of(currentDate.getYear(), Month.JANUARY.getValue(), 1);
		} 
		return getBusinessDays(startDate, currentDate, false).size();
	}

	public static List<LocalDate>  getBusinessDays(LocalDate startDate, LocalDate endDate, boolean includeWeekends) {

		Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
				|| date.getDayOfWeek() == DayOfWeek.SUNDAY;
		// Get all days between two dates
		long daysBetween = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));// To Temporal2Inclusive


		// Iterate over stream of all dates and check each day against any weekday or
		// holiday
		List<LocalDate> businessDates = null;
		businessDates =  includeWeekends ? Stream.iterate(startDate, date -> date.plusDays(1))
				.limit(daysBetween)
				.collect(Collectors.toList())
				: Stream.iterate(startDate, date -> date.plusDays(1))
				.limit(daysBetween)
				.filter(isWeekend.negate())
				.collect(Collectors.toList());


		return businessDates;

	}

	public static long calculateDays(LocalDate startDate, LocalDate endDate) {	  
		final int startW = startDate.getDayOfWeek().getValue();
		final int endW = endDate.getDayOfWeek().getValue();
		final long days = ChronoUnit.DAYS.between(startDate, endDate);
		long numberofweekends = 2*(days/7);
		long totalDays = days - numberofweekends; //remove weekends
		if (days % 7 != 0) { //deal with the rest days
			if (startW == 7) {
				totalDays -= 1;
			} else if (endW == 7) {  //they can't both be Sunday, otherwise rest would be zero
				totalDays -= 1;
			} else if (endW < startW) { //another weekend is included
				totalDays -= 2;
			}
		}
		return totalDays;
	}

	public static <T> T sum(Map<?, T> m, T identity, BinaryOperator<T> summer) {
		return m.values().stream().reduce(identity, summer);
	}

	public static double convertBytesToGB(double bytes) {

		BigDecimal bd = new BigDecimal(bytes/(QsConstants.STORAGE_DIVISOR * QsConstants.STORAGE_DIVISOR * QsConstants.STORAGE_DIVISOR))
				.setScale(3, RoundingMode.HALF_EVEN);
		return bd.doubleValue();

	}

	public static double convertBytesToMB(double bytes) {
		BigDecimal bd = new BigDecimal(bytes/(QsConstants.STORAGE_DIVISOR * QsConstants.STORAGE_DIVISOR)).setScale(3, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}

	public static double convertBytesToKB(double bytes) {
		BigDecimal bd = new BigDecimal(bytes/(QsConstants.STORAGE_DIVISOR)).setScale(3, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}


	public static ValueUnitPair calculateVolume(final double bytes) {
		ValueUnitPair valueUnitPair = null;

		double size_kb = bytes /QsConstants.STORAGE_DIVISOR;
		double size_mb = size_kb / QsConstants.STORAGE_DIVISOR;
		double size_gb = size_mb / QsConstants.STORAGE_DIVISOR ;

		if (size_kb < QsConstants.STORAGE_DIVISOR){
			valueUnitPair = new ProjectStatistics().new ValueUnitPair();
			valueUnitPair.setValue(new BigDecimal(size_kb).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
			valueUnitPair.setUnit("KB");
		}else if(size_mb < QsConstants.STORAGE_DIVISOR){
			valueUnitPair = new ProjectStatistics().new ValueUnitPair();
			valueUnitPair.setValue(new BigDecimal(size_mb).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
			valueUnitPair.setUnit("MB");
		}else{
			valueUnitPair = new ProjectStatistics().new ValueUnitPair();
			valueUnitPair.setValue(new BigDecimal(size_gb).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
			valueUnitPair.setUnit("GB");

		}	

		return valueUnitPair;

	}

	public static Map<String, Long> getDaysOfWeeks(Map<String, Long> existingDaysOfWeeks) {
		Map<String, Long> OverAllDaysOfWeeks = Stream.of(DayOfWeek.values())
				.collect(LinkedHashMap<String, Long>::new, (m, v) -> m.put(StringUtils.capitalize(v.name().toLowerCase()), 0L), (m, m2) -> {});
		OverAllDaysOfWeeks.putAll(existingDaysOfWeeks);
		return OverAllDaysOfWeeks;
	}

	public static Map<String, Long> getDaysOfMonth(Map<String, Long> existingDaysOfMonth) {
		LocalDate start = LocalDate.now().withDayOfMonth(1);
		LocalDate end = LocalDate.now().plusMonths(1).withDayOfMonth(1);
		Map<String, Long> ovarAllDaysOfMonth = IntStream.range(1, (Stream.iterate(start, date -> date.plusDays(1))
				.limit(ChronoUnit.DAYS.between(start, end))
				.collect(Collectors.toList()).size()+1))
				.collect(LinkedHashMap<String, Long>::new, (m, v) -> m.put(String.valueOf(v), 0L), (m, m2) -> {});
		ovarAllDaysOfMonth.putAll(existingDaysOfMonth);
		return ovarAllDaysOfMonth;
	}

	public static Map<String, Long> getMonthsOfYear(Map<String, Long> existingMonthsOfYear) {
		Map<String, Long> OverAllMonthsOfYear = Stream.of(new DateFormatSymbols().getMonths()).filter(item-> !item.isEmpty())
				.collect(LinkedHashMap<String, Long>::new, (m, v) -> m.put(v, 0L), (m, m2) -> {});
		OverAllMonthsOfYear.putAll(existingMonthsOfYear);
		return OverAllMonthsOfYear;
	}

	public static void main(String...args) {
		LocalDate projectDate = LocalDate.of(2021, 11, 29);
		System.out.println(isWithInWeeklyWindow(projectDate));

	}
}
