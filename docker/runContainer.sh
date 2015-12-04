#!/bin/bash

if (docker ps -a | grep meetup_spark_container) then
  docker ps -a | grep meetup_spark_container | awk {' print $1 '} | xargs docker rm -f
fi

# Build the jar file
(cd ../meetup_rsvps
mvn package)

# Copy the new jar file into Docker directory, build image and remove it
cp ../meetup_rsvps/target/meetup_rsvps-jar-with-dependencies.jar .
docker build -t meetup_spark .
rm meetup_rsvps-jar-with-dependencies.jar

# Run the container
docker run -it -p 3306:3306 --name meetup_spark_container  meetup_spark