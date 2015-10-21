#!/bin/bash

while true
do 
count=0
count=$(( $count + 1 ))
echo "Curl Attempt ${count}" >> /opt/meetup_data1_log.txt
start=$(date)
echo "Start time: ${start}" >> /opt/meetup_data1_log.txt
curl http://stream.meetup.com/2/rsvps >> /opt/meetup_data1.json
end=$(date)
echo "End time: ${end}" >> /opt/meetup_data1_log.txt
done
