#!/bin/bash

if (docker ps -a | grep meetup_spark_container) then
  docker ps -a | grep meetup_spark_container | awk {' print $1 '} | xargs docker rm -f
fi
docker build -t meetup_spark .
docker run -d -p 3306:3306 --name meetup_spark_container  meetup_spark
