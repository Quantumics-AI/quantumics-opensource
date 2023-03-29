<div align="center">

  ![Logo Dark](https://user-images.githubusercontent.com/34600724/218383159-097b28ba-da0d-44db-b690-1473c0d9b716.png#gh-dark-mode-only)

</div>

<div align="center">

  ![Logo Light](https://user-images.githubusercontent.com/34600724/218385621-a77dc8f6-e662-4e1f-bca4-101d9c38275f.png#gh-light-mode-only)

</div>

<p align="center">
    <em>A self-service, easy to use data platform that makes data accessible so that you can focus on your big ideas and your job</em>
</p>
<p align="center">
<a href="https://github.com/Quantumics-AI/quantumics" target="_blank">
    <img src="https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white" alt="Test">
</a>
<a href="https://quantumicsai.slack.com/join/shared_invite/zt-1c2kddx44-x6XU7Eqwrf0Q87oW0IQOfQ?utm_source=substack&utm_medium=email#/shared-invite/email" target="_blank">
    <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white" alt="Slack">
</a>
<a href="https://www.youtube.com/@quantumicsai1434" target="_blank">
    <img alt="YouTube Channel Views" src="https://img.shields.io/badge/YouTube-%23FF0000.svg?style=for-the-badge&logo=YouTube&logoColor=white">
</a>
<a href="https://www.linkedin.com/company/quantumics-ai/" target="_blank">
    <img alt="YouTube Channel Views" src="https://img.shields.io/badge/linkedin-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white">
</a>
<a href="https://www.facebook.com/QuantumicsAI" target="_blank">
    <img alt="YouTube Channel Views" src="https://img.shields.io/badge/Facebook-%231877F2.svg?style=for-the-badge&logo=Facebook&logoColor=white">
</a>
<a href="https://www.instagram.com/quantumics.ai/" target="_blank">
    <img alt="YouTube Channel Views" src="https://img.shields.io/badge/Instagram-%23E4405F.svg?style=for-the-badge&logo=Instagram&logoColor=white">
</a>
</p>

# About Quantumics

Velocity, variety, veracity, and volume of data have exploded over the years. The use of the data has been broadly consistent for the top line or bottom line improvement and compliance. But the complexity of data and operational costs have skyrocketed because of high licence cost for different tools, integration and hard dependence on IT and scarce data resources such as System admins, Data Engineers, and Data Scientists to clean and engineering data.

Every product idea starts with a personal struggle with something. Quantumics.AI founder, Shen Pandi, having worked in the data industry and consulting firms for a decade, found no single SaaS solution in the market to extract value from data with self-servicing capability (ingest, cleanse, engineer, automate, govern with storytelling capability). The need to solve this problem became pressing after working with various clients in different industries who had the same problem; hence there was a desperate need for a better solution.

# Prerequisites.
## Deploy Quantumics.AI required cloud resources 
By using our [cloudformation temlate](https://github.com/Quantumics-AI/quantumics-opensource/blob/main/configurations/AWS_CFT_EMR_QSAI.yaml), you can easly deploy all the required AWS service and configurations on your AWS account. Please read the description in the [cloudformation temlate](https://github.com/Quantumics-AI/quantumics-opensource/blob/main/configurations/AWS_CFT_EMR_QSAI.yaml) to make yourself aware that what are all the services and configuration are deployed. Below is a highlevel services which will be deployed by the template.
```text
1. IAM programatic user with AccessKey and SecreatAccesskey and roles
2. Five numbers of s3 buckets.
3. EMR cluster on EC2 which lunches three EC2 instaces Primery, Core and Task
4. EC2 Machine prefere t3.larg with atlease 30 GB diskspace
```
You have to manually configure security groups as below
```text
Security Grpous configuration 
   - EMR-Master Security group : open below port on 0.0.0.0/0.
        - 8080 To allowing trafic from Quantumics gateway service 
        - 8998 To allowing trafic from EMR Livy traffic  
        - 18080 To allowing trafic from EMR Spark traffic
   - EC2-Security group : open below port on 0.0.0.0/0.
        - 7000 To allowing trafic from Quantumics backend service 
        - 8080 To allowing trafic from Quantumics backend service 
        - 8081 To allowing trafic from Quantumics backend service 
        - 8082 To allowing trafic from Quantumics backend service 
        - 8083 To allowing trafic from Quantumics backend service 
        - 5000 To allowing trafic from Quantumics backend service 
```
### Setting up EMR with python modules
Once the Quantumics.AI [cloudformation temlate](https://github.com/Quantumics-AI/quantumics-opensource/blob/main/configurations/AWS_CFT_EMR_QSAI.yaml) is created sucsufully you need to login to the primery EMR instace by using SSM and run the script [install-python-modules-emr.sh](https://github.com/Quantumics-AI/quantumics-opensource/blob/main/configurations/install-python-modules-emr.sh) to install required python modules. 

# Quick Start
You can run Quantumics.AI locally with Docker.

## Cloning repo and creating Environment file
1. Cloning Quantumics.AI repository and creating .env file
```bash
git clone https://github.com/Quantumics-AI/quantumics-opensource.git
# Change directory to quantumics directory 
cd quantumics
# Create .env file and provide above said information
touch .env
```
2. Edit .env file in the root directory that you created and copy the information in bash format as below. Replace required values. Replace the local host with public IP of your EC2 instace. Do not chage the ports. Copy the Livy url from EMR console of your AWS account. Copy the AccessKey ans SecreateAccessKey from AWS secreates of your accunt. Copy the S3 bucketNames. All this information is avialble in the CLoudformation console. 
```bash
API_URL=http://localhost:8080
DASHBOARD_URL=http://localhost:5000
SERVICE_HOST_URL= http://localhost:8080/
# EMR livy end point
LIVY_URL=${YOUR-LIVY-URLHERE}
# AWS Access keys and Secreate access keys
AWS_ACCESS_KEY=${YOUR-AccessKeysHere}
AWS_SECRET_KEY=${YOUR-SecreateAccessKeysHere}
AWS_REGION=${YOUR-DefaultRegionHere}
# You need create 7 number of s3 bucket frist and then provide the s3 bucket name as below.
S3_IMAGES_PROJECT_BUCKET={PIUIBucktnameCreatedFromCFT}/Project-Images 
S3_IMAGES_USER_BUCKET={PIUIBucktnameCreatedFromCFT}/User-Images 
S3_UDF_BUCKET={UDFBucketNameCreatedFrom CFT} 
S3_ATHENA_QUERY_LOCATION=s3://{AthenaBucketNAmeCreatedFromCFT}/data/ 
S3_CLEANSING_ETL_LOCATION=s3://{ETLBucketNameCreatedFromCFT}/ 
S3_ENG_ETL_LOCATION=s3://{ETLBucketNameCreatedFromCFT}/eng/ 
S3_ENG_RESULT_LOCATION=s3://{ResulutsBucketNameCreatedFromCFT}
PROJECT_NAME={YOUR_USERNAME}-workspace{SOME_RANDOM_STRING}
```
## Initial Setup 
Use below commands to setup the intial setting and running the Quantumics.AI app on your local. Make sure that .env file is created and required environemnt values are provied. Then run docker-compose commands sequentially as shown in below snipet.

```bash
docker-compose build 
docker-compose up -d postgres
docker-compose run --rm server create_db
docker-compose up -d
```
While you are developing and facing any issues in the application, use below command to see logs. Capture the error logs and post it as issues. We will check and update the issues accordingly.

```bash
docker-compose up
```
To configure the data source for dashboard please follow below steps in this [Dcoument](https://github.com/Quantumics-AI/quantumics-opensource/blob/main/configurations/OpesourceDocument.pdf)
 
### If are deleting docker volume which will flush the local db data, it is necessary to update PROJECT_NAME in .env and do not keep the old project name.

## Login and Accessing the Quantumics.AI Application.
Now Login to the Quantumics.AI app at [http://localhost:7000](http://localhost:7000) by entering the default credentials given below.

```
BASIC_AUTH_USERNAME=quantumics
BASIC_AUTH_PASSWORD=quantumics
```
## Quantumics.AI Usage and tutorials.
Follow Quantumics.AI documentations to know how to use Quantumics.AI application and process your data.

Read the [Quantumics.AI docs](https://docs.quantumics.ai).

## Quantumics.AI features : Open-source vs SaaS.
Please see this [Dcoument](https://github.com/Quantumics-AI/quantumics-opensource/blob/main/Quantumics_Features.pdf) to know about the features available in opensource Quantumics.AI application and Quantumics.AI SaaS platform.

# Use Quantumics.AI SaaS

Quantumics.AI SaaS is the fastest and most reliable way to run Quantumics.AI. You can get started with 14 days free trail in minutes.

Sign up for [Quantumics.AI SaaS](https://app.quantumics.ai/signup).

# Contributing

Get started by checking Github issues and creating a Pull Request. An easy way to start contributing is to update an existing services or create a new service using the Java SDKs and Python CDKs. You can find the code for existing services in the [services](https://github.com/Quantumics-AI/quantumics/services) directory. The Quantumics.AI platform is written in Java, Python and the frontend in React.

## Reporting vulnerabilities

⚠️ Please do not file GitHub issues or post on our public forum for security vulnerabilities as they are public! ⚠️

Quantumics.AI takes security issues very seriously. If you have any concerns about Quantumics.AI or believe you have uncovered a vulnerability, please get in touch via the e-mail address devops@quantumics.ai. In the message, try to provide a description of the issue and ideally a way of reproducing it. The security team will get back to you as soon as possible.

Note that this security address should be used only for undisclosed vulnerabilities. Dealing with fixed issues or general questions on how to use the security features should be handled regularly via the user and the dev lists. Please report any security problems to us before disclosing it publicly.

# License

GNU Affero General Public License v3.0
