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
    csv_string = lines.decode('cp1252')
    df = pd.read_csv(StringIO(csv_string))    
    
    return df;


def np_encoder(object):
    if isinstance(object, np.generic):
        return object.item()

def readAndComputeStats(file1BucketName, file1ObjKey, file2BucketName, file2ObjKey, awsAccessKey, awsSecretKey):
  #df1 = pd.read_csv(file1)
  #df2 = pd.read_csv(file2)
  
  df1 = readCsvFileIntoDataFrame(file1BucketName, file1ObjKey, awsAccessKey, awsSecretKey)
  df2 = readCsvFileIntoDataFrame(file2BucketName, file2ObjKey, awsAccessKey, awsSecretKey)

  # Null values
  null_f1 =  df1.isnull().sum().to_dict()
  null_f2 =  df2.isnull().sum().to_dict()

  #Unique values count
  unique_f1 = df1.nunique(axis=0).to_dict()
  unique_f2 = df2.nunique(axis=0).to_dict()

  # Missing values count
  missing_count_f1 =  df1.isna().sum().to_dict()
  missing_count_f2 =  df2.isna().sum().to_dict()

  # Descriptive Statistics
  stats1 = df1.describe(include='all')
  stats2 = df2.describe(include='all')

  stats1 = stats1.replace(np.nan,'-')
  stats2 = stats2.replace(np.nan,'-')

  f1_row1 = {'Null':null_f1,'Unique': unique_f1, 'Missing': missing_count_f1}
  f2_row1 = {'Null':null_f2,'Unique': unique_f1, 'Missing': missing_count_f2}

  for i in range(len(stats1)):
    f1_row1[stats1.index[i]] = stats1.iloc[i].to_dict()
    f2_row1[stats2.index[i]] = stats2.iloc[i].to_dict()

  newdf1 = pd.DataFrame(data=f1_row1)
  
  newdf2 = pd.DataFrame(data=f2_row1)

  f1 = newdf1.T.to_dict()
  f2 = newdf2.T.to_dict()

  final = []

  for column in f1:
    # Both Numerical and Categorical values common stats
    diff = {'null_diff':f2[column]['Null'] - f1[column]['Null'], 
            'unique':f2[column]['Unique'] - f1[column]['Unique'],
            'Missing':f2[column]['Missing'] - f1[column]['Missing'],
            'Count':f2[column]['count'] - f1[column]['count']
            }
    # Numerical values
    if f1[column]['top'] == '-':
      diff_numericals = {
        'mean': f2[column]['mean'] -  f1[column]['mean'], 
          'std':f2[column]['std'] - f1[column]['std'],
          '25%':f2[column]['25%'] - f1[column]['25%'],
          '50%':f2[column]['50%'] - f1[column]['50%'],
          '75%':f2[column]['75%'] - f1[column]['75%'],
          'max':f2[column]['max'] - f1[column]['max'],
          'min':f2[column]['min'] - f1[column]['min']
        }
      row = {'column_name':column, 
           'Nullvalues':{'file1':f1[column]['Null'], 'file2':f2[column]['Null'], 'diff':diff['null_diff'], 'sign': '+' if diff['null_diff'] >= 0 else  '-'}, 
           'Unique':{'file1':f1[column]['Unique'], 'file2':f2[column]['Unique'], 'diff':diff['unique'], 'sign': '+' if diff['unique'] >= 0 else '-'},
           'Missing':{'file1':f1[column]['Missing'], 'file2':f2[column]['Missing'],'diff':diff['Missing'], 'sign': '+' if diff['Missing'] >= 0 else '-' },
           'RowCount':{'file1':f1[column]['count'], 'file2':f2[column]['count'], 'diff':diff['Count'], 'sign': '+' if diff['Count'] >= 0 else '-'},
           'Maximum':{'file1':f1[column]['max'], 'file2':f2[column]['max'],'diff':diff_numericals['max'], 'sign': '+' if diff_numericals['max'] >= 0 else '-'},
           'Minimum':{'file1':f1[column]['min'], 'file2':f2[column]['min'],'diff':diff_numericals['min'], 'sign': '+' if diff_numericals['min'] >= 0 else '-'},
           'Mean':{'file1':f1[column]['mean'], 'file2':f2[column]['mean'],'diff':diff_numericals['mean'], 'sign': '+' if diff_numericals['mean'] >= 0 else '-'},
           'StandardDeviation':{'file1':f1[column]['std'], 'file2':f2[column]['std'],'diff':diff_numericals['std'], 'sign': '+' if diff_numericals['std'] >= 0 else '-'},
            '25%':{'file1':f1[column]['25%'], 'file2':f2[column]['25%'],'diff':diff_numericals['25%'], 'sign': '+' if diff_numericals['25%'] >= 0 else '-'},
           '50%':{'file1':f1[column]['50%'], 'file2':f2[column]['50%'],'diff':diff_numericals['50%'], 'sign': '+' if diff_numericals['50%'] >= 0 else '-'},
           '75%':{'file1':f1[column]['75%'], 'file2':f2[column]['75%'],'diff':diff_numericals['75%'], 'sign': '+' if diff_numericals['75%'] >= 0 else '-'},
           'Top':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           'Frequency':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           'unique':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'}
      }
      final.append(row)

    # Categorical values
    else:
      diff_catg = {
          'top': 'NoChange' if f2[column]['top'] ==  f1[column]['top'] else f2[column]['top'],
          'freq': 'NoChange' if f2[column]['freq'] ==  f1[column]['freq'] else f2[column]['freq'],
          'unique': 'NoChange' if f2[column]['unique'] ==  f1[column]['unique'] else f2[column]['unique']
      }
      row = {'column_name':column, 
           'Nullvalues':{'file1':f1[column]['Null'], 'file2':f2[column]['Null'], 'diff':diff['null_diff'], 'sign': '+' if diff['null_diff'] >= 0 else  '-'}, 
           'Unique':{'file1':f1[column]['Unique'], 'file2':f2[column]['Unique'], 'diff':diff['unique'], 'sign': '+' if diff['unique'] >= 0 else '-'},
           'Missing':{'file1':f1[column]['Missing'], 'file2':f2[column]['Missing'],'diff':diff['Missing'], 'sign': '+' if diff['Missing'] >= 0 else '-' },
           'RowCount':{'file1':f1[column]['count'], 'file2':f2[column]['count'], 'diff':diff['Count'], 'sign': '+' if diff['Count'] >= 0 else '-'},
           'Maximum':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           'Minimum':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           'Mean':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           'StandardDeviation':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
            '25%':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           '50%':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           '75%':{'file1':'N/A', 'file2':'N/A','diff':'N/A',  'sign': 'N/A'},
           'Top':{'file1':f1[column]['top'], 'file2':f2[column]['top'],'diff':diff_catg['top'],  'sign': '=' if diff_catg['top'] ==  'NoChange' else '+'},
           'Frequency':{'file1':f1[column]['freq'], 'file2':f2[column]['freq'],'diff':diff_catg['freq'], 'sign': '=' if diff_catg['freq'] ==  'NoChange' else '+'},
           'unique':{'file1':f1[column]['unique'], 'file2':f2[column]['unique'],'diff':diff_catg['unique'],  'sign': '=' if diff_catg['unique'] ==  'NoChange' else '+'}
      }
      final.append(row)

  #File1 & file2 individual stats output formatting
  newdf1 = newdf1.rename_axis(['Column_name']).reset_index()
  newdf2 = newdf2.rename_axis(['Column_name']).reset_index()
  file1_stats = newdf1.to_dict('records')
  file2_stats = newdf2.to_dict('records')
  response = {'file1_stats': file1_stats, 'file2_stats': file1_stats, 'difference': final}

  jsonobj = json.dumps(response, default=np_encoder)
  return jsonobj


# Calling delta feature
#response = readAndComputeStats("transactions_200614.csv", "transactions_200615.csv")

file1BucketName = str(sys.argv[1])
file1ObjKey = str(sys.argv[2])
file2BucketName = str(sys.argv[3])
file2ObjKey = str(sys.argv[4])
awsAccessKey = str(sys.argv[5])
awsSecretKey = str(sys.argv[6])

response = readAndComputeStats(file1BucketName, file1ObjKey, file2BucketName, file2ObjKey, awsAccessKey, awsSecretKey)
print(response)