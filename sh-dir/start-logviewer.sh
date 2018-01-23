#!/bin/bash

STORM_HOME=`cat /home/team1/seokwoo/sh-dir/storm-dir.txt`
SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
eval "nohup ${STORM_HOME}/bin/storm logviewer > ${SHDIR}/logviewer.log 2>&1 &"

LOGVIEWER_BACKGROUND_PID=$!
export LOGVIEWER_BACKGROUND_PID
echo $LOGVIEWER_BACKGROUND_PID > ${SHDIR}/logviewer.pid
