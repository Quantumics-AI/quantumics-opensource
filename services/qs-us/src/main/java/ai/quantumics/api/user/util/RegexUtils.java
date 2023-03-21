package ai.quantumics.api.user.util;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RegexUtils {
  
  public static boolean isAlphaNumeric(String str) {
    return Pattern.matches(RegexPatterns.ALPHA_NUMERIC.getPattern(), str);
  }

  public static boolean isInteger(String str) {
    return Pattern.matches(RegexPatterns.NUMERIC.getPattern(), str);
  }

  public static boolean isLong(String str) {
    return Pattern.matches(RegexPatterns.LONG.getPattern(), str);
  }

  public static boolean isDouble(String str) {
    return Pattern.matches(RegexPatterns.DOUBLE.getPattern(), str);
  }

  public static boolean hasSpecialChars(String str) {
    return Pattern.matches(RegexPatterns.SPECIAL_CHARS.getPattern(), str);
  }

  public static boolean isCreditCardNum(String str) {
    return Pattern.matches(RegexPatterns.CREDIT_CARD_NUM.getPattern(), str);
  }

  public static boolean isCreditCardCvv(String str) {
    return Pattern.matches(RegexPatterns.CREDIT_CARD_CVV.getPattern(), str);
  }

  public static boolean isEmailId(String str) {
    return Pattern.matches(RegexPatterns.EMAIL_ID.getPattern(), str);
  }

  public static boolean isSsn(String str) {
    return Pattern.matches(RegexPatterns.SSN.getPattern(), str);
  }
  
  public static boolean isPassportNum(String str) {
    return Pattern.matches(RegexPatterns.PASSPORT_NUM_FORMAT.getPattern(), str);
  }
  
  public static boolean isPhNumberItut(String str) {
    return Pattern.matches(RegexPatterns.PHONE_NUM_ITUT_FORMAT.getPattern(), str);
  }

  public static boolean isPhNumberEpp(String str) {
    return Pattern.matches(RegexPatterns.PHONE_NUM_EPP_FORMAT.getPattern(), str);
  }

}
