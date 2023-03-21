package com.qs.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class QsConstants {
  
  //  Utility Constants..
  public static final String MESSAGE = "message"; 
  public static final String CODE_STR = "code";
  public static final String RESULT = "result";
  public static final String PUBLIC = "public";
  public static final String BEARER_AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODgzMjE3ODksInVzZXJfbmFtZSI6InF1YW50dW1zcGFyayIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI4NzUxYTZlMi0zZmFiLTRhZWItOGY5MS1jMmExMTkzMmMyNjkiLCJjbGllbnRfaWQiOiJtb2RlbENsaWVudEFwcCIsInNjb3BlIjpbInJlYWRfcHJvZmlsZSJdfQ.-pPHIIJUCRlR31P2pHbkJCS-pYT3_oi24Yi8CkyA4ME";
  
  public static final String CODE = "###$CODE;";
  public static final String COL_ARRAY = "$COL_ARRAY";
  public static final String PATH_SEP = "/";
  public static final String DB_TABLE = "$DB_TABLE";
  public static final String HYPHEN = "-";
  public static final String PROCESSED = "processed";
  public static final String QS_DEFAULT_TENANT_ID = "public";
  public static final String QS_EMAIL = "email";
  public static final String QS_ORG_NAME = "orgName";
  public static final String QS_OUT_COME = "selectedOutcome";
  public static final String QS_PRJ_DESC = "projectDesc";
  public static final String QS_PRJ_NAME = "projectName";
  public static final String QS_PWD = "password";
  public static final String QS_SELECT_AUT = "selectedAutomation";
  public static final String QS_SELECT_DSET = "selectedDataset";
  public static final String QS_SELECT_ENGG = "selectedEngineering";
  public static final String QS_USER_ID = "userId";
  public static final String RAW = "raw";
  public static final String S3_OUT_PUT_PATH = "$s3_OUT_PUT_PATH";
  public static final String UNDERSCORE = "_";
  public static final String UDF = "udf";
  public static final String JOIN = "join";
  public static final String FILE_OP = "file";
  public static final String AGG_OP = "agg";
  public static final String DB_OP = "db";
  public static final String ENG_OP = "eng";
  public static final String ENGINEERED = "engineered";

  private QsConstants() {}

  public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final TimeZone utc = TimeZone.getTimeZone("UTC");
  public static final SimpleDateFormat formatter = new SimpleDateFormat(STANDARD_DATE_FORMAT);

  public static Date getCurrentUtcDate() {
    formatter.setTimeZone(QsConstants.utc);

    try {
      return formatter.parse(Instant.now().toString());
    } catch (ParseException e) {
      return DateTime.now(DateTimeZone.UTC).toDate();
    }
  }
}
