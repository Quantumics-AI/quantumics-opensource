package ai.quantumics.api.user.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ai.quantumics.api.user.exceptions.PipeLineNotFoundException;
import ai.quantumics.api.user.exceptions.ProjectNotFoundException;
import ai.quantumics.api.user.exceptions.QuantumsparkUserNotFound;
import ai.quantumics.api.user.model.Pipeline;
import ai.quantumics.api.user.model.Projects;
import ai.quantumics.api.user.model.QsUserV2;
import ai.quantumics.api.user.service.PipelineService;
import ai.quantumics.api.user.service.ProjectService;
import ai.quantumics.api.user.service.UserServiceV2;

@Component
public class ValidatorUtils {
	private static Map<String, String> dateFormats = new HashMap<>();
	private static Map<String, DateTimeFormatter> dateFormatters = new HashMap<>();

	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserServiceV2 userService;
	@Autowired
	private PipelineService pipelineService;

	static {
		dateFormats.put("FORMAT1", "dd-MMM-yy hh.mm.ss.SSSSSS a");
		dateFormats.put("FORMAT2", "dd-MMM-yy");
		dateFormats.put("FORMAT3", "yyyy-MM-dd hh.mm.ss.SSSSSS a");
		dateFormats.put("FORMAT4", "yyyy-MM-dd");
		dateFormats.put("FORMAT5", "dd-MMM-yy hh.mm.ss");
		dateFormats.put("FORMAT6", "MM-dd-yyyy");
		dateFormats.put("FORMAT7", "MM/dd/yyyy");
		dateFormats.put("FORMAT8", "dd-M-yyyy hh:mm:ss");
		dateFormats.put("FORMAT9", "dd MMMM yyyy");
		dateFormats.put("FORMAT10", "dd MMMM yyyy zzzz");
		dateFormats.put("FORMAT11", "E, dd MMM yyyy HH:mm:ss z");

		dateFormatters.put("FORMATTER1", DateTimeFormat.forPattern(dateFormats.get("FORMAT1")));
		dateFormatters.put("FORMATTER2", DateTimeFormat.forPattern(dateFormats.get("FORMAT2")));
		dateFormatters.put("FORMATTER3", DateTimeFormat.forPattern(dateFormats.get("FORMAT3")));
		dateFormatters.put("FORMATTER4", DateTimeFormat.forPattern(dateFormats.get("FORMAT4")));
		dateFormatters.put("FORMATTER5", DateTimeFormat.forPattern(dateFormats.get("FORMAT5")));
		dateFormatters.put("FORMATTER6", DateTimeFormat.forPattern(dateFormats.get("FORMAT6")));
		dateFormatters.put("FORMATTER7", DateTimeFormat.forPattern(dateFormats.get("FORMAT7")));
		dateFormatters.put("FORMATTER8", DateTimeFormat.forPattern(dateFormats.get("FORMAT8")));
		dateFormatters.put("FORMATTER9", DateTimeFormat.forPattern(dateFormats.get("FORMAT9")));
		dateFormatters.put("FORMATTER10", DateTimeFormat.forPattern(dateFormats.get("FORMAT10")));
		dateFormatters.put("FORMATTER11", DateTimeFormat.forPattern(dateFormats.get("FORMAT11")));
	}

	public static boolean isDate(String str) {
		boolean flag = false;
		try {
			for (Map.Entry<String, DateTimeFormatter> me : dateFormatters.entrySet()) {
				DateTime value = DateTime.parse(str, me.getValue());
				// flag = dateValidator.isValid(str, dateFormat, Locale.getDefault());
				flag = true; // If parsing is successful, that means it is a valid date. No further
								// processing is needed.
				break;
			}
		} catch (Exception e) {
			// System.out.println(e.getMessage());
			flag = false;
		}
		return flag;
	}

	/*
	 * public static boolean isCurrency(String str){ return
	 * currencyValidator.isValid(str, Locale.getDefault()); }
	 */

	public QsUserV2 checkUser(int userId) throws QuantumsparkUserNotFound {
		QsUserV2 userObj;
		try {
			userObj = userService.getUserById(userId);
		} catch (SQLException e) {
			throw new QuantumsparkUserNotFound("User Not found");
		}
		if (userObj == null)
			throw new QuantumsparkUserNotFound("User Not found");
		return userObj;
	}

	public Projects checkProject(int projectId) throws ProjectNotFoundException {
		Projects project = projectService.getProject(projectId);
		if (project == null) {
			throw new ProjectNotFoundException("Project Not found");
		}
		return project;
	}

	public Pipeline checkPipeline(int pipelineId) throws PipeLineNotFoundException {
		Pipeline pipeline;
		try {
			pipeline = pipelineService.getPipelineById(pipelineId);
		} catch (SQLException e) {
			throw new PipeLineNotFoundException("Pipeline Not found");
		}
		if (pipeline == null)
			throw new PipeLineNotFoundException("Pipeline Not found");

		return pipeline;
	}
}
