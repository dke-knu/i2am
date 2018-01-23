#!/bin/bash

STORM_HOME=`cat /home/team1/seokwoo/sh-dir/storm-dir.txt`
SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
eval "nohup ${STORM_HOME}/bin/storm supervisor > ${SHDIR}/supervisor.log 2>&1 &"

SUPERVISOR_BACKGROUND_PID=$!
export SUPERVISOR_BACKGROUND_PID
echo $SUPERVISOR_BACKGROUND_PID > ${SHDIR}/supervisor.pid
