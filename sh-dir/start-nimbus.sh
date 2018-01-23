#!/bin/bash

STORM_HOME=`cat /home/team1/seokwoo/sh-dir/storm-dir.txt`
SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
eval "nohup ${STORM_HOME}/bin/storm nimbus > ${SHDIR}/nimbus.log 2>&1 &"

NIMBUS_BACKGROUND_PID=$!
export NIMBUS_BACKGROUND_PID
echo $NIMBUS_BACKGROUND_PID > ${SHDIR}/nimbus.pid
