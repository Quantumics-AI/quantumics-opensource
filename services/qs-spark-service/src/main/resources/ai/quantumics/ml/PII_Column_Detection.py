import pandas as pd
import re
import sys
import json

if sys.version_info[0] < 3: 
    from StringIO import StringIO # Python 2.x
else:
    from io import StringIO # Python 3.x
    
def processDataFrame(input_data):
    #df = pd.DataFrame(data=input_data[1:], columns=[input_data[0]])
    df = pd.read_json(input_data, orient='records')
    #Regex for every pii 
    pii_pattern = {
           '((?:(?:\\d{4}[- ]?){3}\\d{4}|\\d{15,16}))(?![\\d])':'credit_card',
           '^4[0-9]{12}(?:[0-9]{3})?$':'credit_card_visa',
          '^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$':'credit_card_master',
          '\d{1,4} [\w\s]{1,20}(?:street|st|avenue|ave|road|rd|highway|hwy|square|sq|trail|trl|drive|dr|court|ct|park|parkway|pkwy|circle|cir|boulevard|blvd)\W?(?=\s|$)':'street_addres',
          '^3[47][0-9]{13}$':'credit_card_amex',
          '^3(?:0[0-5]|[68][0-9])[0-9]{11}$':'credit_card_diners',
          '^6(?:011|5[0-9]{2})[0-9]{12}$':'credit_card_discover',
          '^(?:2131|1800|35\d{3})\d{11}$':'credit_card_jcb',
          '^(5018|5081|5044|5020|5038|603845|6304|6759|676[1-3]|6799|6220|504834|504817|504645)[0-9]{8,15}$':'credit_card_maestro',
          '''((?:(?<![\d-])(?:\+?\d{1,3}[-.\s*]?)?(?:\(?\d{3}\)?[-.\s*]?)?\d{3}[-.\s*]?\d{4}(?![\d-]))|(?:(?<![\d-])(?:(?:\(\+?\d{2}\))|(?:\+?\d{2}))\s*\d{2}\s*\d{3}\s*\d{4}(?![\d-])))''':'phone',
           "[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$": "email",
            "^(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$": "ssn",
           "^\\d{10}GB[P,R]\\d{7}[U,M,F]{1}\\d{9}|\\w{1}\\d{7}$": "pport",
           "^\\+(?:\\d ?){6,14}\\d$": "pitut",
           "^\\+\\d{1,3}\\.\\d{4,14}(?:x.+)?$": "pepp",
          #  "^[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?:[ ]?[0-9]{1,2})?$":"iban",
           "^(?:(?:IT|SM)\d{2}[A-Z]\d{22}|CY\d{2}[A-Z]\d{23}|NL\d{2}[A-Z]{4}\d{10}|LV\d{2}[A-Z]{4}\d{13}|(?:BG|BH|GB|IE)\d{2}[A-Z]{4}\d{14}|GI\d{2}[A-Z]{4}\d{15}|RO\d{2}[A-Z]{4}\d{16}|KW\d{2}[A-Z]{4}\d{22}|MT\d{2}[A-Z]{4}\d{23}|NO\d{13}|(?:DK|FI|GL|FO)\d{16}|MK\d{17}|(?:AT|EE|KZ|LU|XK)\d{18}|(?:BA|HR|LI|CH|CR)\d{19}|(?:GE|DE|LT|ME|RS)\d{20}|IL\d{21}|(?:AD|CZ|ES|MD|SA)\d{22}|PT\d{23}|(?:BE|IS)\d{24}|(?:FR|MR|MC)\d{25}|(?:AL|DO|LB|PL)\d{26}|(?:AZ|HU)\d{27}|(?:GR|MU)\d{28})$/i":'iban',
           "^[A-Z9]{5}\d{6}[A-Z9]{2}\d[A-Z]{2}$":"driving_license_uk",
           "^([a-zA-Z]){2}([0-9]){2}([0-9]){2}([0-9]){2}?([a-zA-Z]){1}?$":"ni_number"
           }

    output = []
    Column_PII = {}
    
    #Dropping null rows - if entire row is null
    df = df.dropna(how='all', inplace=False)

    # Dropping null columns - If entire column is null
    df = df.dropna(subset=df.columns, inplace=False)

    df = df.astype(str)

    all_columns = df.columns

    start = 0
    pii_columns = []
    for  col in df.columns:
      start = 0
      end = start + 50
      for value in df[col].values[start:end]:

        try:
            pii = next(True for k, v in pii_pattern.items() if re.match(k, str(value)))
            if pii:
              pii_columns.append(col)
              break;
        except StopIteration:
            continue
      if col not in pii_columns:
        start = start + 50
    # print(pii_columns)
    pii_result_columns = []
    for col in df.columns:
      if col in pii_columns:
        row = {"Column":col, "PII":"Yes"}
        pii_result_columns.append(row)
      else:
        row = {"Column":col, "PII":"No"}
        pii_result_columns.append(row)

    # d  = pd.DataFrame(data=pii_result_columns)
    return pii_result_columns

input_data_file = sys.argv[1]
#Calling the core function
finalResult = processDataFrame(input_data_file);
print(finalResult);