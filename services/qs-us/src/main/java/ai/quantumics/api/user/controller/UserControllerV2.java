
package ai.quantumics.api.user.controller;

import ai.quantumics.api.user.adapter.AwsAdapter;
import ai.quantumics.api.user.constants.ExceptionConstant;
import ai.quantumics.api.user.constants.QsConstants;
import ai.quantumics.api.user.exceptions.UnauthorizedException;
import ai.quantumics.api.user.helper.ControllerHelper;
import ai.quantumics.api.user.model.*;
import ai.quantumics.api.user.req.RefreshTokenRequest;
import ai.quantumics.api.user.req.UserRegRequest;
import ai.quantumics.api.user.security.TokenManager;
import ai.quantumics.api.user.service.*;
import ai.quantumics.api.user.util.DbSessionUtil;
import ai.quantumics.api.user.util.ProtectedUser;
import ai.quantumics.api.user.util.RedashClient;
import ai.quantumics.api.user.vo.RefreshTokenResponse;
import ai.quantumics.api.user.vo.UserInfo;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@RestController
@Api(value = "Quantumics Service API ")
public class UserControllerV2 {

	private static final String EMAIL_VERIFICATION = "_em";

	private final DbSessionUtil dbUtil;
	private final UserServiceV2 userService;
	private final UserProfileServiceV2 userProfileService;
	private final RedashClient redashClient;
	private final ProtectedUser protectedUser;
	private final ProjectService projectService;
	private final UserProjectsService userProjectsService;
	private final SubscriptionService subscriptionService;
	private final TokenManager tokenManager;
	private AwsAdapter awsAdapter;
	private ControllerHelper controllerHelper;
	private CustomerService customerService;


	@Value(value = "${qs.user.account.lock.threshold}")
	private int userAccountLockThreshold;

	@Value("${s3.images.user.bucketName}")
	private String imagesBucketName;


	@Value(value = "${qs.user.username}")
	private String username;

	@Value(value = "${qs.user.password}")
	private String password;

