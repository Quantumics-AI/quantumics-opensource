import pyspark
import pyspark.sql.functions as f
from pyspark.sql import SQLContext, SparkSession, functions as F
from pyspark.context import SparkContext
from pyspark.sql.types import *
from pyspark.sql.functions import *
from py4j.protocol import Py4JJavaError
from unidecode import unidecode
import re
import pandas as pd
import csv
import unicodedata

 
# SPARK Session & Glue Catalog Configuration (Important)
spark = SparkSession.builder.appName("Quantumics").config("hive.metastore.client.factory.class", "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory").enableHiveSupport().getOrCreate()

dbname=$DB_NAME
dbtable=$DB_TABLE
columns=$COL_ARRAY
filterCondition=$FILTER_CONDITION
s3Path=$s3_OUT_PUT_PATH
fileName=$CSV_FILE_NAME
partitionName=$PARTITION_NAME
finalProcessedFile=s3Path+'/'+fileName

# DataFrame (Creating the Dataframe from the tables of Glue Catalog DB)
#spark.catalog.setCurrentDatabase(dbname)
df = spark.sql('select '+columns+' from '+dbname+'.'+dbtable+' where partition_0="'+partitionName+'"')
df = df.drop('partition_0') # Deleting the Partition Column
df = df.filter(filterCondition)

#collData=df.collect()
#collData.pop(0)
#df = spark.createDataFrame(collData)
#df.createOrReplaceTempView("df")

###$DEF;



####$CODE;




#collData=df.collect()
#collData.pop(0)

#dfNew = spark.createDataFrame(collData)
#dfNew.repartition(1).write.format('csv').options(header='true', mode='overWrite').csv(s3Path)
df.repartition(1).write.format('csv').options(header='true', mode='overWrite').csv(s3Path)

#output_df = pd.DataFrame(collData, columns= df.columns)
#output_df.to_csv(finalProcessedFile, mode = 'w', index=False, encoding='utf-8', escapechar='\\')

# Glue Catalog DB (Selecting the DB to load tables)
#spark.catalog.setCurrentDatabase(dbname)

# Updating the glue catalog
#df.write.mode('overwrite').format('parquet').option('path', s3Path).saveAsTable(dbtable)


