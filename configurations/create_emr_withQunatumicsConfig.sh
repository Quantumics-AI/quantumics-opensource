#!/bin/bash
####################  QUANTUMICS.AI EMR CONFIGURATIONS #######################
# By using and running this script you are going to create EMR cluster on EC2.
# Make sure you know waht you are doing. 
# Replace the Values in ${} before you run this script as per your reuirment. 
##############################################################################
aws emr create-cluster \
--release-label emr-6.4.0 \
--instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=${YOUR_REQUIRED_INSTACE} InstanceGroupType=CORE,InstanceCount=1,InstanceType=${YOUR_REQUIRED_INSTACE} \
--use-default-roles \
--ec2-attributes SubnetId=${YOUR_SUBNETID},KeyName=${YOUR_KEY} \
--applications Name=Livy Name=Spark Name=Hadoop Name=Zeppelin Name=Ganglia \
--name=“${YOUR_CLUSTER_NAME}” \
--log-uri ${YOUR_S3_BUCKET_NAME} \
--steps Type=CUSTOM_JAR,Name="Setup hadoop debugging",ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar,Args=["state-pusher-script"] \
--step-concurrency-level 10 \
--configurations file://configurations.json \
--region ${YOUR_AWS_REGION}