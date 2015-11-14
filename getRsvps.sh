#!/bin/bash

count=0
datafilename="meetup_data1.txt"
logfilename="meetup_data1_log.txt"

while true
do
  count=$(( $count + 1 ))
  echo "Curl Attempt ${count}" >> /opt/$logfilename
  start=$(date)
  echo "Start time: ${start}" >> /opt/$logfilename
  curl http://stream.meetup.com/2/rsvps >> /opt/$datafilename
  end=$(date)
  echo "End time: ${end}" >> /opt/$logfilename
done
