/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.constants;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public class QsConstants {

  public static final String CODE = "###$CODE;";
  public static final String DEF = "###$DEF;";
  public static final String COL_ARRAY = "$COL_ARRAY";
  public static final String FILTER_CONDITION = "$FILTER_CONDITION";
  public static final String PATH_SEP = "/";
  public static final String DB_NAME = "$DB_NAME";
  public static final String DB_TABLE = "$DB_TABLE";
  public static final String CSV_FILE_NAME = "$CSV_FILE_NAME";
  public static final String PARTITION_NAME = "$PARTITION_NAME";
  public static final String SERVICE_SERVER_URL_PREFIX = "$URL_PREFIX";
  public static final String HYPHEN = "-";
  public static final String PROCESSED = "processed";
  public static final String ENGINEERED = "engineered";
  public static final String ENG = "engresult";
  public static final String QS_DEFAULT_TENANT_ID = "public";
  public static final String QS_EMAIL = "email";
  
  public static final String QS_ORG_NAME = "orgName";
  public static final String QS_OUT_COME = "selectedOutcome";
  public static final String QS_PRJ_DESC = "projectDesc";
  public static final String QS_PRJ_NAME = "projectName";
  public static final String QS_SELECT_AUT = "selectedAutomation";
  public static final String QS_SELECT_DSET = "selectedDataset";
  public static final String QS_SELECT_ENGG = "selectedEngineering";
  public static final String QS_USER_ID = "userId";
  public static final String RAW = "raw";
  public static final String S3_OUT_PUT_PATH = "$s3_OUT_PUT_PATH";
  public static final String UNDERSCORE = "_";
  public static final String JOIN = "join";
  public static final String FILE_OP = "file";
  public static final String AGG = "agg";
  public static final String DB_OP = "db";
  public static final String ENG_OP = "eng";
  public static final String PUBLIC = "public";
  public static final String VIEW = "_view";
  public static final String UDF = "udf";
  public static final String[] PREDEFINED_SCHEMAS = {
    "qs_emp", "qs_epl", "qs_football", "qs_ops", "qs_sales", "qs_tech", "qs_tennis"
  };
  private static final String crawler = "crawler";
  public static final String PROCESSED_CRAWL = "_processed" + crawler + "_";
  public static final String RAW_CRAWL = "_raw" + crawler + "_";
  public static final String ENG_CRAWL = "_eng" + crawler + "_"; 
  
  private static final String db = "db";
  public static final String PROCESSED_DB = "_processed" + db + "_";
  public static final String RAW_DB = "_raw" + db + "_";
  public static final String ENG_DB = "_eng" + db + "_";
  
  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String ADMIN_ROLE = "Admin";
  
  public static final String FILTERROWS = "filterRows";
  public static final String TOP_ROWS = "TopRows";
  public static final String RANGE_ROWS = "RangeRows";
  public static final String TOP_ROWS_AT_REG_INTERVAL = "TopRowsAtRegularInterval";
  
  public static final String DEFAULT = "default";
  public static final String COLUMNS = "columns";
  public static final String BYCOLUMN = "ByColumn";

  // Mail constants...
  public static final String MAIL_FROM_ADDRESS = "support@quantumics.ai";
  public static final String MAIL_SUBJECT = "Welcome to Quantumics.ai";
  public static final String MAIL_WELCOME_NOTE = "Welcome to Quantumics.ai, please use below login credentials to login:";  
  public static final String MAIL_RESET_PASSWORD_NOTE = "Please click on the below link to reset your password:";
  
  // DELIMITER
  public static final String DELIMITER = ",";
  public static final String DELIMITER_SPLIT_PATTERN = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
  public static final String DELIMITER_TAB_SPLIT_PATTERN = "\t(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
  public static final String DELIMITER_PIPE_SPLIT_PATTERN = "\\|(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
  public static final String S3_FILE_URL = "S3_FILE_URL";
  
  // ANALYTICS Constants
  public static final String PII_DETECTION = "pii";
  public static final String PII_PYTHON_FILE_REL_LOC = "ai/quantumics/ml/PII_Detection.py";
  public static final String OUTLIERS_DETECTION = "outliers";

  public static final String COLUMN_VAL_FREQ_PYTHON_FILE_REL_LOC = "ai/quantumics/ml/Column_Value_Freq.py";

  public static final String FILE_STATS_PYTHON_FILE_REL_LOC = "ai/quantumics/ml/File_Statistics.py";
  public static final String OUTLIERS_PYTHON_FILE_REL_LOC = "ai/quantumics/ml/Outliers_Detection.py";
  public static final String DELTA_PYTHON_FILE_REL_LOC = "ai/quantumics/ml/Delta_Detection.py";
  public static final String PII_COL_DETECTION_PYTHON_FILE_REL_LOC = "ai/quantumics/ml/PII_Column_Detection.py";
  public static final String QS_LIVY_TEMPLATE_ENG_NAME = "ai/quantumics/ml/etl_livy_template_eng.py";
  public static final String SAVE_XLSX_FILE_PYTHON_SCRIPT_REL_LOC = "ai/quantumics/ml/Write_Xlsx_To_Csv.py";
  public static final String QS_LIVY_TEMPLATE_NAME = "ai/quantumics/ml/etl_livy_template.py";
  
  // Email Template Actions
  public static final String USER_NAME_PLACE_HOLDER = "{USER_NAME}";
  public static final String REST_PWD_LINK_PLACE_HOLDER = "{RESET_PASSWORD_LINK}";
  public static final String USER_EMAIL_ID_PLACE_HOLDER = "{USER_EMAIL_ID}";
  public static final String USER_LOGIN_LINK_PLACE_HOLDER = "{LOGIN_LINK}";
  
  public static final String USER_SIGNUP = "USER_SIGNUP";
  public static final String FORGOT_PSWD = "FORGOT_PSWD";
  
  public static final String CHANGE_PSWD = "CHANGE_PSWD";
  
  public static final String USER_SUBSCRIPTION ="USER_SUBSCRIPTION";
  public static final String ADD_USER = "ADD_USER";
  public static final String UPDATE_PROJECT = "UPDATE_PROJECT";
  public static final String UPDATE_PROJECT_NOTIFICATION = "Quantumics: Project update notification";
  public static final String USER_SIGNUP_SUBJECT = "Quantumics: User signup notification";
  public static final String USER_FORGOT_PSWD_SUBJECT = "Quantumics: User forgot password notification";
  
  public static final String CHANGE_PSWD_SUBJECT = "Password change confirmation on Quantumics.AI";
  
  public static final String ALL_NOTIFS = "all";
  public static final String UNREAD_NOTIFS = "unread";
  public static final String ADMIN = "Admin";
  
  // User Type Constants
  public static final String USER_TYPE_QSAI = "qsai";
  public static final String USER_TYPE_AWS = "aws";
  
  // Redash constants:
  public static final String REDASH_FILE_TYPE_AWS = "aws";
  public static final String REDASH_FILE_TYPE_PGSQL = "pgsql";
  
  public static final String ENG_FLOW_FINAL_EVENT_RESULT_TBL_PREFIX = "qsp_eng_flow_";
  public static final String ENG_FLOW_FINAL_EVENT_RESULT_TBL_PK = ENG_FLOW_FINAL_EVENT_RESULT_TBL_PREFIX+"id";
  
  // Subscription constants:
  public static final String SUBSCRIPTION_MONTHLY = "Monthly";
  public static final String SUBSCRIPTION_ANNUALLY = "Annually";
  public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
  public static final String SUBSCRIPTION_TYPE_DEFAULT = "Trial";
  public static final String SUBSCRIPTION_TYPE_STARTER = "Starter";
  public static final String SUBSCRIPTION_TYPE_BUSINESS = "Business";
  public static final String SUBSCRIPTION_TYPE_ENTERPRISE = "Enterprise";
  public static final String SUBSCRIPTION_TYPE_ENTERPRISE_PLUS = "Enterprise Plus";
  public static final String SUBSCRIPTION_CUMULATIVE_SIZE_BYTES_PROP = "cumulative_size_bytes";
  public static final String SUBSCRIPTION_MAX_FILE_SIZE_BYTES_PROP = "max_file_size";
  /**
   * Encryption Algorithm constants. DO NOT CHANGE
   */
  
  public static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  public static final String RANDOM_SALT = "AcuaIeY5DiQmejROI3a8ECfbDNWes8qs2HjRrJv4T2M8OUkVJnMBxFHUymzXM8ALBFUYbIsVlRZ8D9KhS9ejkAX6LxH8InKx2zWn8y2iGxcsL5ZBEIzdsm2xkP0WGPcv";
  
  // Stripe integration constants...  
  public static final String CARD_NUMBER_KEY = "cardNumberKey";
  public static final String CARD_HOLDER_NAME_KEY = "cardHolderNameKey";
  public static final String CARD_TYPE_KEY = "cardTypeKey";
  public static final String CARD_EXPIRY_DATE_KEY = "cardExpiryDateKey";
  public static final String CARD_CVV_KEY = "cardCvvKey";
  
  // Stripe events DO NOT CHANGE..
  public static final String STRIPE_DEFAULT_SUBSCRIPTION = "Trial";
  public static final String STRIPE_STARTER_SUBSCRIPTION = "Starter";
  public static final String STRIPE_CHARGE_SUCCEEDED = "charge.succeeded";
  public static final String STRIPE_CHARGE_FAILED = "charge.failed";
  public static final String STRIPE_CHARGE_REFUNDED = "charge.refunded";
  public static final String STRIPE_INVOICE_PAYMENT_SUCCEEDED = "invoice.payment_succeeded";
  public static final String STRIPE_INVOICE_PAYMENT_FAILED = "invoice.payment_failed";
  public static final String STRIPE_USER_SUBSC_TYPE_ID = "subscriptionId";
  public static final String STRIPE_USER_SUBSC_TYPE = "subscriptionType";
  public static final String STRIPE_USER_PLAN_TYPE = "planType";
  public static final String STRIPE_USER_PLAN_TYPE_ID = "planTypeId";
  public static final String STRIPE_USER_PLAN_RENEWS_ON = "renewsOn";
  public static final String STRIPE_USER_PLAN_SUBTOTAL = "subTotal";
  public static final String STRIPE_USER_PLAN_TOTAL = "total";
  public static final String STRIPE_USER_PLAN_TAX = "tax";
  public static final String STRIPE_PRICE_ID = "priceId";
  public static final String STRIPE_SUBS_SUCCESS_PATH = "subscription-success";
  public static final String STRIPE_SUBS_CANCELLED_PATH = "billing";
  
  public static final String DATETIME_PATTERN= "dd/MM/yyyy hh:mm:ss";
  public static final String TIME_PATTERN= "hh:mm:ss";
  
  public static final String PERIOD= ".";
  
  // Join Types...
  public static final String JOIN_TYPE_RIGHT = "right";
  public static final String JOIN_TYPE_LEFT = "left";
  
  public static final String AUDIT_FOLDER_MSG = "ingest-folder";
  
  public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final TimeZone utc = TimeZone.getTimeZone("UTC");
  public static final SimpleDateFormat formatter = new SimpleDateFormat(STANDARD_DATE_FORMAT);
  
  public static final String[] NEW_COL_GENERATING_RULES = {"countMatch", "mergeRule", "manageColumns", "extractColumnValues", "split"};
  
  // Cleansing Rule Types
  public static final String COUNT_MATCH = "countMatch";
  public static final String FILL_NULL = "fillNull";
  public static final String SPLIT = "split";
  public static final String EXTRACT_COLUMN_VALUES = "extractColumnValues";
  public static final String FORMAT = "format";
  public static final String MANAGE_COLUMNS = "manageColumns";
  public static final String FILTER_ROWS = "filterRows";
  public static final String FILTER_ROWS_BY_COLUMN = "filterRowsByColumn";
  public static final String UNCATEGORIZED = "uncategorized";
  public static final String UDF_FILE_EXT = "py";
  
  public static final int STORAGE_DIVISOR = 1000;
  
  //RDBMS
  public static final String SCHEMA = "schema";
  public static final String TABLE = "table";
  public static final String METADATE = "metadata";
  
  public static final String EMPTY = " ";
  
  private QsConstants() {}
  
  public static Date getCurrentUtcDate() {
    formatter.setTimeZone(QsConstants.utc);
    
    try {
      return formatter.parse(Instant.now().toString());
    } catch (ParseException e) {
      return DateTime.now(DateTimeZone.UTC).toDate();
    }
  }

  public static Date getUTCDate(String date) throws ParseException {
    SimpleDateFormat format1 = new SimpleDateFormat(QsConstants.STANDARD_DATE_FORMAT);
    format1.setTimeZone(QsConstants.utc);
    Date newDate = format1.parse(date);
    return newDate;
  }
}
