FROM quantumics/qsaiofficial:qsai-services
RUN apt-get update && apt-get upgrade -y
COPY qs-spark-service /usr/src/app/qs-spark-service
#COPY . /usr/src/app/qs-spark-service
WORKDIR /usr/src/app/qs-spark-service
RUN mvn clean package
##ADD target/QuantumSparkServiceAPI.jar /QuantumSparkServiceAPI.jar

## Exposed required ports
EXPOSE 8090 8082 485 587

## Installing and setting up python modules
RUN apt-get update
RUN apt-get install -y
#RUN apt install python3-pip -y
#RUN pip3 install pandas
RUN python3 -c 'import pandas'
RUN python3 -m pip install boto3
RUN python3 -c 'import boto3'
RUN python3 -m pip install smart_open
RUN python3 -c 'import smart_open'
RUN python3 -m pip install fsspec
RUN python3 -c 'import fsspec'
RUN python3 -m pip install s3fs
RUN python3 -c 'import s3fs'
RUN python3 -m pip install unidecode
RUN python3 -c 'import unidecode'
RUN python3 -m pip install requests
RUN python3 -c 'import requests'
RUN python3 -m pip install openpyxl
RUN python3 -c 'import openpyxl'
RUN python3 -m pip install xlrd
RUN python3 -c 'import xlrd'

## Setting up entrypoint
COPY qs-spark-service/src/main/resources /usr/src/app/qs-spark-service
ENTRYPOINT  java -Dspring.profiles.active=sat -jar target/QuantumSparkServiceAPI.jar