	public UserControllerV2(final DbSessionUtil dbUtilCi, final UserServiceV2 userServiceCi,
							final UserProfileServiceV2 userProfileServiceCi, final RedashClient redashClientCi,
							final ProtectedUser protectedUserCi, final ProjectService projectServiceCi,
							final UserProjectsService userProjectsServiceCi,
							final SubscriptionService subscriptionServiceCi,
							final TokenManager tokenManagerCi, AwsAdapter awsAdapterCi,
							ControllerHelper controllerHelperCi, CustomerService customerServiceCi) {

		dbUtil = dbUtilCi;
		protectedUser = protectedUserCi;
		userService = userServiceCi;
		userProfileService = userProfileServiceCi;
		redashClient = redashClientCi;
		projectService = projectServiceCi;
		userProjectsService = userProjectsServiceCi;
		subscriptionService = subscriptionServiceCi;
		awsAdapter = awsAdapterCi;
		controllerHelper = controllerHelperCi;
		customerService = customerServiceCi;
		tokenManager = tokenManagerCi;
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
	
	@ApiOperation(value = "authorization", response = Json.class, notes = "User Login")
	@PostMapping("/api/v2/authorization")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User authenticated successfully..."),
			@ApiResponse(code = 401, message = "Email and password does not match..."),
			@ApiResponse(code = 404, message = "No user found") })
	public ResponseEntity<Object> qsUserLogin(
			@ApiParam(value = "Expected json of {'email':'emailValue' , 'password':'*****'} In Request Body", required = true) @RequestBody final Map<String, String> params) {
		final HashMap<String, Object> genericResponse = new HashMap<>();
		final LoginStatus loginStatus = new LoginStatus();
		dbUtil.changeSchema("public");
		QsUserV2 user;
		final QsUserProfileV2 userProfile;
		HttpStatus status = null;
		String message = null;
		try {
			user = userService.getUserByEmail(params.get(QsConstants.QS_EMAIL));

			if (user == null) {
				status = HttpStatus.NOT_FOUND;
				message = "User Not Found";
			} else {
				DateTime now = DateTime.now();
				DateTime modifiedDate = null;
				int minutes = 0;
				if (user.getModifiedDate() != null) {
					modifiedDate = DateTime.parse(user.getModifiedDate().toString(),
							DateTimeFormat.forPattern(QsConstants.DATE_TIME_FORMAT));
					minutes = Minutes.minutesBetween(modifiedDate, now).getMinutes();
				}

				if (!user.isActive() && (minutes <= 30)) {
					loginStatus.setAuth("false");
					status = HttpStatus.UNAUTHORIZED;
					message = "Your account is locked after " + userAccountLockThreshold
							+ " unsuccessful attempts. Please try to login into the application after 30 minutes.";
				} else {
					userProfile = userProfileService.getUserProfileById(user.getUserId());
					DateTime currentDate = DateTime.now();
					// Date userValidityTo = user.getUserSubscriptionValidTo();

					final boolean verifyUserPassword = protectedUser.verifyUserPassword(params.get("password"),
							user.getUserPwd(), user.getSalt());

					if (verifyUserPassword) {

						user.setActive(true);
						user.setFailedLoginAttemptsCount(0);
						user.setModifiedDate(currentDate.toDate());


						user = userService.save(user);

						// List of all sub users in case admin role

						// list of all users including parent in case ofsub user
						// update users with expiry date and subscription

						loginStatus.setFirstTimeLogin(user.isFirstTimeLogin());
						loginStatus.setUserRole(user.getUserRole());

						loginStatus.setAuth("true");
						// loginStatus.setToken(getTokenFromAuthServer());
						loginStatus.setToken(tokenManager.generateToken(user));
						loginStatus.setRefreshToken(tokenManager.generateRefreshToken(user));
						loginStatus.setEmail(user.getUserEmail());
						loginStatus.setUserImageUrl(userProfile.getUserImage());
						loginStatus.setMessage("Success... Login successful");
						loginStatus.setUserId(Long.toString(user.getUserId()));
						loginStatus.setUser(getUserName(userProfile.getUserFirstName(), userProfile.getUserLastName()));
						final String redashKey = (StringUtils.isEmpty(user.getUserRedashKey())) ? "NOTOKEN"
								: user.getUserRedashKey();
						loginStatus.setRedashKey(redashKey);
						loginStatus.setUserType(user.getUserType());
						log.debug("Valid QsUser Found - Request sent for JWT Token");

						return ResponseEntity.ok().body(loginStatus);
					} else {
						loginStatus.setAuth("false");
						status = HttpStatus.UNAUTHORIZED;

						int failedAttemptsCount = user.getFailedLoginAttemptsCount();

						log.info("----> Modified Date: {} and Current Date: {} are minutes: {} mins apart.",
								modifiedDate, now, minutes);

						if (failedAttemptsCount >= userAccountLockThreshold && modifiedDate != null) {
							log.info(
									"Number of failed login attempts exceeded the limit, checking for attempts duration...");
							if (minutes > 30) {
								user.setActive(true);
								user.setFailedLoginAttemptsCount(0);
								message = "Email and password does not match.";
								log.info("Email and password does not match.");
							} else if (minutes <= 30) {
								user.setActive(false);
								user.setFailedLoginAttemptsCount(failedAttemptsCount);
								message = "Your account is locked after " + userAccountLockThreshold
										+ " unsuccessful attempts. Please try to login into the application after 30 minutes.";
								log.info(
										"Your account is locked after {} unsuccessful attempts. Please try to login into the application after 30 minutes.",
										userAccountLockThreshold);
							}
						} else {
							user.setFailedLoginAttemptsCount(failedAttemptsCount + 1);
							message = "Email and password does not match.";
							log.info("Email and password does not match.");
						}

						user.setModifiedDate(now.toDate());
						// Update the QS_USER_V2 table with the counts attempt.
						userService.save(user);
					}
				}
			}
		} catch (final SQLException exception) {
			status = HttpStatus.NOT_FOUND;
			message = exception.getMessage();

			log.error("User not found {}", exception.getMessage());
		}

		genericResponse.put("code", status.value());
		genericResponse.put("message", message);

		return ResponseEntity.status(status).body(genericResponse);
	}

	@ApiOperation(value = "To get the user login info", response = Json.class, notes = "User Login info")
	@GetMapping("/api/v2/userinfo/{userId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User found successfully"),
			@ApiResponse(code = 404, message = "User not found") })
	public ResponseEntity<Object> getUserInfo(@PathVariable(name = "userId") int userId) {
		final HashMap<String, Object> genericResponse = new HashMap<>();
		try {
			QsUserV2 user = userService.getActiveUserById(userId);
			if (user == null) {
				genericResponse.put("code", HttpStatus.NOT_FOUND.value());
				genericResponse.put("message", "User Not Found");
			} else {
				QsUserProfileV2 userProfile = user.getQsUserProfile();
				LoginStatus loginStatus = new LoginStatus();
				loginStatus.setFirstTimeLogin(user.isFirstTimeLogin());
				loginStatus.setUserRole(user.getUserRole());
				//loginStatus.setAuth("true");
				//loginStatus.setToken(getTokenFromAuthServer());
				loginStatus.setEmail(user.getUserEmail());
				loginStatus.setUserImageUrl(userProfile.getUserImage());
				loginStatus.setUserId(Long.toString(user.getUserId()));
				loginStatus.setUser(userProfile.getUserFirstName() + " " + userProfile.getUserLastName());
				final String redashKey = (StringUtils.isEmpty(user.getUserRedashKey())) ? "NOTOKEN"
						: user.getUserRedashKey();
				loginStatus.setRedashKey(redashKey);
				loginStatus.setUserType(user.getUserType());
				return ResponseEntity.ok().body(loginStatus);
			}
		} catch (final SQLException exception) {
			genericResponse.put("code", HttpStatus.NOT_FOUND.value());
			genericResponse.put("message", exception.getMessage());
		}
		return ResponseEntity.status(HttpStatus.valueOf((Integer) genericResponse.get("code"))).body(genericResponse);
	}

	@ApiOperation(value = "To get the user login info", response = Json.class, notes = "User Login info")
	@GetMapping("/api/v2/projectuserinfo/{projectId}/{userId}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User found successfully"),
			@ApiResponse(code = 404, message = "User not found") })
	public ResponseEntity<Object> getProjectUserInfo(@PathVariable(name = "projectId") int projectId,
			@PathVariable(name = "userId") int userId) {
		ResponseEntity<Object> response = null;
		final HashMap<String, Object> output = new HashMap<>();

		try {
			dbUtil.changeSchema("public");

			QsUserV2 user = userService.getActiveUserById(userId);
			if (user == null) {
				output.put("code", HttpStatus.NOT_FOUND.value());
				output.put("message", "User Not Found");

				response = returnResInstance(HttpStatus.NOT_FOUND, "User Not Found", "User Not Found");

			} else {
				ProjectSubscriptionDetails projSubDetails = null;
				boolean createProject = false;
				Map<String, String> result = new HashMap<>();

				if (projectId == 0) {
					projSubDetails = subscriptionService.getProjectSubscriptionDetails(
							(user.getUserParentId() == 0) ? user.getUserId() : user.getUserParentId(),
							QsConstants.SUBSCRIPTION_TYPE_DEFAULT, QsConstants.SUBSCRIPTION_STATUS_ACTIVE);

					if (projSubDetails != null) {
						createProject = false;
					} else {
						createProject = true;
					}
					result.put("createProject", Boolean.toString(createProject));
				} else {
					projSubDetails = subscriptionService.getProjectSubscriptionByStatus(projectId,
							(user.getUserParentId() == 0) ? user.getUserId() : user.getUserParentId(),
							QsConstants.SUBSCRIPTION_STATUS_ACTIVE);
					QsSubscription subscription = subscriptionService.getSubscriptionByNameAndPlanType(
							projSubDetails.getSubscriptionType(), projSubDetails.getSubscriptionPlanType());
					result.put("subscriptionType", projSubDetails.getSubscriptionType());
					result.put("noOfUsers", Integer.toString(subscription.getAccounts()));
				}

				response = returnResInstance(HttpStatus.OK, "User subscription details returned successfully.", result);
			}
		} catch (final SQLException exception) {
			response = returnResInstance(HttpStatus.NOT_FOUND, exception.getMessage(), exception.getMessage());
		}
		return response;
	}

	private boolean isEmpty(String str) {
		return Objects.isNull(str) || str.isEmpty();
	}


	//@PostConstruct
	public ResponseEntity<Object> createUser() {
		UserRegRequest userRegReq = new UserRegRequest();
		userRegReq.setUserFirstName("Test");
		userRegReq.setUserLastName("Test");
		userRegReq.setUserEmail(username);
		userRegReq.setUserPwd(password);
		userRegReq.setUserCompany("qsai");
		userRegReq.setUserRole("Admin");

		ResponseEntity<Object> response = null;
		System.out.println("User Creation starting now");
		try {
			dbUtil.changeSchema("public");
			log.info("Received the request for User creation: {}", userRegReq);

			if (userRegReq.getUserEmail() == null || userRegReq.getUserEmail().isEmpty())
				return returnResInstance(HttpStatus.FAILED_DEPENDENCY, "Email Id is mandatory.",
						"Email Id is mandatory.");

			if (QsConstants.ADMIN_ROLE.equals(userRegReq.getUserRole()) && isEmpty(userRegReq.getUserPwd()))
				return returnResInstance(HttpStatus.FAILED_DEPENDENCY, "User password is mandatory.",
						"User password is mandatory.");

			if (userService.isExists(userRegReq.getUserEmail())) {
				response = returnResInstance(HttpStatus.CONFLICT, "Email already exists.", "Email already exists.");

				return response;
			}
			DateTime currentTime = DateTime.now();
			QsUserV2 createUser = null;
			String userName = getUserName(userRegReq.getUserFirstName(), userRegReq.getUserLastName(),
					userRegReq.getUserEmail());
			log.info("User name is: {}", userName);
			if (userRegReq.getUserRole() != null) {
				if (QsConstants.ADMIN_ROLE.equals(userRegReq.getUserRole())) {
						QsSubscription subscription = subscriptionService.getSubscriptionByNameAndPlanType(
								QsConstants.STRIPE_DEFAULT_SUBSCRIPTION, QsConstants.SUBSCRIPTION_MONTHLY);

						if (subscription == null) {
							System.out.println("Subscription is empty");
						}
						createUser = buildUser(subscription, userRegReq);
						createUser.setUserSubscriptionValidTo(
								currentTime.plusDays(subscription.getValidityDays()).toDate());
						createUser.setUserType(QsConstants.USER_TYPE_QSAI);

				}
			} else {
				response = returnResInstance(HttpStatus.NOT_FOUND, "User don't have a valid Role",
						"User don't have a valid Role");
			}
			System.out.println("RedashUsserCreating");
			String apiToken = null;
			try {
				redashClient.createRedashRootUser();
				/*try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}*/
				apiToken = redashClient.createRedashUser(userRegReq.getUserEmail(), userRegReq.getUserEmail(),
						createUser.getUserPwd());

				log.info("Redash User is created and token is generated...");
			} catch (Exception e) {
				log.error("Failed to create redash user");
			}
			System.out.println("apiToken is" + apiToken);
			log.info("Redash User is created and token is generated...");

			createUser.setUserRedashKey(apiToken);
			createUser.setActive(true);
			createUser.setFirstTimeLogin(true);

			final QsUserV2 user = userService.save(createUser);

			// Save User Profile Info as well..
			QsUserProfileV2 userProfile = new QsUserProfileV2();
			userProfile.setUserId(user.getUserId());
			userProfile.setUserPhone(userRegReq.getUserPhone());
			userProfile.setUserFirstName(userRegReq.getUserFirstName());
			userProfile.setUserLastName(userRegReq.getUserLastName());
			userProfile.setCreationDate(currentTime.toDate());
			userProfile.setCreatedBy(userName);

			final boolean save = userProfileService.save(userProfile);
			if (QsConstants.ADMIN_ROLE.equals(user.getUserRole())) {
				if (QsConstants.USER_TYPE_QSAI.equalsIgnoreCase(createUser.getUserType())) {
					customerService.save(buildQsaiCustomerInfo(user));
				}
			}

			// Now save a record in qs_userprojects table in case, the Project Id is not
			// null || 0 || -1.
			if (userRegReq.getProjectId() > 0) {

				Optional<Projects> projectOp = projectService.getProjectOp(userRegReq.getProjectId());
				if (projectOp.isPresent()) {
					Projects project = projectOp.get();
					QsUserProjects userProject = new QsUserProjects();
					userProject.setUserId(user.getUserId()); // User id of the newly created user.
					userProject.setProjectId(project.getProjectId());

					DateTime now = DateTime.now();
					userProject.setCreationDate(now.toDate());
					userProject.setCreatedBy(userName);
					userProject = userProjectsService.save(userProject);

				} else {
					log.warn("Project with Id: {} not found. Failed to associate the current user with the project.",
							userRegReq.getProjectId());
				}
			}

		} catch (final Exception exception) {
			exception.printStackTrace();
			log.error("Exception During Create User: {}", exception.getMessage());
			response = returnResInstance(HttpStatus.CONFLICT, exception.getMessage(), exception.getMessage());
		}
		System.out.println("User Created");
		return response;
	}

	private QsUserV2 buildUser(QsSubscription subscription, UserRegRequest userRegReq) {
		DateTime currentTime = DateTime.now();
		String salt = userRegReq.getUserPwd() == null ? protectedUser.getDefaultSalt() : protectedUser.getSalt();
		return QsUserV2.builder().salt(salt).userEmail(userRegReq.getUserEmail())
				.userPwd(protectedUser.generateSecurePassword(userRegReq.getUserPwd(), salt))
				.userRole(userRegReq.getUserRole()).userSubscriptionType(subscription.getName())
				.userSubscriptionValidFrom(currentTime.toDate())
				.userSubscriptionTypeId(subscription.getSubscriptionId()).build();
	}

	private QsaiCustomerInfo buildQsaiCustomerInfo(QsUserV2 user) {
		DateTime currentTime = DateTime.now();
		return QsaiCustomerInfo.builder().createdDate(currentTime.toDate()).modifiedDate(currentTime.toDate())
				.userId(user.getUserId()).build();
	}


	@ApiOperation(value = "users", response = Json.class, notes = "Update specific user")
	@PutMapping(value = "/api/v2/users/{userId}", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User Profile updated successfully..."),
			@ApiResponse(code = 409, message = "User Update Failed!") })
	public ResponseEntity<Object> updateQsUser(@PathVariable("userId") final int userId,
			@RequestBody final UserRegRequest updateUser) {

		ResponseEntity<Object> response = null;
		try {
			dbUtil.changeSchema("public");

			QsUserProfileV2 userObj = userProfileService.getUserProfileById(userId);
			getUserToUpdate(userObj, updateUser);

			final boolean save = userProfileService.save(userObj);
			if (save) {
				response = returnResInstance(HttpStatus.OK, "User Profile updated successfully.", null);
			} else {
				response = returnResInstance(HttpStatus.CONFLICT, "Failed to update the User Profile.", null);
			}
		} catch (final Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.EXPECTATION_FAILED, e.getMessage(), null);
		}

		return response;
	}

	@ApiOperation(value = "users", response = Json.class, notes = "Fetch specific user")
	@GetMapping(value = "/api/v2/users/{userId}", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User details returned successfully..!"),
			@ApiResponse(code = 400, message = "User not found!") })
	public ResponseEntity<Object> getQsUser(@PathVariable("userId") final int userId) {

		ResponseEntity<Object> response = null;
		try {

			log.info("Get User request for User Id: {}", userId);
			dbUtil.changeSchema("public");

			final QsUserV2 userObj = userService.getUserById(userId);
			final QsUserProfileV2 userProfile = userProfileService.getUserProfileById(userId);

			if (userObj != null && userProfile != null) {
				UserInfo userTobeReturned = new UserInfo();
				userTobeReturned.setUserId(userObj.getUserId());
				userTobeReturned.setCompany(userProfile.getUserCompany());
				userTobeReturned.setCompanyRole(userProfile.getUserCompanyRole());
				userTobeReturned.setEmail(userObj.getUserEmail());
				userTobeReturned.setFirstName(userProfile.getUserFirstName());
				userTobeReturned.setLastName(userProfile.getUserLastName());
				userTobeReturned.setRole(userObj.getUserRole());
				userTobeReturned.setPhone(userProfile.getUserPhone());
				userTobeReturned.setCountry(userProfile.getUserCountry());
				userTobeReturned.setUserImage(userProfile.getUserImage());
				response = returnResInstance(HttpStatus.OK, "User details returned successfully.", userTobeReturned);
			} else {
				response = returnResInstance(HttpStatus.BAD_REQUEST, "User not found.", null);
			}
		} catch (final Exception e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
		}

		return response;
	}


	@ApiOperation(value = "Uploads the user display picture", response = Json.class, notes = "Uploads the user display picture")
	@PostMapping(value = "/api/v1/uploadImage/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User image is been uploaded successfully"),
			@ApiResponse(code = 500, message = "Error while uploading the image") })
	public ResponseEntity<Object> uploadImage(@PathVariable(value = "userId") final int userId,
			@RequestPart(value = "file") final MultipartFile userImage) {
		ResponseEntity<Object> uploadResponse = null;
		try {
			dbUtil.changeSchema("public");
			QsUserProfileV2 userObj = userProfileService.getUserProfileById(userId);
			String userImageUrl = "";
			if (userImage != null) {
				userImageUrl = awsAdapter.storeImageInS3Async(imagesBucketName, userImage,
						userId + QsConstants.PATH_SEP + userImage.getOriginalFilename(), userImage.getContentType());
			}

			boolean save = false;
			if (!userImageUrl.isEmpty()) {
				userObj.setUserImage(userImageUrl);
				userObj.setModifiedDate(DateTime.now().toDate());
				save = userProfileService.save(userObj);
			}

			if (save) {
				uploadResponse = returnResInstance(HttpStatus.OK, "User image is been uploaded successfully",
						userImageUrl);
			} else {
				throw new Exception("Error while saving the image url");
			}

		} catch (final Exception exc) {
			uploadResponse = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Error while uploading the image",
					exc.getMessage());
			log.error("ALERT - Image upload failed {}", exc.getMessage());
		}
		return ResponseEntity.ok().body(uploadResponse);

	}


	@ApiOperation(value = "Validate token", response = Json.class, notes = "Validate token")
	@GetMapping("/api/v2/authorization/validateToken")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Authorized"),
			@ApiResponse(code = 401, message = "Unauthorized user"),
			@ApiResponse(code = 500, message = "Internal Server Error.")})
	public ResponseEntity<Object> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
		ResponseEntity<Object> response = null;
		try {
			if (org.apache.commons.lang3.StringUtils.isEmpty(token)) {
				throw new UnauthorizedException(ExceptionConstant.UNAUTHORIZED_USER);
			}
			token = token.substring(7);
			if (!tokenManager.validateToken(token)) {
				throw new UnauthorizedException(ExceptionConstant.UNAUTHORIZED_USER);
			}
			response = returnResInstance(HttpStatus.OK, "Authorized user",
					null);
			return response;
		} catch (final SQLException e) {
			log.error("Error {} ", e.getMessage());
			response = returnResInstance(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
		}
		return response;
	}

	@ApiOperation(value = "Refresh Token", response = Json.class, notes = "Refresh Token")
	@PostMapping("/api/v2/authorization/refreshToken")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "New access token created"),
			@ApiResponse(code = 401, message = "Unauthorized user"),
			@ApiResponse(code = 500, message = "Internal Server Error.") })
	public ResponseEntity<Object> refreshToken(
			@ApiParam(value = "Expected json of {'refreshToken':'refreshToken'} In Request Body", required = true) @RequestBody final RefreshTokenRequest tokenRequest) throws Exception{
		final HashMap<String, Object> genericResponse = new HashMap<>();
		HttpStatus status = null;
		String message = null;
		try {
			if (tokenRequest == null || org.apache.commons.lang3.StringUtils.isEmpty(tokenRequest.getRefreshToken())) {
				throw new UnauthorizedException(ExceptionConstant.UNAUTHORIZED_USER);
			}
			RefreshTokenResponse refreshTokenResponse = tokenManager.refreshAccessToken(tokenRequest.getRefreshToken());
			if(!refreshTokenResponse.isSuccess()) {
				throw new UnauthorizedException(ExceptionConstant.UNAUTHORIZED_USER);
			}
			return ResponseEntity.ok().body(refreshTokenResponse);
		} catch (final SQLException exception) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			message = exception.getMessage();
			log.error("Error {}", exception.getMessage());
		}
		genericResponse.put("code", status.value());
		genericResponse.put("message", message);
		return ResponseEntity.status(status).body(genericResponse);
	}

	private void getUserToUpdate(QsUserProfileV2 originalUserProfile, final UserRegRequest updatedUser) {
		originalUserProfile.setUserFirstName(updatedUser.getUserFirstName());
		originalUserProfile.setUserLastName(updatedUser.getUserLastName());
		originalUserProfile.setUserPhone(updatedUser.getUserPhone());
		originalUserProfile.setUserCompany(updatedUser.getUserCompany());
		originalUserProfile.setUserCountry(updatedUser.getUserCountry());
		originalUserProfile.setUserCompanyRole(updatedUser.getCompanyRole());
	}


	/*private GetEntitlementsResult getAWSUserEntitle(String productCode, String customerIdentifier) throws Exception {
		log.info("productCode :{}, customerIdentifier :{}", productCode, customerIdentifier);
		GetEntitlementsRequest getEntitlementsRequest = new GetEntitlementsRequest();
		getEntitlementsRequest.setProductCode(productCode);
		List<String> list = new ArrayList<>();
		list.add(customerIdentifier);
		getEntitlementsRequest.addFilterEntry("CUSTOMER_IDENTIFIER", list);
		//getEntitlementsRequest.setMaxResults(1);
		return awsMPService.getEntitlement(getEntitlementsRequest);

	}*/

	private void updateProjectSubscrition(QsUserV2 user) throws SQLException {
		List<ProjectSubscriptionDetails> projectSubscriptionDetailsList = subscriptionService
				.getProjectSubscriptionDetailsByUser(user.getUserId());
		for (ProjectSubscriptionDetails projectSubscriptionDetails : projectSubscriptionDetailsList) {
			projectSubscriptionDetails.setSubscriptionStatus("active");
			projectSubscriptionDetails.setInvoiceId(null);
			projectSubscriptionDetails.setSubscriptionValidFrom(user.getUserSubscriptionValidFrom());
			projectSubscriptionDetails.setSubscriptionValidTo(user.getUserSubscriptionValidTo());
			projectSubscriptionDetails.setSubscriptionType(user.getUserSubscriptionType());
			projectSubscriptionDetails.setSubscriptionPlanTypeId(user.getUserSubscriptionTypeId());
			//projectSubscriptionDetails.setSubscriptionPlanType(user.getUserSubscriptionPlanType());
			projectSubscriptionDetails.setChargeStatus("AWS");
			subscriptionService.saveProjectSubscriptionDetails(projectSubscriptionDetails);
		}

	}

	@SuppressWarnings("unused")
	private String getFinalValue(String originalVal, String modifiedVal) {
		return (isNullOrEmpty(modifiedVal) ? originalVal : modifiedVal);
	}

	private boolean isNullOrEmpty(String str) {
		return (str == null || str.isEmpty());
	}

	private String getUserName(String firstName, String lastName, String emailId) {
		return (firstName != null && lastName != null) ? (firstName + " " + lastName) : emailId;
	}

	private String getUserName(String firstName, String lastName) {
		return (lastName != null) ? (firstName + " " + lastName) : firstName;
	}

}
