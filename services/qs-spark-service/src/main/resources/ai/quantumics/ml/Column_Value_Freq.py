import csv
import pandas as pd
import numpy as np
import json
import boto3
import sys
from smart_open import smart_open

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
    #csv_string = lines.decode('cp1252')
    csv_string = lines.decode('cp437')
    df = pd.read_csv(StringIO(csv_string))

    return df;

def np_encoder(object):
    if isinstance(object, np.generic):
        return object.item()

def readColumnFrequency(file1BucketName, file1ObjKey, columnName, awsAccessKey, awsSecretKey):
    df1 = readCsvFileIntoDataFrame(file1BucketName, file1ObjKey, awsAccessKey, awsSecretKey)
    df1_cols = df1.columns

    if columnName == 'EMPTY':
        columnName = df1_cols[0]

    columns_stats = []
    val_frequency = df1[columnName].value_counts().to_dict()
    sorted(val_frequency.items(), key=lambda x: x[1], reverse=True)

    # Get only a max of 50 elements
    val_frequency_50 = {}
    count=0
    for key, value in val_frequency.items():
        if count >= 50:
            break
        val_frequency_50[key] = value;
        count = count+1

    column_stats = {
            	       columnName: {
            	                   'valueFrequency':val_frequency_50
    			           }
        	       }
    response = {'column_value_frequency': column_stats}

    jsonobj = json.dumps(response, default=np_encoder)
    return jsonobj

# Calling delta feature

file1BucketName = str(sys.argv[1])
file1ObjKey = str(sys.argv[2])
columnName = None

if len(sys.argv) < 4:
    columnName = 'EMPTY'
else:
    columnName = str(sys.argv[3])

awsAccessKey = str(sys.argv[4])
awsSecretKey = str(sys.argv[5])

response = readColumnFrequency(file1BucketName, file1ObjKey, columnName, awsAccessKey, awsSecretKey)
print(response)