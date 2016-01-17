#!/bin/bash
# Bash script to get Rsvps from http://stream.meetup.com/2/rsvps
# Received RSVPs are stored in $datafilename
# Log for script is written into $logfilename

count=0 # keeps tract of curl attempts
datafilename="meetup_data.txt"
logfilename="meetup_data_log.txt"

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
