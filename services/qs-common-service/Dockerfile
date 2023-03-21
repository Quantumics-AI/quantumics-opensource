### Service Layer Docker File

## BASE IMAGE - Maven
FROM quantumics/qsaiofficial:qsai-services
RUN apt-get update && apt-get upgrade -y
COPY qs-common-service /usr/src/app/qs-common-service
WORKDIR /usr/src/app/qs-common-service
RUN mvn clean package
## ADD target/QSCommonService.jar /QSCommonService.jar
## ML Modules
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

## PORT
EXPOSE 8090 8083 485 587

## Setting up entrypoint
COPY qs-common-service/src/main/resources /
ENTRYPOINT  java -Dspring.profiles.active=sat  -jar target/QSCommonService.jar
