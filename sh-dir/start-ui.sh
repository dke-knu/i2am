#!/bin/bash

STORM_HOME=`cat /home/team1/seokwoo/sh-dir/storm-dir.txt`
SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
eval "nohup ${STORM_HOME}/bin/storm ui > ${SHDIR}/ui.log 2>&1 &"

UI_BACKGROUND_PID=$!
export UI_BACKGROUND_PID
echo $UI_BACKGROUND_PID > ${SHDIR}/ui.pid
