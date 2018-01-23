#!/bin/bash

if [ $# -lt 1 ] ; then
    echo "Usage: $0 [start | stop]";
    exit 0;
else

shdir=/home/team1/seokwoo/sh-dir
zoohome=/home/team1/seokwoo/zookeeper
	if [ "$1" == "start" ]; then
    	echo "[$HOSTNAME]: zkServer.sh start!!";
    	$zoohome/bin/zkServer.sh start;

    	for slave in $(cat /home/team1/seokwoo/sh-dir/nodes.txt); do
       	 	echo "ssh -p 10022 $slave "$zoohome/bin/zkServer.sh start!!" ";
        	ssh -p 10022 $slave "$zoohome/bin/zkServer.sh start";
    	done

	elif [ "$1" == "stop" ]; then
    	echo "[$HOSTNAME]: zkServer.sh stop!!";
    	$zoohome/bin/zkServer.sh stop;

    	for slave in $(cat /home/team1/seokwoo/sh-dir/nodes.txt); do
        	echo "ssh -p 10022 $slave "$zoohome/bin/zkServer.sh stop!!" ";
        	ssh -p 10022 $slave "$zoohome/bin/zkServer.sh stop";
    	done
	fi
fi
