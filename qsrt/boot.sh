#!/bin/sh

exec manage.py database create_tables
exec manage.py users create_root adminqsai@quantumspark.ai adminqsai --password admin@123