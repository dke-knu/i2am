#!/bin/bash

for i in {1..100}; do
jps | grep worker | awk '{ split($0, arr, " "); printf("%s\n", arr[1]); }' > ./jpslist.txt 
	for workerPID in $(cat ./jpslist.txt); do
		top -b -d 1 -n 1 -p ${workerPID} | grep ${workerPID} | tee -a ./cpuinfo.txt;	
	done
done

