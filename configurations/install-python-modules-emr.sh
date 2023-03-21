#!/bin/bash -xe

# Non-standard and non-Amazon Machine Image Python modules:

sudo yum install -y python3-devel 


sudo pip3 install -U \
  s3fs \
  fsspec \
  unidecode \
  Smart_open \
  requests \
  pandas \
  pyarrow==2