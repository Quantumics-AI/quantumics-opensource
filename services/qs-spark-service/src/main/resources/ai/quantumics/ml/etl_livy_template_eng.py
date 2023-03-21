import pyspark
import pyspark.sql.functions as f
from pyspark.sql import SQLContext, SparkSession, functions as F
from pyspark.context import SparkContext
from pyspark.sql.types import *
from pyspark.sql.functions import *
from py4j.protocol import Py4JJavaError
import pandas as pd
import csv
import re
import json
import requests
import logging


URL=$URL_PREFIX
API_ENDPOINT= URL + 'QuantumSparkServiceAPI/api/v1/ops/engflowevent'
token = 'Quantumics'
headers1 = {'Authorization': token, 'Content-Type': 'application/json'}


# SPARK Session and Glue Catalog Configuration (Important)
spark = SparkSession.builder.appName("Quantumics").config("hive.metastore.client.factory.class", "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory").enableHiveSupport().getOrCreate()

# Process the events.

