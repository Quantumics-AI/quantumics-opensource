package ai.quantumics.api.util;

public enum RegexPatterns {
  ALPHA_NUMERIC("^[a-zA-Z0-9_-]+$"),
  NUMERIC("^-?[0-9]{1,5}$"),
  LONG("^-?[0-9]{6,12}$"),
  DOUBLE("^-?[0-9]{1,12}.?[0-9]{1,6}$"),
  SPECIAL_CHARS("\\W"),
  
  // Supported CC types are: Visa & Visa Electron, Mastercard, Amex, JCB, Diners Club - International, Diners Club - USA & Canada, Diners Club - Carte Blanche 
  CREDIT_CARD_NUM("^4\\d{15}|5[1-5]\\d{14}|3[47]\\d{13}|36\\d{12}|54\\d{14}|30[0-5]\\d{11}$"),
  CREDIT_CARD_CVV("^[0-9]{3,4}$"),
  EMAIL_ID("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"), // Regex permitted by RFC5322
  SSN("^(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$"),
  PASSPORT_NUM_FORMAT("^\\d{10}GB[P,R]\\d{7}[U,M,F]{1}\\d{9}|\\w{1}\\d{7}$"),
  PHONE_NUM_ITUT_FORMAT("^\\+(?:\\d ?){6,14}\\d$"), // ITUT standard
  PHONE_NUM_EPP_FORMAT("^\\+\\d{1,3}\\.\\d{4,14}(?:x.+)?$") // EPP standard
  
  
  ;
  
  private String pattern;
  private RegexPatterns(final String pattern){
      this.pattern = pattern;
  }

  private RegexPatterns() {
  }

  public String getPattern() {
      return pattern;
  }

  public void setPattern(String pattern) {
      this.pattern = pattern;
  }
}
