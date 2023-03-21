import pandas as pd
import re
import boto3
import csv
import sys
import math
from smart_open import smart_open
import json

if sys.version_info[0] < 3: 
    from StringIO import StringIO # Python 2.x
else:
    from io import StringIO # Python 3.x

def createAwsSession(awsAccessKey, awsSecretKey):
    session = boto3.Session(
    aws_access_key_id=awsAccessKey,
    aws_secret_access_key=awsSecretKey,
    region_name='eu-west-2')
    
    return session;

def readCsvFileIntoDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey):
    session = createAwsSession(awsAccessKey, awsSecretKey)
    s3 = session.resource('s3')
    bucket = s3.Bucket(bucketName)
    obj = bucket.Object(key=objectKey)
    response = obj.get()
    lines = response['Body'].read()
    csv_string = lines.decode('cp1252')
    df = pd.read_csv(StringIO(csv_string))    
    
    return df;
    
def processDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey):
    df = readCsvFileIntoDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey)
    
    #Regex for every vendor type
    pii_pattern = {
               '((?:(?:\\d{4}[- ]?){3}\\d{4}|\\d{15,16}))(?![\\d])':'credit_card',
               '^4[0-9]{12}(?:[0-9]{3})?$':'credit_card_visa',
               '^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$':'credit_card_master',
               '\d{1,4} [\w\s]{1,20}(?:street|st|avenue|ave|road|rd|highway|hwy|square|sq|trail|trl|drive|dr|court|ct|park|parkway|pkwy|circle|cir|boulevard|blvd)\W?(?=\s|$)':'street_addres',
               '^3[47][0-9]{13}$':'credit_card_amex',
               '^3(?:0[0-5]|[68][0-9])[0-9]{11}$':'credit_card_diners',
               '^6(?:011|5[0-9]{2})[0-9]{12}$':'credit_card_discover',
               '^(?:2131|1800|35\d{3})\d{11}$':'credit_card_jcb',
               '''((?:(?<![\d-])(?:\+?\d{1,3}[-.\s*]?)?(?:\(?\d{3}\)?[-.\s*]?)?\d{3}[-.\s*]?\d{4}(?![\d-]))|(?:(?<![\d-])(?:(?:\(\+?\d{2}\))|(?:\+?\d{2}))\s*\d{2}\s*\d{3}\s*\d{4}(?![\d-])))''':'phone',
               "[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$": "email",
               "^(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$": "ssn",
               "^\\d{10}GB[P,R]\\d{7}[U,M,F]{1}\\d{9}|\\w{1}\\d{7}$": "pport",
               "^\\+(?:\\d ?){6,14}\\d$": "pitut",
               "^\\+\\d{1,3}\\.\\d{4,14}(?:x.+)?$": "pepp",
               "^[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?:[ ]?[0-9]{1,2})?$":"iban",
               "^[A-Z9]{5}\d{6}[A-Z9]{2}\d[A-Z]{2}$":"driving_license_uk",
               "^([a-zA-Z]){2}([0-9]){2}([0-9]){2}([0-9]){2}?([a-zA-Z]){1}?$":"ni_number"
           }
    
    output = []
    
    #Dropping null rows - if entire row is null
    df = df.dropna(how='all', inplace=False)
    
    # Dropping null columns - If entire column is null
    df = df.dropna(subset=df.columns, inplace=False)
    df = df.astype(str)
    
    for row in df.itertuples(index=False):
        Column_PII = {}
        for column in df.columns:
           value = row[df.columns.get_loc(column)]
           try:
                pii = next(True for k, v in pii_pattern.items() if re.match(k, str(value)))
                if pii:
                     value = "__ML__"+value
           except StopIteration:
                Column_PII[column] = value
                continue
           
           Column_PII[column] = value
           output.append(Column_PII)
           
    df  = pd.DataFrame(data=output)
    df = df.drop_duplicates()
    dff = df.to_dict('records')
    jsonobj = json.dumps(dff)
    return jsonobj;

#Calling the core function
bucketName = str(sys.argv[1])
objectKey = str(sys.argv[2])
awsAccessKey = str(sys.argv[3])
awsSecretKey = str(sys.argv[4])

finalResult = processDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey);
print(finalResult);