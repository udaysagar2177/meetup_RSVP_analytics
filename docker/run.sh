#!/bin/bash

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

mysql -h "localhost" -u spark --password=spark MeetupRsvps -e \
  "SET GLOBAL query_cache_size = 16777216"

mysql -h "localhost" -u spark --password=spark MeetupRsvps -e \
  "CREATE TABLE Persons ( PersonID int);
  INSERT INTO Persons VALUES ( 1 );
  INSERT INTO Persons VALUES ( 2 );"

while true; do
  sleep 1
  mysql -h "localhost" -u spark --password=spark MeetupRsvps -e \
    "INSERT INTO Persons VALUES (3)"
done
