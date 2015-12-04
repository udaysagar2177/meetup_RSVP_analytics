#!/bin/bash
set -xe

trap "exit" SIGINT SIGTERM
mkdir -p /var/docker-share/logs

sed -i "s/^bind-address/#bind-address/" /etc/mysql/my.cnf
service mysql start

mysql -h "localhost" -u root -e \
  "CREATE USER 'spark'@'localhost' IDENTIFIED BY 'spark';
   GRANT ALL PRIVILEGES ON * . * TO 'spark'@'localhost';
   CREATE USER 'spark'@'%' IDENTIFIED BY 'spark';
   GRANT ALL PRIVILEGES ON * . * TO 'spark'@'%';
   FLUSH PRIVILEGES;
    ";

mysql -h "localhost" -u spark --password=spark -e "CREATE DATABASE MeetupRsvps;"

mysql -h "localhost" -u spark --password=spark -e "CREATE TABLE MeetupRsvps.EventData (
  visibility VARCHAR(10),
  event_id VARCHAR(100),
  event_name VARCHAR(1000),
  event_time TIMESTAMP,
  group_topics VARCHAR(1000),
  group_city VARCHAR(100),
  group_country VARCHAR(10),
  group_name VARCHAR(1000),
  group_lon Decimal(9,6),
  group_lat Decimal(9,6),
  PRIMARY KEY (event_id)
  )
  "

mysql -h "localhost" -u spark --password=spark -e "CREATE TABLE MeetupRsvps.RsvpData (
  rsvp_time TIMESTAMP,
  event_id VARCHAR(100)
  )
  "

/opt/spark/bin/spark-submit --class com.udaysagar.meetup_rsvps.App meetup_rsvps-jar-with-dependencies.jar

while true; do
  sleep 1
done
