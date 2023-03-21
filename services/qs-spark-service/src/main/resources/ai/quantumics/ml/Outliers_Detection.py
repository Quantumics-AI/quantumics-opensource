import numpy as np
import pandas as pd
import sys
import boto3
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
    df1 = readCsvFileIntoDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey)
    dfedit1 = df1.dropna()
    
    if(df1.shape[0]==dfedit1.shape[0]):
        #print('The data has no missing values')
        jsonobj = json.dumps([])
        return jsonobj;
	
    #selecting only numeric columns
    #numerics = ['int16', 'int32', 'int64', 'float16', 'float32', 'float64']
    numerics = ['int16', 'int32', 'float16', 'float32', 'float64']
    newdf = df1.select_dtypes(include=numerics)
    df2 = newdf

    #Inter Quartile Range
    Q1 = df2.quantile(0.25)
    Q3 = df2.quantile(0.75)
    IQR = Q3 - Q1

    #count of all outliers
    count=((df2 < (Q1 - 1.5 * IQR)) | (df2 > (Q3 + 1.5 * IQR))).sum()
 
    # mask of the data showing the outliers
    mask = (df2 < (Q1 - 1.5 * IQR)) | (df2 > (Q3 + 1.5 * IQR))

	#Outlier rows
    outlierdf = df2[((df2 < (Q1 - 1.5 * IQR)) |(df2 > (Q3 + 1.5 * IQR))).any(axis=1)]
    
    sum1=0
    outlierData = []
    for i in range(df2.columns.size):
        if(count[i]!=0):
            outlierPrefix = {df2.columns[i]:{}}
            newdict = {}
            for j in range((mask.shape[0])):
                if(mask.iloc[j,i]==True):
                    newdict[df2.iloc[j,i]] = "__ML__"+str(df2.iloc[j,i])
                    sum1 = sum1 + 1
            outlierPrefix[df2.columns[i]] = newdict
            outlierdf = outlierdf.replace(outlierPrefix)
   
    result = outlierdf.to_json(orient="split")
    parsed = json.loads(result)
    response = json.dumps(parsed)
    return response;
    
#Calling the core function
bucketName = str(sys.argv[1])
objectKey = str(sys.argv[2])
awsAccessKey = str(sys.argv[3])
awsSecretKey = str(sys.argv[4])

finalResult = processDataFrame(bucketName, objectKey, awsAccessKey, awsSecretKey);
print(finalResult);