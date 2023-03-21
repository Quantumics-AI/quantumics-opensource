package ai.quantumics.api.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ai.quantumics.api.adapter.AwsAdapter;
import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.helper.ControllerHelper;
import ai.quantumics.api.model.QsUdf;
import ai.quantumics.api.model.QsUserV2;
import ai.quantumics.api.req.UdfInfo;
import ai.quantumics.api.req.UdfReqColumn;
import ai.quantumics.api.service.UdfService;
import ai.quantumics.api.service.UserServiceV2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

@Slf4j
@RestController
@Api(value = "QuantumSpark Service API ")
public class UdfController {
	
	private static final Gson gson = new Gson();
	private final UdfService udfService;
	private final UserServiceV2 userServiceV2;
	private final AwsAdapter awsAdapter;
	private final ControllerHelper helper;
	
	@Value("${s3.udf.bucketName}")
	private String udfBucketName;

	public UdfController(UdfService udfService, UserServiceV2 userServiceV2, 
			AwsAdapter awsAdapter, ControllerHelper helper){
		this.udfService = udfService;
		this.userServiceV2 = userServiceV2;
		this.awsAdapter = awsAdapter;
		this.helper = helper;
	}
	
	 private String getGson(final Object toConvert) {
		  return gson.toJson(toConvert);
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

	@ApiOperation(value = "List User Defined functions for a Project", response = Json.class)
	@GetMapping("/api/v1/udf/{projectId}/{userId}")
	@ApiResponses(
			value = {@ApiResponse(code = 200, message = "UDF's for a project returned successfully."),
					@ApiResponse(code = 400, message = "Failed to fetch the UDF's for a project."),
					@ApiResponse(code = 500, message = "Internal error occured.")})
	public ResponseEntity<Object> getUdfsForProject(
			@PathVariable(value = "projectId") final int projectId, 
			@PathVariable(value = "userId") final int userId) {
		ResponseEntity<Object> response = null;
		try {
			helper.swtichToPublicSchema();
			List<QsUdf> udfs = udfService.getUdfByProjectIdAndUserId(projectId, userId);
			log.info("Udf Info :: for all udfs {}", udfs);
			if (udfs != null && !udfs.isEmpty()) {
				List<UdfInfo> udfsInfo = new ArrayList<>();
				for(QsUdf udf : udfs) {
					UdfInfo udfInfo = new UdfInfo();
					udfInfo.setUdfId(udf.getUdfId());
					udfInfo.setProjectId(udf.getProjectId());
					udfInfo.setUserId(udf.getUserId());
					udfInfo.setUdfName(udf.getUdfName());
					udfInfo.setUdfSyntax(udf.getUdfSyntax());
					udfInfo.setUdfFilePath(udf.getUdfFilepath());
					udfInfo.setUdfIconPath(udf.getUdfIconpath());
					udfInfo.setUdfScriptLanguage(udf.getUdfScriptlanguage());
					udfInfo.setUdfVersion(udf.getUdfVersion());
					udfInfo.setArguments(gson.fromJson(udf.getArguments(), new TypeToken<ArrayList<UdfReqColumn>>(){}.getType()));
					udfInfo.setActive(udf.isActive());
					udfInfo.setCreatedDate(udf.getCreatedDate());
					udfInfo.setModifiedDate(udf.getModifiedDate());
					udfInfo.setCreatedBy(udf.getCreatedBy());
					udfInfo.setModifiedBy(udf.getModifiedBy());
					udfInfo.setUdfPublish(udf.isUdfPublish());
					udfInfo.setUdfReturnvalue(udf.getUdfReturnvalue());
					udfsInfo.add(udfInfo);
				}
				response = returnResInstance(HttpStatus.OK, "UDF's for a project returned successfully.",
						udfsInfo);
			} else {
				response = returnResInstance(HttpStatus.BAD_REQUEST,
						"Failed to fetch the UDF's for a project.", null);
			}
		} catch (final Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
		}
		return response;
	}

	@ApiOperation(value = "Fetch User Defined Function by Id", response = Json.class)
	@GetMapping("/api/v1/udf/{projectId}/{userId}/{udfId}")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "UDF details returned successfully."),
			@ApiResponse(code = 400, message = "Failed to fetch the UDF details."),
			@ApiResponse(code = 500, message = "Internal error occured.")})
	public ResponseEntity<Object> getUdf(@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId, @PathVariable(value = "udfId") final int udfId) {
		ResponseEntity<Object> response = null;
		try {
			helper.swtichToPublicSchema();
			QsUdf udf = udfService.getByUdfIdAndProjectIdAndUserId(udfId, projectId, userId);
			log.info("Udf Info :: for udfs {}", udf);
			if (udf != null) {
				UdfInfo udfInfo = new UdfInfo();
				udfInfo.setUdfId(udf.getUdfId());
				udfInfo.setProjectId(udf.getProjectId());
				udfInfo.setUserId(udf.getUserId());
				udfInfo.setUdfName(udf.getUdfName());
				udfInfo.setUdfSyntax(udf.getUdfSyntax());
				udfInfo.setUdfScript(getUdfDefFromS3(udf));
				udfInfo.setUdfFilePath(udf.getUdfFilepath());
				udfInfo.setUdfIconPath(udf.getUdfIconpath());
				udfInfo.setUdfScriptLanguage(udf.getUdfScriptlanguage());
				udfInfo.setUdfVersion(udf.getUdfVersion());
				udfInfo.setArguments(gson.fromJson(udf.getArguments(), new TypeToken<ArrayList<UdfReqColumn>>(){}.getType()));
				udfInfo.setActive(udf.isActive());
				udfInfo.setCreatedDate(udf.getCreatedDate());
				udfInfo.setModifiedDate(udf.getModifiedDate());
				udfInfo.setCreatedBy(udf.getCreatedBy());
				udfInfo.setModifiedBy(udf.getModifiedBy());
				udfInfo.setUdfPublish(udf.isUdfPublish());
				udfInfo.setUdfReturnvalue(udf.getUdfReturnvalue());

				response = returnResInstance(HttpStatus.OK, "UDF info returned successfully.", udfInfo);
			} else {
				response = returnResInstance(HttpStatus.BAD_REQUEST, "Failed to fetch the UDF info.", null);
			}

		} catch (final Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
		}
		return response;
	}

	@ApiOperation(value = "UDF", notes = "Create a UDF by sending request json in body", response = Json.class)
	@PostMapping("/api/v1/udf")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "UDF created successfully"),
			@ApiResponse(code = 400, message = "Error creating the UDF")})
	public ResponseEntity<Object> insertUdf(@RequestBody UdfInfo udfInfo) {
		ResponseEntity<Object> response = null;
		log.info("Received create User Defined Function request with data: {}", udfInfo);
		try {
			helper.swtichToPublicSchema();
			final QsUserV2 userObj = userServiceV2.getUserById(udfInfo.getUserId());
			QsUdf qsUdf = new QsUdf();
			qsUdf.setProjectId(udfInfo.getProjectId());
			qsUdf.setUserId(udfInfo.getUserId());
			qsUdf.setUdfName(udfInfo.getUdfName());
			qsUdf.setUdfSyntax(udfInfo.getUdfSyntax());
			qsUdf.setUdfIconpath(udfInfo.getUdfIconPath());
			qsUdf.setUdfScriptlanguage(udfInfo.getUdfScriptLanguage());
			qsUdf.setUdfVersion(udfInfo.getUdfVersion());
			qsUdf.setArguments(getGson(udfInfo.getArguments()));
			qsUdf.setUdfPublish(udfInfo.isUdfPublish());
			qsUdf.setUdfReturnvalue(udfInfo.getUdfReturnvalue());
			qsUdf.setActive(true);
			qsUdf.setCreatedBy(userObj.getQsUserProfile().getUserFirstName() + " " + userObj.getQsUserProfile().getUserLastName());
			qsUdf.setCreatedDate(DateTime.now().toDate());

			QsUdf qsUdfObj = udfService.save(qsUdf);
			log.info("Udf Info :: before saved with filepath{}", qsUdfObj);
			if (qsUdfObj != null) {		
				
				  qsUdfObj.setUdfFilepath(saveUdfDefInS3(qsUdfObj, udfInfo.getUdfScript()));
				  qsUdfObj = udfService.save(qsUdfObj);
				  log.info("Udf Info :: after saved with file path{}", qsUdfObj);
				response = returnResInstance(HttpStatus.OK, "UDF created successfully.", null);
			}else {
				response = returnResInstance(HttpStatus.BAD_REQUEST, "Failed to save the UDF.", null);
			}    
		} catch (Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create UDF because: " + e.getMessage(), null);
		}
		return response;
	}

	@ApiOperation(value = "Delete a UDF", response = Json.class, notes = "Delete a UDF")
	@DeleteMapping("/api/v1/udf/{projectId}/{userId}/{udfId}")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, message = "UDF deleted successfully"),
					@ApiResponse(code = 404, message = "UDF not found")
			})
	public ResponseEntity<Object> deleteUdf(
			@PathVariable(value = "projectId") final int projectId,
			@PathVariable(value = "userId") final int userId,
			@PathVariable(value = "udfId") final int udfId) {
		ResponseEntity<Object> response = null;
		try {
			helper.swtichToPublicSchema();
			QsUdf qsUdf = udfService.getByUdfIdAndProjectIdAndUserId(udfId, projectId, userId);
			if(qsUdf != null) {
				final QsUserV2 userObj = userServiceV2.getUserById(userId);
				qsUdf.setActive(false);
				qsUdf.setModifiedBy(userObj.getQsUserProfile().getUserFirstName() + " "+ userObj.getQsUserProfile().getUserLastName());
				qsUdf.setModifiedDate(DateTime.now().toDate());

				QsUdf qsUdfSaved = udfService.save(qsUdf);
				log.info("Udf Info :: deleted udf{}", qsUdfSaved);
				if (qsUdfSaved != null) {
					response = returnResInstance(HttpStatus.OK, "UDF deleted successfully.", null);
				}else {
					response = returnResInstance(HttpStatus.CONFLICT, "Failed to delete the Udf.", null);
				}
			}
		} catch (Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete UDF because: " + e.getMessage(), null);
		}
		return response;
	}

	@ApiOperation(value = "update a Udf", response = Json.class, notes = "update a udf")
	@PutMapping("/api/v1/udf")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, message = "Udf updated successfully"),
					@ApiResponse(code = 422, message = "Error updating udf")
			})
	public ResponseEntity<Object> updateUdf(@RequestBody UdfInfo udfInfo) {
		ResponseEntity<Object> response = null;
		try {
			helper.swtichToPublicSchema();
			QsUdf qsUdf = udfService.getByUdfIdAndProjectIdAndUserId(udfInfo.getUdfId(), udfInfo.getProjectId(), udfInfo.getUserId());
			log.info("Udf Info :: get the udf{} ", qsUdf);
			if(qsUdf != null) {
				final QsUserV2 userObj = userServiceV2.getUserById(udfInfo.getUserId());
				qsUdf.setProjectId(udfInfo.getProjectId());
				qsUdf.setUserId(udfInfo.getUserId());
				qsUdf.setUdfName(udfInfo.getUdfName());
				qsUdf.setUdfSyntax(udfInfo.getUdfSyntax());
				qsUdf.setUdfIconpath(udfInfo.getUdfIconPath());
				qsUdf.setUdfScriptlanguage(udfInfo.getUdfScriptLanguage());
				qsUdf.setUdfVersion(udfInfo.getUdfVersion());
				qsUdf.setArguments(getGson(udfInfo.getArguments()));
				qsUdf.setUdfPublish(udfInfo.isUdfPublish());
				qsUdf.setUdfReturnvalue(udfInfo.getUdfReturnvalue());

				qsUdf.setModifiedBy(userObj.getQsUserProfile().getUserFirstName() + " "+ userObj.getQsUserProfile().getUserLastName()); 
				qsUdf.setModifiedDate(DateTime.now().toDate());

				QsUdf qsUdfSaved = udfService.save(qsUdf);
				log.info("Udf Info :: updated the udfInfo{} ", qsUdfSaved);
				if (qsUdfSaved != null) {
					qsUdfSaved.setUdfFilepath(saveUdfDefInS3(qsUdfSaved, udfInfo.getUdfScript()));
					qsUdfSaved = udfService.save(qsUdfSaved);
					response = returnResInstance(HttpStatus.OK, "UDF updated successfully.", null);
				}else {
					response = returnResInstance(HttpStatus.BAD_REQUEST, "Failed to update the UDF.", null);
				}
			}else {
				response = returnResInstance(HttpStatus.NOT_FOUND, "UDF not found.", null);
			}
		}catch (Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update the UDF because: " + e.getMessage(), null);
		}
		return response;
	}
	
	public String saveUdfDefInS3(QsUdf qsUdfObj, String udfScript) {
		File tmpUdfFile = null;
		BufferedWriter bwUdf = null;
		String s3FileUploadRes = null;
		try {

			String fileNameWithoutExt = qsUdfObj.getUdfName();
			String tmpUdfFilePath = String.format("%s/%s/%s/%s/%s.py", System.getProperty("java.io.tmpdir"), qsUdfObj.getProjectId(), qsUdfObj.getUserId(), qsUdfObj.getUdfId(), fileNameWithoutExt);
			log.info("Udf File path :: FilePath {}", tmpUdfFilePath);
			tmpUdfFile = new File(tmpUdfFilePath);
			if (tmpUdfFile != null) {
				if(!tmpUdfFile.getParentFile().exists()) {
					tmpUdfFile.getParentFile().mkdirs();
			    }
				bwUdf = new BufferedWriter(new FileWriter(tmpUdfFile));
				bwUdf.write(udfScript);
			}
			
			bwUdf.flush();
			s3FileUploadRes = awsAdapter.storeUdfDefInS3Async(udfBucketName, tmpUdfFile, (qsUdfObj.getProjectId()+"/"+fileNameWithoutExt+"."+QsConstants.UDF_FILE_EXT), "text/x-python");
			log.info("Uploaded udf file to destination : {}", s3FileUploadRes);

			if(tmpUdfFile != null) {
				tmpUdfFile.delete();
			}

		}catch(Exception e) {
			log.error("Error {} ", e.getMessage());
		}
      return s3FileUploadRes;
	}
	
	public String getUdfDefFromS3(QsUdf qsUdfObj) {
		S3Object s3Object = null;
		StringBuilder sbUdfObj = new StringBuilder();
		try {
			//Get the UDF script from s3 location
			s3Object = awsAdapter.fetchObject(udfBucketName, qsUdfObj.getProjectId()+"/"+new File(qsUdfObj.getUdfFilepath()).getName());
			if(s3Object != null) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
					char[] theChars = new char[128];
					int charsRead = br.read(theChars, 0, theChars.length);
					while(charsRead != -1) {
						sbUdfObj.append(new String(theChars, 0, charsRead));
						charsRead = br.read(theChars, 0, theChars.length);
					}  
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Exception e) {
			log.error("Error {} ", e.getMessage());
		}
		return sbUdfObj.toString();
	}
}
