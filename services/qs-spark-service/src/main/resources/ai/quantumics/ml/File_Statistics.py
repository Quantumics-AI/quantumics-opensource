import csv
import pandas as pd
import numpy as np
import json
import boto3
import sys
import os

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

def get_size(size):
    if size < 1024:
        return f"{size} bytes"
    elif size < pow(1024,2):
        return f"{round(size/1024, 2)} KB"
    elif size < pow(1024,3):
        return f"{round(size/(pow(1024,2)), 2)} MB"
    elif size < pow(1024,4):
        return f"{round(size/(pow(1024,3)), 2)} GB"

def readCsvFileIntoDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey):
    session = createAwsSession(awsAccessKey, awsSecretKey)
    s3 = session.resource('s3')
    bucket = s3.Bucket(bucketName)
    obj = bucket.Object(key=objectKey)
    response = obj.get()
    lines = response['Body'].read()
    #csv_string = lines.decode('cp1252')
    csv_string = lines.decode('cp437')
    strData = StringIO(csv_string)
    file_length = get_size(len(csv_string))
    df = pd.read_csv(strData)

    return (df, file_length);


def np_encoder(object):
    if isinstance(object, np.generic):
        return object.item()

def getColumnDTypeAsString(colDType):
    columnDataType = None
    if colDType == np.int64:
        columnDataType = 'int64'
    elif colDType == np.float64:
        columnDataType = 'float64'
    elif colDType == np.object:
        columnDataType = 'string'
    else:
        columnDataType = 'string'

    return columnDataType;

def col_types(df):
    df_cols = df.columns
    df_cols_dict = {}
    for column in df_cols:
        df_cols_dict[column] = getColumnDTypeAsString(df[column].dtypes)

    return df_cols_dict

def readAndComputeStats(file1BucketName, file1ObjKey, awsAccessKey, awsSecretKey):
    result = readCsvFileIntoDataFrame(file1BucketName, file1ObjKey, awsAccessKey, awsSecretKey)

    df1 = result[0]

    # Columns list
    # columnsList = list(df1)
    columnsList = col_types(df1)

    rowCount = df1.shape[0]
    colCount = df1.shape[1]

    file_stats = {'fileStats': {
			'recordCount':rowCount,
			'columnCount':colCount,
			'columns':columnsList,
            'fileSize': result[1],
		 }}

    columns_stats = []
    final = []
    final.append(file_stats)

    # Iterate through the Columns list
    for column in columnsList:
        # Column datatype..
        colDType = df1[column].dtype

        # Null values count..
        null_count = df1[column].isnull().sum()

        # Not Null values count..
        not_null_count = df1[column].notnull().sum()

        # Duplicates count..
        #dup_count = df1.pivot_table(index=[column], aggfunc='size')
        dup_count = df1.duplicated(column).sum()

        # Unique values count..
        #unique_f1 = df1[column].value_counts()
        unique_f1 = len(pd.unique(df1[column]))

        # Frequency values..
        #val_frequency = df1[column].value_counts().to_dict()

        colMean = 0.0
        colMedian = 0.0
        #colMode = df1[column].mode(axis=1)
        colVariance = 0.0
        colDataType = 0.0
        colSumb = 0.0
        colSTD	= 0.0

        if colDType == np.int64 or colDType == np.float64 or colDType == np.float16 or colDType == np.float32:
            # Mean..
            colMean = df1[column].mean(skipna=True)
            colMedian = df1[column].median(skipna=True)
            colVariance = df1[column].var(skipna=True)
            minValue = df1[column].min(skipna=True)
            maxValue = df1[column].max(skipna=True)
            colSumb = df1[column].sum(skipna=True)
            colSTD = df1[column].std(skipna=True)

            # stadnDiv
            # sum
        else:
            #Column Datatype is string here..
            #colDataType = 'string'

            # Mean..
            colMean = 0.0

            # Median..
            colMedian = 0.0

            # Variance..
            colVariance = 0.0

            # Min value..
            minValue = 0.0

            # Max value..
            maxValue = 0.0

            # Sum Value..
            colSumb = 0.0
            colSTD = 0.0

        column_stats = {
        	               column: {
        	          	               'attributeCount':{'null':null_count, 'notNull':not_null_count, 'duplicate':dup_count, 'distinct':unique_f1, 'mean':colMean},
        	          	               'statisticsParams':{'mean':colMean, 'median':colMedian, 'variance': colVariance, 'min':minValue, 'max': maxValue, 'sum': colSumb, 'std': colSTD}
				                   }
        	           }
        columns_stats.append(column_stats)

    finalColStats = {
    			        'columnsStats': columns_stats
    		        }

    final.append(finalColStats)
    response = {'file1_stats': final}

    jsonobj = json.dumps(response, default=np_encoder)
    return jsonobj


# Calling delta feature

file1BucketName = str(sys.argv[1])
file1ObjKey = str(sys.argv[2])
awsAccessKey = str(sys.argv[3])
awsSecretKey = str(sys.argv[4])

response = readAndComputeStats(file1BucketName, file1ObjKey, awsAccessKey, awsSecretKey)
print(response)